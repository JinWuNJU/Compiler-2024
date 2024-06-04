import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static org.bytedeco.llvm.global.LLVM.*;



public class MyVisit extends SysYParserBaseVisitor<LLVMValueRef>{
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");

    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();

    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();

    private llvmSymbolTable GlobalScope = new llvmSymbolTable(null);
    private llvmSymbolTable currentScope = GlobalScope;

    //private Map<String, LLVMValueRef> symbolTable = new HashMap<>();

    private LLVMBasicBlockRef currentBlock;
    private LLVMValueRef currentFunction;

    private Stack<LLVMBasicBlockRef> loopBeginStack = new Stack<>();
    private Stack<LLVMBasicBlockRef> loopEndStack = new Stack<>();

    LLVMValueRef zeroValue = LLVM.LLVMConstInt(i32Type, 0, 0);

    //初始化LLVM
    public MyVisit(){
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
    }

    public LLVMModuleRef getModule() {
        return module;
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        String functionName = ctx.IDENT().getText();
        LLVMTypeRef returnType = (ctx.functype().VOID() != null) ? LLVM.LLVMVoidType() : i32Type;
        // 获取函数参数列表
        List<SysYParser.FuncFParamContext> params = ctx.funcFParams() != null ? ctx.funcFParams().funcFParam() : Collections.emptyList();

        // 计算参数个数
        int numParams = params.size();

        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(numParams);

        for (int i = 0; i < numParams; i++) {
            argumentTypes.put(i,i32Type);
        }
        LLVMTypeRef functionType = LLVM.LLVMFunctionType(returnType, argumentTypes, numParams, 0);
        LLVMValueRef function = LLVM.LLVMAddFunction(module, functionName, functionType);

        currentScope.define(functionName, function);

        LLVMBasicBlockRef entry = LLVM.LLVMAppendBasicBlock(function, functionName + "Entry");
        LLVM.LLVMPositionBuilderAtEnd(builder, entry);

        // 保存当前基本块
        LLVMBasicBlockRef previousBlock = currentBlock;
        currentBlock = entry;
        currentFunction = function;

        currentScope = new llvmSymbolTable(currentScope);

        if (ctx.funcFParams() != null) {
            int index = 0;
            for (SysYParser.FuncFParamContext paramContext : ctx.funcFParams().funcFParam()) {
                String paramName = paramContext.IDENT().getText();
                LLVMValueRef param = LLVM.LLVMBuildAlloca(builder, i32Type, paramName);
                LLVM.LLVMBuildStore(builder, LLVMGetParam(LLVMGetNamedFunction(module,functionName),index++), param);
                currentScope.define(paramName, param);
            }
        }

        visit(ctx.block());

        //处理void函数不包含return语句的情况
        currentBlock = LLVM.LLVMGetInsertBlock(builder);
        LLVMValueRef lastInst = LLVM.LLVMGetLastInstruction(currentBlock);
        if (lastInst == null || LLVM.LLVMGetInstructionOpcode(lastInst) != LLVM.LLVMRet) {
            LLVM.LLVMBuildRetVoid(builder);
        }

        currentBlock = previousBlock;
        currentScope = currentScope.getParent();

        return function;
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        for (SysYParser.BlockItemContext blockItemContext : ctx.blockItem()) {
            visit(blockItemContext);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx) {
        if (ctx.RETURN() != null) {
            LLVMValueRef returnValue = (ctx.exp() != null) ? visit(ctx.exp()) : null;
            LLVMBuildRet(builder, returnValue);
        } else if (ctx.lVal() != null) {
            String varName = ctx.lVal().IDENT().getText();
            LLVMValueRef var = currentScope.resolve(varName);
            LLVMValueRef value = visit(ctx.exp());
            LLVMBuildStore(builder, value, var);
        } else if (ctx.block() != null) {
            currentScope = new llvmSymbolTable(currentScope);
            visit(ctx.block());
            currentScope = currentScope.getParent();
        } else if (ctx.exp() != null) {
            visit(ctx.exp());
        } else if(ctx.IF() != null){
            LLVMBasicBlockRef ifTrueBlock = LLVMAppendBasicBlock(currentFunction, "if_true");
            LLVMBasicBlockRef ifElseBlock = LLVMAppendBasicBlock(currentFunction, "if_else");
            LLVMBasicBlockRef ifNextBlock = LLVMAppendBasicBlock(currentFunction, "if_next");

            LLVMValueRef condValue = visit(ctx.cond());
            condValue = LLVMBuildICmp(builder,LLVMIntNE,zeroValue,condValue,"icmp_ne");
            LLVMBuildCondBr(builder, condValue, ifTrueBlock, ifElseBlock);

            LLVMPositionBuilderAtEnd(builder, ifTrueBlock);
            visit(ctx.stmt(0));
            LLVMBuildBr(builder, ifNextBlock);
            if (LLVMIsATerminatorInst(LLVMGetLastInstruction(ifTrueBlock)) == null) {
                LLVMPositionBuilderAtEnd(builder, ifTrueBlock);
                LLVMBuildBr(builder, ifNextBlock);
            }

            if (ctx.ELSE() != null) {
                LLVMPositionBuilderAtEnd(builder, ifElseBlock);
                visit(ctx.stmt(1));
                LLVMBuildBr(builder, ifNextBlock);
                if (LLVMIsATerminatorInst(LLVMGetLastInstruction(ifElseBlock)) == null) {
                    LLVMPositionBuilderAtEnd(builder, ifElseBlock);
                    LLVMBuildBr(builder, ifNextBlock);
                }
            } else {
                LLVMPositionBuilderAtEnd(builder, ifElseBlock);
                LLVMBuildBr(builder, ifNextBlock);
            }

            LLVMPositionBuilderAtEnd(builder, ifNextBlock);
        } else if(ctx.WHILE() != null){
            LLVMBasicBlockRef whileConditionBlock = LLVMAppendBasicBlock(currentFunction, "while_condition");
            LLVMBasicBlockRef whileBodyBlock = LLVMAppendBasicBlock(currentFunction, "while_body");
            LLVMBasicBlockRef whileEndBlock = LLVMAppendBasicBlock(currentFunction, "while_end");

            LLVMBuildBr(builder, whileConditionBlock);

            LLVMPositionBuilderAtEnd(builder, whileConditionBlock);
            LLVMValueRef condValue = visit(ctx.cond());
            condValue = LLVMBuildICmp(builder,LLVMIntNE,zeroValue,condValue,"icmp_ne");
            LLVMBuildCondBr(builder, condValue, whileBodyBlock, whileEndBlock);

            loopEndStack.push(whileEndBlock);
            loopBeginStack.push(whileConditionBlock);
            LLVMPositionBuilderAtEnd(builder, whileBodyBlock);
            visit(ctx.stmt(0));
            if (LLVMIsATerminatorInst(LLVMGetLastInstruction(whileBodyBlock)) == null) {
                LLVMPositionBuilderAtEnd(builder, whileBodyBlock);
                LLVMBuildBr(builder, whileConditionBlock);
                //loopEndStack.peek();
            }
            loopEndStack.pop();
            loopBeginStack.pop();
            LLVMBuildBr(builder, whileConditionBlock);

            LLVMPositionBuilderAtEnd(builder, whileEndBlock);
        } else if(ctx.BREAK() != null){
            LLVMBuildBr(builder, loopEndStack.peek());
        } else if(ctx.CONTINUE() != null){
            LLVMBuildBr(builder,loopBeginStack.peek());
        }
        return null;
    }

    @Override
    public LLVMValueRef visitCond(SysYParser.CondContext ctx) {
        if (ctx.exp() != null) {
//            LLVMValueRef zeroValue = LLVM.LLVMConstInt(LLVM.LLVMInt1Type(), 0, 0);
//            LLVMValueRef condValue = visit(ctx.exp());
//            return LLVMBuildICmp(builder,LLVMIntNE,condValue,zeroValue,"");
            return LLVMBuildZExt(builder, visit(ctx.exp()), i32Type, "");
        } else if (ctx.AND() != null) {
            // 如果条件是一个AND逻辑运算符连接的多个表达式，需要进行短路求值
            LLVMValueRef leftValue = visitCond(ctx.cond(0));
            LLVMBasicBlockRef currentBlock = LLVM.LLVMGetInsertBlock(builder);
            LLVMBasicBlockRef thenBlock = LLVM.LLVMAppendBasicBlock(currentFunction, "and_then");
            LLVMBasicBlockRef elseBlock = LLVM.LLVMAppendBasicBlock(currentFunction, "and_else");
            LLVMBasicBlockRef mergeBlock = LLVM.LLVMAppendBasicBlock(currentFunction, "and_merge");

            //LLVMValueRef zeroValue = LLVM.LLVMConstInt(LLVM.LLVMInt1Type(), 0, 0);

            // 判断左侧条件是否为假，如果是，直接跳转到合并块
            LLVMValueRef cmpInst = LLVMBuildICmp(builder, LLVMIntNE, zeroValue, leftValue,"and_cmp");
            LLVMBuildCondBr(builder, cmpInst, thenBlock, elseBlock);

            // 生成 then 基本块
            LLVMPositionBuilderAtEnd(builder, thenBlock);
            LLVMValueRef rightValue = visitCond(ctx.cond(1));
            //rightValue = LLVMBuildICmp(builder,LLVMIntNE,zeroValue,rightValue,"icmp_ne");
            LLVMBuildBr(builder, mergeBlock);

            // 生成 else 基本块
            LLVMPositionBuilderAtEnd(builder, elseBlock);
            LLVMBuildBr(builder, mergeBlock);

            // 生成 merge 基本块
            LLVMPositionBuilderAtEnd(builder, mergeBlock);
            LLVMBasicBlockRef phiBlock = LLVM.LLVMGetInsertBlock(builder);

            LLVMValueRef phiNode = LLVMBuildPhi(builder, LLVM.LLVMInt32Type(), "and_phi");
            LLVMValueRef[] phiIncomingValues = {rightValue, zeroValue};
            LLVMBasicBlockRef[] phiIncomingBlocks = {thenBlock, elseBlock};
            LLVMAddIncoming(phiNode, new PointerPointer<>(phiIncomingValues), new PointerPointer<>(phiIncomingBlocks), 2);

            return phiNode;
        } else if (ctx.OR() != null) {
            // 如果条件是一个OR逻辑运算符连接的多个表达式，需要进行短路求值
            LLVMValueRef leftValue = visitCond(ctx.cond(0));
            LLVMBasicBlockRef currentBlock = LLVM.LLVMGetInsertBlock(builder);
            LLVMBasicBlockRef thenBlock = LLVM.LLVMAppendBasicBlock(currentFunction, "or_then");
            LLVMBasicBlockRef elseBlock = LLVM.LLVMAppendBasicBlock(currentFunction, "or_else");
            LLVMBasicBlockRef mergeBlock = LLVM.LLVMAppendBasicBlock(currentFunction, "or_merge");

            LLVMValueRef oneValue = LLVM.LLVMConstInt(LLVM.LLVMInt32Type(), 1, 0);

            // 判断左侧条件是否为真，如果是，直接跳转到合并块
            LLVMValueRef cmpInst = LLVMBuildICmp(builder, LLVMIntNE, zeroValue, leftValue,"or_cmp");
            LLVMBuildCondBr(builder, cmpInst, thenBlock, elseBlock);

            // 生成 then 基本块
            LLVMPositionBuilderAtEnd(builder, thenBlock);
            LLVMBuildBr(builder, mergeBlock);

            // 生成 else 基本块
            LLVMPositionBuilderAtEnd(builder, elseBlock);
            LLVMValueRef rightValue = visitCond(ctx.cond(1));
            //rightValue = LLVMBuildICmp(builder,LLVMIntNE,zeroValue,rightValue,"icmp_ne");
            LLVMBuildBr(builder, mergeBlock);

            // 生成 merge 基本块
            LLVMPositionBuilderAtEnd(builder, mergeBlock);
            LLVMBasicBlockRef phiBlock = LLVM.LLVMGetInsertBlock(builder);

            LLVMValueRef phiNode = LLVMBuildPhi(builder, LLVM.LLVMInt32Type(), "or_phi");
            LLVMValueRef[] phiIncomingValues = {oneValue, rightValue};
            LLVMBasicBlockRef[] phiIncomingBlocks = {thenBlock, elseBlock};
            LLVMAddIncoming(phiNode, new PointerPointer<>(phiIncomingValues), new PointerPointer<>(phiIncomingBlocks), 2);

            return phiNode;
        } else {
            LLVMValueRef left = visit(ctx.cond(0));
            LLVMValueRef right = visit(ctx.cond(1));
            String operator = ctx.getChild(1).getText();
            switch (operator) {
                case "<":
                    return LLVMBuildZExt(builder,LLVMBuildICmp(builder, LLVMIntSLT, left, right, "icmp_lt"),i32Type,"icmp_lt");
                case ">":
                    return LLVMBuildZExt(builder,LLVMBuildICmp(builder, LLVMIntSGT, left, right, "icmp_gt"),i32Type,"icmp_gt");
                case "<=":
                    return LLVMBuildZExt(builder,LLVMBuildICmp(builder, LLVMIntSLE, left, right, "icmp_le"),i32Type,"icmp_le");
                case ">=":
                    return LLVMBuildZExt(builder,LLVMBuildICmp(builder, LLVMIntSGE, left, right, "icmp_ge"),i32Type,"icmp_ge");
                case "==":
                    return LLVMBuildZExt(builder,LLVMBuildICmp(builder, LLVMIntEQ, left, right, "icmp_eq"),i32Type,"icmp_eq");
                case "!=":
                    return LLVMBuildZExt(builder,LLVMBuildICmp(builder, LLVMIntNE, left, right, "icmp_ne"),i32Type,"icmp_ne");
                default:
                    break;
            }
            return null;
        }
    }

    @Override
    public LLVMValueRef visitConstdecl(SysYParser.ConstdeclContext ctx) {
        for (SysYParser.ConstdefContext  constDefContext : ctx.constdef()) {
            visit(constDefContext);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitConstdef(SysYParser.ConstdefContext ctx) {
        String varName = ctx.IDENT().getText();
        LLVMValueRef var;
        LLVMValueRef value = visit(ctx.constInitVal());
        if (currentBlock == null) {
            // 全局常量
            var = LLVM.LLVMAddGlobal(module, i32Type, varName);
            LLVM.LLVMSetInitializer(var, value);
        } else {
            // 局部常量
            var = LLVM.LLVMBuildAlloca(builder, i32Type, varName);
            LLVM.LLVMBuildStore(builder, value, var);
        }

        currentScope.define(varName, var);
        return var;
    }

    @Override
    public LLVMValueRef visitVardecl(SysYParser.VardeclContext ctx) {
        for (SysYParser.VardefContext varDefContext : ctx.vardef()) {
            visit(varDefContext);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitVardef(SysYParser.VardefContext ctx) {
        String varName = ctx.IDENT().getText();
        LLVMValueRef var;
        if (currentBlock == null) {
            // 全局变量
            var = LLVM.LLVMAddGlobal(module, i32Type, varName);
            if (ctx.ASSIGN() != null) {
                LLVMValueRef value = visit(ctx.initVal());
                LLVM.LLVMSetInitializer(var, value);
            } else {
                LLVM.LLVMSetInitializer(var, LLVM.LLVMConstInt(i32Type, 0, 0));
            }
        } else {
            // 局部变量
            var = LLVM.LLVMBuildAlloca(builder, i32Type, varName);
            if (ctx.ASSIGN() != null) {
                LLVMValueRef value = visit(ctx.initVal());
                LLVM.LLVMBuildStore(builder, value, var);
            }
        }

        currentScope.define(varName, var);
        return var;
    }

    @Override
    public LLVMValueRef visitInitVal(SysYParser.InitValContext ctx) {
        return visit(ctx.exp());
    }

    @Override
    public LLVMValueRef visitConstInitVal(SysYParser.ConstInitValContext ctx) {
        return visit(ctx.constExp());
    }

    @Override
    public LLVMValueRef visitConstExp(SysYParser.ConstExpContext ctx) {
        return visitExp(ctx.exp());
    }

    @Override
    public LLVMValueRef visitExp(SysYParser.ExpContext ctx) {
        if (ctx.L_PAREN() != null && ctx.IDENT() == null) {
            return visit(ctx.exp(0));
        } else if (ctx.lVal() != null) {
            return visit(ctx.lVal());
        } else if (ctx.number() != null) {
            return visit(ctx.number());
        } else if(ctx.IDENT() != null){
            String functionName = ctx.IDENT().getText();
            LLVMValueRef function = GlobalScope.resolve(functionName);
            // 处理函数参数
            List<LLVMValueRef> args = new ArrayList<>();
            if (ctx.funcRParams() != null) {
                for (SysYParser.ParamContext paramContext : ctx.funcRParams().param()) {
                    args.add(visit(paramContext.exp()));
                }
            }
            // 生成函数调用指令
            if(LLVMGetTypeKind(LLVMGetReturnType(LLVMGetElementType(LLVMTypeOf(function)))) == LLVMVoidTypeKind){
                return LLVM.LLVMBuildCall(builder, function, new PointerPointer<>(args.toArray(new LLVMValueRef[0])), args.size(), "");
            }else {
                return LLVM.LLVMBuildCall(builder, function, new PointerPointer<>(args.toArray(new LLVMValueRef[0])), args.size(), "returnValue");
            }

        } else if (ctx.IDENT() != null) {
            // Handle function call
        } else if (ctx.unaryOp() != null) {
            LLVMValueRef operand = visit(ctx.exp(0));
            switch (ctx.unaryOp().getText()) {
                case "-":
                    return LLVM.LLVMBuildNeg(builder, operand, "negtmp");
                case "!":
                    LLVMValueRef zero = LLVM.LLVMConstInt(i32Type, 0, 0);
                    return LLVMBuildZExt(builder,LLVMBuildICmp(builder, LLVM.LLVMIntEQ, operand, zero, "cmptmp"),i32Type,"nottmp");
                case "+":
                default:
                    return operand;
            }
        } else if (ctx.MUL() != null || ctx.DIV() != null || ctx.MOD() != null) {
            LLVMValueRef left = visit(ctx.exp(0));
            LLVMValueRef right = visit(ctx.exp(1));
            if (ctx.MUL() != null) {
                return LLVM.LLVMBuildMul(builder, left, right, "multmp");
            } else if (ctx.DIV() != null) {
                return LLVM.LLVMBuildSDiv(builder, left, right, "divtmp");
            } else {
                return LLVM.LLVMBuildSRem(builder, left, right, "modtmp");
            }
        } else if (ctx.PLUS() != null || ctx.MINUS() != null) {
            LLVMValueRef left = visit(ctx.exp(0));
            LLVMValueRef right = visit(ctx.exp(1));
            if (ctx.PLUS() != null) {
                return LLVM.LLVMBuildAdd(builder, left, right, "addtmp");
            } else {
                return LLVM.LLVMBuildSub(builder, left, right, "subtmp");
            }
        }
        return null;
    }

    @Override
    public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        String varName = ctx.IDENT().getText();
        LLVMValueRef var = currentScope.resolve(varName);
        if (ctx.L_BRACKT().size() > 0) {
            // Handle array access
        }
        return LLVM.LLVMBuildLoad(builder, var, varName);
    }

    @Override
    public LLVMValueRef visitNumber(SysYParser.NumberContext ctx) {
        int num;
        String numString = ctx.INTEGER_CONST().getText();
        if(numString.startsWith("0x") || numString.startsWith("0X")){
            num = Integer.parseInt(numString.substring(2),16);
        }else if(numString.startsWith("0") && numString.length() > 1){
            num = Integer.parseInt(numString.substring(1),8);
        }else {
            num = Integer.parseInt(numString);
        }
        return LLVM.LLVMConstInt(i32Type, num, 0);
    }
}
