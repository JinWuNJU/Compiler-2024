import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class MyVisit extends SysYParserBaseVisitor {

    Scope globalScope = null;
    Scope curScope = null;

    Type curRetTpye = null;


    @Override
    public Object visitProgram(SysYParser.ProgramContext ctx) {
        globalScope = new GlobalScope(null);
        curScope = globalScope;

        return super.visitProgram(ctx);
    }

    @Override
    public Void visitFuncDef(SysYParser.FuncDefContext ctx) {
        String funcName = ctx.IDENT().getText();
        Type retType;
        if (curScope.find(funcName)) { // curScope为当前的作用域
            OutputHelper.printSemanticError(ErrorType.Redefinde_function, ctx.IDENT().getSymbol().getLine(),
                    ctx.IDENT().getText());
            return null;

        }
        String type_Str = ctx.getChild(0).getText();
        if ("int".equals(type_Str)) {
            retType = IntType.getInt32();
        }
        else {
            retType = VoidType.getVoidType();
        }

        List<Type> fParams_Type = visitFuncFParams(ctx.funcFParams());//null返回空list
        curScope.getSymbols().put(funcName, new FunctionType(retType, fParams_Type));
        curRetTpye = retType;
        if(fParams_Type.isEmpty()){//空参
            visitBlock(ctx.block());
        }
        else {//将新的参数加入新的scope
            visitBlock(ctx.block(),makeFParams(ctx.funcFParams()));
        }

        return null;
    }


    public Object visitBlock(SysYParser.BlockContext ctx, Map<String,Type>fParams) {
        LocalScope localScope = new LocalScope(curScope);
        curScope = localScope;
        curScope.getSymbols().putAll(fParams);
        ctx.blockItem().forEach(this::visit); // 依次visit block中的节点
        //切换回父级作用域
        curScope = curScope.getEnclosingScope();
        return null;
    }


    public Object visitBlock(SysYParser.BlockContext ctx) {
        LocalScope localScope = new LocalScope(curScope);
        curScope = localScope;
        ctx.blockItem().forEach(this::visit); // 依次visit block中的节点
        //切换回父级作用域
        curScope = curScope.getEnclosingScope();

        return null;
    }

    @Override
    public Object visitConstdecl(SysYParser.ConstdeclContext ctx) {
        for (int i = 0; i < ctx.constdef().size(); i ++) {
            visit(ctx.constdef(i)); // 依次visit def，即依次visit c=4 和 d=5
        }
        return null;
    }

    @Override
    public Object visitVardecl(SysYParser.VardeclContext ctx) {
        for (int i = 0; i < ctx.vardef().size(); i ++) {
            visit(ctx.vardef(i)); // 依次visit def，即依次visit c=4 和 d=5
        }
        return null;
    }


    private Map<String,Type>makeFParams(SysYParser.FuncFParamsContext ctx){
        Map<String,Type>map = new HashMap<>();
        List<SysYParser.FuncFParamContext> funcFParams = ctx.funcFParam();
        for (SysYParser.FuncFParamContext param : funcFParams) {
            if(curScope.find(param.getText())){//是否在本作用域中定义过变量
                OutputHelper.printSemanticError(ErrorType.Redefined_variable,param.IDENT().getSymbol().getLine(),
                        param.IDENT().getText());
                return map;
            }
            if(param != null && !param.L_BRACKT().isEmpty()){//默认1维数组为1(函数形参)
                map.put(param.IDENT().getText(),new ArrayType(1));
            }
            else {
                map.put(param.IDENT().getText(),IntType.getInt32());
            }
        }
        return map;
    }
    @Override
    public List<Type> visitFuncFParams(SysYParser.FuncFParamsContext ctx) {
        List<Type> list = new ArrayList<>();
        if(ctx == null){
            return list;
        }
        List<SysYParser.FuncFParamContext> funcFParams = ctx.funcFParam();
        if (funcFParams != null) {
            list = funcFParams.stream().map(param ->
            {
                if(param != null && !param.L_BRACKT().isEmpty()){//默认1维数组为空list
                    return new ArrayType(1);
                }
                else {
                    return IntType.getInt32();
                }
            }).collect(Collectors.toList());
        }
        return list;
    }



    @Override
    public Object visitConstInitVal(SysYParser.ConstInitValContext ctx) {
        if(ctx.constInitVal().isEmpty()){
            return visitConstExp(ctx.constExp());
        }
        else {
            for (SysYParser.ConstInitValContext initVal : ctx.constInitVal()){
                visitConstInitVal(initVal);
            }
        }
        return null;
    }

    @Override
    public Object visitInitVal(SysYParser.InitValContext ctx) {
        if(ctx.initVal().isEmpty()){
            return visitExp(ctx.exp());
        }
        else {
            for (SysYParser.InitValContext initVal : ctx.initVal()){
                visitInitVal(initVal);
            }
        }
        return null;
    }


    @Override
    public Object visitConstdef(SysYParser.ConstdefContext ctx) {
        if(curScope.find(ctx.IDENT().getText())){
            OutputHelper.printSemanticError(ErrorType.Redefined_variable,ctx.IDENT().getSymbol().getLine()
                    ,ctx.IDENT().getText());
            return null;
        }
        String varName = ctx.IDENT().getText(); // 获取变量名
        List<SysYParser.ConstExpContext> dimensions = ctx.constExp(); // 获取维度信息
        if (dimensions.isEmpty()) {//Int
            if (ctx.ASSIGN() != null) {
                if(visitConstInitVal(ctx.constInitVal()) != null){
                    Type tmp = (Type) visitConstInitVal(ctx.constInitVal());
                    if(tmp != null && !(tmp instanceof IntType)){
                        OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_assignment,ctx.IDENT().getSymbol().getLine(),
                                ctx.IDENT().getText());
                        return null;
                    }//不确定是否在null时候也要return
                }
            }
            curScope.getSymbols().put(varName, IntType.getInt32());
        }
        else {//数组
            if (ctx.ASSIGN() != null) {
                if(visitConstInitVal(ctx.constInitVal()) != null){
                    Type tmp = (Type) visitConstInitVal(ctx.constInitVal());
                    if(tmp != null  && !(tmp instanceof ArrayType)){
                        OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_assignment,ctx.IDENT().getSymbol().getLine(),
                                ctx.IDENT().getText());
                        return null;
                    }//不确定是否在null时候也要return
                }
            }
            //List<String> list = dimensions.stream().map(text -> text.getText()).collect(Collectors.toList());
            curScope.getSymbols().put(varName, new ArrayType(dimensions.size()));
        }
        return null;
    }

    @Override
    public Object visitVardef(SysYParser.VardefContext ctx) {
        if(curScope.find(ctx.IDENT().getText())){
            OutputHelper.printSemanticError(ErrorType.Redefined_variable,ctx.IDENT().getSymbol().getLine()
                    ,ctx.IDENT().getText());
            return null;
        }
        String varName = ctx.IDENT().getText(); // 获取变量名
        List<SysYParser.ConstExpContext> dimensions = ctx.constExp(); // 获取维度信息
        if (dimensions.isEmpty()) {//Int
            if (ctx.ASSIGN() != null) {
                if(visitInitVal(ctx.initVal()) != null){
                    Type tmp = (Type) visitInitVal(ctx.initVal());
                    if(!(tmp instanceof IntType)){
                        OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_assignment,ctx.IDENT().getSymbol().getLine(),
                                ctx.IDENT().getText());
                        return null;
                    }//不确定是否在null时候也要return
                }
            }
            curScope.getSymbols().put(varName, IntType.getInt32());
        }
        else {//数组
            if (ctx.ASSIGN() != null) {
                if(visitInitVal(ctx.initVal()) != null){
                    Type tmp = (Type) visitInitVal(ctx.initVal());
                    if(!(tmp instanceof ArrayType)){
                        OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_assignment,ctx.IDENT().getSymbol().getLine(),
                                ctx.IDENT().getText());
                        return null;
                    }//不确定是否在null时候也要return
                }
            }
            //List<String> list = dimensions.stream().map(text -> text.getText()).collect(Collectors.toList());
            curScope.getSymbols().put(varName, new ArrayType(dimensions.size()));
        }
        return null;
    }



    @Override
    public Object visitCond(SysYParser.CondContext ctx) {
        if(ctx.exp() != null){
            Type tmp = (Type) visitExp(ctx.exp());
            if(tmp != null && !(tmp instanceof IntType)){
                OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_operands,ctx.exp().getStart().getLine(),
                        ctx.exp().getText());
                return new Object();
            }
            if(tmp == null){
                return new Object();
            }
            else if(tmp instanceof IntType){
                return IntType.getInt32();
            }
        }
        else {
            List<SysYParser.CondContext> cond = ctx.cond();
            for (int i = 0; i < cond.size(); i++) {
                if(visitCond(cond.get(i)) == null){
                    OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_operands,cond.get(i).getStart().getLine(),
                            cond.get(i).getStart().getText());
                    return new Object();
                }
            }
        }
        return null;
    }

    @Override
    public Object visitConstExp(SysYParser.ConstExpContext ctx) {
        return visitExp(ctx.exp());
    }
    @Override
    public Object visitExp(SysYParser.ExpContext ctx) {
        if(ctx == null){
            return null;
        }
        else if(ctx.number() != null){
            return IntType.getInt32();
        }
        else if(ctx.lVal() != null){//变量引用
            return visitLVal(ctx.lVal());
        }
        else if(ctx.IDENT() != null){//CallFunc
            if(curScope.resolve(ctx.IDENT().getText()) == null){
                OutputHelper.printSemanticError(ErrorType.Undefined_function, ctx.IDENT().getSymbol().getLine(),
                        ctx.IDENT().getText());
                return null;
            }
            else if(!(curScope.resolve(ctx.IDENT().getText()) instanceof FunctionType)){
                OutputHelper.printSemanticError(ErrorType.Not_a_function, ctx.IDENT().getSymbol().getLine(),
                        ctx.IDENT().getText());
                return null;
            }
            else {
                if(ctx.L_PAREN() == null){
                    OutputHelper.printSemanticError(ErrorType.Function_is_not_applicable_for_arguments,ctx.IDENT().getSymbol().getLine(),
                            ctx.IDENT().getText());
                    return null;
                }
                List<Type> fparams = ((FunctionType)curScope.resolve(ctx.IDENT().getText())).paramsType;
                if(ctx.funcRParams() == null){
                    if(fparams == null || fparams.isEmpty()){
                        return ((FunctionType)curScope.resolve(ctx.IDENT().getText())).retTy;
                    }
                    else {
                        OutputHelper.printSemanticError(ErrorType.Function_is_not_applicable_for_arguments,ctx.IDENT().getSymbol().getLine(),
                                ctx.IDENT().getText());
                        return null;
                    }
                }else {//注意比对参数
                    List<SysYParser.ParamContext> rparams = ctx.funcRParams().param();
                    if(rparams.size() != fparams.size()){
                        OutputHelper.printSemanticError(ErrorType.Function_is_not_applicable_for_arguments,ctx.IDENT().getSymbol().getLine(),
                                ctx.IDENT().getText());
                        return null;
                    }
                    for (int i = 0; i < rparams.size(); i++) {
                        if(visitExp(rparams.get(i).exp()) != null){
                            Type tmpTy = (Type) visitExp(rparams.get(i).exp());
                            if(tmpTy.getClass() != fparams.get(i).getClass()){
                                OutputHelper.printSemanticError(ErrorType.Function_is_not_applicable_for_arguments,ctx.funcRParams().param(i).getStart().getLine(),
                                        ctx.funcRParams().param(i).getStart().getText());
                                return null;
                            };
                        }
                    }
                    //列表对比完成

                }
            }
        }
        else if(ctx.exp() != null){
            if(ctx.R_PAREN() != null){//   (  exp  ) 列表只有一个exp
                return visitExp(ctx.exp().get(0));
            }
            else {
                List<SysYParser.ExpContext> exps = ctx.exp();
                for (int i =0 ; i<exps.size();i++){
                    if(!checkIsInteger(exps.get(i))){
                        OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_operands,ctx.exp(i).getStart().getLine(),
                                ctx.exp(i).getStart().getText());
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private boolean checkIsInteger(SysYParser.ExpContext ctx){
        if(ctx == null){
            return false;
        }
        return visitExp(ctx) instanceof IntType;
    }


    @Override
    public Object visitStmt(SysYParser.StmtContext ctx) {
        if(ctx == null){
            return null;
        }
        if(!ctx.stmt().isEmpty()){
            for (SysYParser.StmtContext stmtContext : ctx.stmt()){
                visitStmt(stmtContext);
            }
        }
        if(ctx.ASSIGN() != null){
            Type tp1 = (Type) visitLVal(ctx.lVal());
            Type tp2 = (Type) visitExp(ctx.exp());
            if (tp1 != null && tp2 != null && (tp1.getClass() != tp2.getClass())){
                OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_assignment,ctx.ASSIGN().getSymbol().getLine(),
                        ctx.ASSIGN().getText());
                return null;
            }
            if(tp2 instanceof ArrayType && tp1 instanceof ArrayType){
                ArrayType temp1 = (ArrayType) tp1;
                ArrayType temp2 = (ArrayType) tp2;
                if(temp1.getDimension() != temp2.getDimension()){
                    OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_assignment,ctx.ASSIGN().getSymbol().getLine(),
                            ctx.ASSIGN().getText());
                    return null;
                }
            }
        }
        else if(ctx.RETURN() != null){
            if(ctx.exp() == null){
                if(!(curRetTpye instanceof VoidType)){
                    OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_return,ctx.RETURN().getSymbol().getLine(),
                            ctx.RETURN().getText());
                    return null;
                }
            }
            else {
                Type tmp = (Type) visitExp(ctx.exp());
                if (tmp != null && tmp.getClass() != curRetTpye.getClass()){
                    OutputHelper.printSemanticError(ErrorType.Type_mismatched_for_return,ctx.RETURN().getSymbol().getLine(),
                            ctx.RETURN().getText());
                    return null;
                }
            }
        }
        else if(ctx.exp() != null){
            return visitExp(ctx.exp());
        }
        else if(ctx.block() != null){
            return visitBlock(ctx.block());
        }
        else if(ctx.cond() != null){
            return visitCond(ctx.cond());
        }

        return null;
    }

    @Override
    public Object visitLVal(SysYParser.LValContext ctx) {
        if(curScope.resolve(ctx.IDENT().getText()) == null){
            OutputHelper.printSemanticError(ErrorType.Undefined_variable,ctx.IDENT().getSymbol().getLine(),
                    ctx.IDENT().getText());
            return null;
        }
        else if(curScope.resolve(ctx.IDENT().getText()) instanceof IntType && !ctx.L_BRACKT().isEmpty()){
            OutputHelper.printSemanticError(ErrorType.Not_an_array,ctx.IDENT().getSymbol().getLine(),
                    ctx.IDENT().getText());
            return null;
        }

        if(curScope.resolve(ctx.IDENT().getText()) instanceof ArrayType){
            int LbraketSize = ctx.L_BRACKT().size();
            for (int i = 0; i < ctx.exp().size(); i++) {
                Object o = visitExp(ctx.exp(i));
//				if(o == null){
//					OutputHelper.printSemanticError(ErrorType.);
//				}
                // 无需报错，因为先前已经报过
                if(o != null){
                    if(o instanceof IntType){
                        //正确
                    }
                    else {//未提及，不确定
                        OutputHelper.printSemanticError(ErrorType.The_left_hand_side_of_an_assignment_must_be_a_variable,ctx.IDENT().getSymbol().getLine(),
                                ctx.IDENT().getText());
                        return null;
                    }
                }
            }
            int remain_Dim = ((ArrayType)curScope.resolve(ctx.IDENT().getText())).getDimension() - ctx.L_BRACKT().size();
            if(remain_Dim < 0){
                OutputHelper.printSemanticError(ErrorType.Not_an_array,ctx.IDENT().getSymbol().getLine(),
                        ctx.IDENT().getText());
                return null;
            }
            else if(remain_Dim == 0){
                return IntType.getInt32();
            }
            else {
                return new ArrayType(remain_Dim);
            }
        }
        else if(curScope.resolve(ctx.IDENT().getText()) instanceof IntType){
            return IntType.getInt32();
        }
        else {//FunctionType
            OutputHelper.printSemanticError(ErrorType.The_left_hand_side_of_an_assignment_must_be_a_variable,ctx.IDENT().getSymbol().getLine(),
                    ctx.IDENT().getText());
            return null;
        }

    }


//		@Test
//	public void test(){
//			Type A = new ArrayType(1);
//			Type A1 = new ArrayType(10);
//			Type B = IntType.getInt32();
//			System.out.println(A.getClass() == A1.getClass());
//
//			Object C = null;
//			A = (Type) C;
//			System.out.println(A);
//		}

}


