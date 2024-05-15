import org.antlr.v4.runtime.tree.ParseTree;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;

import java.util.HashMap;
import java.util.Map;


public class MyVisit extends SysYParserBaseVisitor<LLVMValueRef> {

    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");

    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();

    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();

    LLVMValueRef zero = LLVMConstInt(i32Type, 0, /* signExtend */ 0);
    //llvmScope curScope;

    Map<String,Integer>map = new HashMap<>();

    public MyVisit() {
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();


    }




    @Override
    public LLVMValueRef visitConstdecl(SysYParser.ConstdeclContext ctx) {
        for (int i = 0; i < ctx.constdef().size(); i++) {
            visit(ctx.constdef(i)); // 依次visit def，即依次visit c=4 和 d=5
        }
        return null;
    }

    @Override
    public LLVMValueRef visitVardecl(SysYParser.VardeclContext ctx) {
        for (int i = 0; i < ctx.vardef().size(); i++) {
            visit(ctx.vardef(i)); // 依次visit def，即依次visit c=4 和 d=5
        }
        return null;
    }

    @Override
    public LLVMValueRef visitVardef(SysYParser.VardefContext ctx) {
        //System.out.println(isGlobalVar(ctx));
        if (isGlobalVar(ctx)) {
            if(ctx.ASSIGN() == null){
                //创建名为globalVar的全局变量
                LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type, /*globalVarName:String*/ctx.IDENT().getText());
                //为全局变量设置初始化器
                LLVMSetInitializer(globalVar, /* constantVal:LLVMValueRef*/zero);
                map.put(ctx.IDENT().getText(),0);
            }
            else {
                LLVMValueRef tmp = LLVMConstInt(i32Type, getExpValue(ctx.initVal().exp()), /* signExtend */ 0);
                LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type, /*globalVarName:String*/ctx.IDENT().getText());
                LLVMSetInitializer(globalVar, /* constantVal:LLVMValueRef*/tmp);
                map.put(ctx.IDENT().getText(),getExpValue(ctx.initVal().exp()));
            }
        } else {
            if(ctx.ASSIGN() != null){
                //int型变量
                //申请一块能存放int型的内存
                LLVMValueRef pointer = LLVMBuildAlloca(builder, i32Type, /*pointerName:String*/ctx.IDENT().getText());
                LLVMValueRef tmp = LLVMConstInt(i32Type, getExpValue(ctx.initVal().exp()), /* signExtend */ 0);
                //将数值存入该内存
                LLVMBuildStore(builder,tmp , pointer);
                //从内存中将值取出
                LLVMValueRef value = LLVMBuildLoad(builder, pointer, /*varName:String*/ctx.IDENT().getText() + "1");
                map.put(ctx.IDENT().getText(),getExpValue(ctx.initVal().exp()));
            }
            else {
                LLVMValueRef pointer = LLVMBuildAlloca(builder, i32Type, /*pointerName:String*/ctx.IDENT().getText());
                LLVMBuildStore(builder,zero , pointer);
                LLVMValueRef value = LLVMBuildLoad(builder, pointer, /*varName:String*/ctx.IDENT().getText() + "1");
                map.put(ctx.IDENT().getText(),0);
            }
        }
        return null;
    }

    @Override
    public LLVMValueRef visitConstdef(SysYParser.ConstdefContext ctx) {
        if (isGlobalVar(ctx)) {
            LLVMValueRef tmp = LLVMConstInt(i32Type, getExpValue(ctx.constInitVal().constExp().exp()), /* signExtend */ 0);
            LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type, /*globalVarName:String*/ctx.IDENT().getText());
            LLVMSetInitializer(globalVar, /* constantVal:LLVMValueRef*/tmp);
            map.put(ctx.IDENT().getText(),getExpValue(ctx.constInitVal().constExp().exp()));
        } else {
            LLVMValueRef pointer = LLVMBuildAlloca(builder, i32Type, /*pointerName:String*/ctx.IDENT().getText());
            LLVMValueRef tmp = LLVMConstInt(i32Type, getExpValue(ctx.constInitVal().constExp().exp()), /* signExtend */ 0);
            LLVMBuildStore(builder,tmp , pointer);
            LLVMValueRef value = LLVMBuildLoad(builder, pointer, /*varName:String*/ctx.IDENT().getText() + "1");
            map.put(ctx.IDENT().getText(),getExpValue(ctx.constInitVal().constExp().exp()));
        }
        //System.out.println(isGlobalVar(ctx));
        return null;
    }

    private boolean isGlobalVar(SysYParser.VardefContext ctx) {
        ParseTree parent = ctx.getParent();
        while (parent != null) {
            if (parent instanceof SysYParser.FuncDefContext) {
                return false;
            }
            parent = parent.getParent();
        }
        return true;
    }

    private boolean isGlobalVar(SysYParser.ConstdefContext ctx) {
        ParseTree parent = ctx.getParent();
        while (parent != null) {
            if (parent instanceof SysYParser.FuncDefContext) {
                return false;
            }
            parent = parent.getParent();
        }
        return true;
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        LLVMTypeRef returnType = i32Type;
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);
        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, 0, 0);
        LLVMValueRef function = LLVMAddFunction(module, ctx.IDENT().getText(), ft);
        LLVMBasicBlockRef block1 = LLVMAppendBasicBlock(function, ctx.IDENT().getText() + "Entry");
        LLVMPositionBuilderAtEnd(builder, block1);

        super.visitChildren(ctx);
        return null;
    }

    @Override
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx) {
        if (ctx.RETURN() != null) {
            if (ctx.exp() != null) {
                int retValue = getExpValue(ctx.exp());
                LLVMValueRef result = LLVMConstInt(i32Type, retValue, 0);
                LLVMBuildRet(builder, result);
            }
        }
        else if(ctx.lVal() != null){
            map.replace(ctx.lVal().IDENT().getText(), getExpValue(ctx.exp()));
        }
        else if(ctx.block() != null){
            ctx.block().blockItem().forEach(this::visit); // 依次visit block中的节点
        }
        return null;
    }

    public int getExpValue(SysYParser.ExpContext ctx) {
        if(ctx == null){
            return 0;
        }
        if (ctx.L_PAREN() != null) {
            return getExpValue(ctx.exp(0));
        } else if (ctx.number() != null) {
            return Integer.parseInt(toInt(ctx.number().getText()));
        } else if (ctx.unaryOp() != null) {
            if (ctx.unaryOp().getText().equals("+")) {
                return getExpValue(ctx.exp(0));
            } else if (ctx.unaryOp().getText().equals("-")) {
                return -getExpValue(ctx.exp(0));
            } else if (ctx.unaryOp().getText().equals("!")) {
                return getExpValue(ctx.exp(0)) == 0 ? 1 : 0;
            }
        }else if(ctx.lVal() != null){
            return map.get(ctx.lVal().IDENT().getText());
        }
        else {
            if (ctx.DIV() != null) {
                return getExpValue(ctx.exp(0)) / getExpValue(ctx.exp(1));
            } else if (ctx.MUL() != null) {
                return getExpValue(ctx.exp(0)) * getExpValue(ctx.exp(1));
            } else if (ctx.MOD() != null) {
                return getExpValue(ctx.exp(0)) % getExpValue(ctx.exp(1));
            } else if (ctx.MINUS() != null) {
                return getExpValue(ctx.exp(0)) - getExpValue(ctx.exp(1));
            } else if (ctx.PLUS() != null) {
                return getExpValue(ctx.exp(0)) + getExpValue(ctx.exp(1));
            }
        }//reduce false
        return 0;
    }

    public String toInt(String ret) {
        if (ret.charAt(0) == '0' && ret.length() > 1 &&
                ret.charAt(1) != 'x' && ret.charAt(1) != 'X') {// 8
            return String.valueOf(Integer.parseInt(ret.substring(1), 8));
        } else if (ret.length() > 2 &&
                (ret.substring(0, 2).equals("0x") || ret.substring(0, 2).equals("0X"))) {// 16
            return String.valueOf(Integer.parseInt(ret.substring(2), 16));
        } else {
            return ret;
        }
    }

}