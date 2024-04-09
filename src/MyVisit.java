import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class MyVisit extends SysYParserBaseVisitor {




    //BrightRed,BrightGreen,BrightYellow,BrightBlue,BrightMagenta,BrightCyan
    private int[] rainbow = new int[]{SGR_Name.LightRed.SGR, SGR_Name.LightGreen.SGR,
            SGR_Name.LightYellow.SGR, SGR_Name.LightBlue.SGR, SGR_Name.LightMagenta.SGR, SGR_Name.LightCyan.SGR};

    private boolean isOut = false;
    private Stack<String> braket = new Stack<>();

    private Set<TerminalNode> baga = new HashSet<>();
    private Stack<TerminalNode> invisible = new Stack<>();

    private Set<String> BrightCyan = new HashSet<>();

    private Set<String> BrightRed = new HashSet<>();

    public void init() {

        BrightCyan.add("const");
        BrightCyan.add("int");
        BrightCyan.add("void");
        BrightCyan.add("if");
        BrightCyan.add("else");
        BrightCyan.add("while");
        BrightCyan.add("break");
        BrightCyan.add("continue");
        BrightCyan.add("return");

        BrightRed.add("+");
        BrightRed.add("-");
        BrightRed.add("*");
        BrightRed.add("/");
        BrightRed.add("%");
        BrightRed.add("=");
        BrightRed.add("==");
        BrightRed.add("!=");
        BrightRed.add("<");
        BrightRed.add(">");
        BrightRed.add("<=");
        BrightRed.add(">=");
        BrightRed.add("!");
        BrightRed.add("&&");
        BrightRed.add("||");
        BrightRed.add(",");
        BrightRed.add(";");
    }
    private String color(String s, boolean isUnderlined) {
        if (isUnderlined) {
            return "\033[4m" +  s + "\033[0m";
        }
        return s + "\033[0m";
    }


    private String color(SGR_Name sgr_name, String s, boolean isUnderlined) {
        if (isUnderlined) {
            return "\033[4m" + "\033[" + sgr_name.SGR + "m" + s + "\033[0m";
        }
        return "\033[" + sgr_name.SGR + "m" + s + "\033[0m";
    }

    private String color(int sgr_name, String s, boolean isUnderlined) {
        if (isUnderlined) {
            return "\033[4m" + "\033[" + sgr_name + "m" + s + "\033[0m";
        }
        return "\033[" + sgr_name + "m" + s + "\033[0m";
    }
//  if   )
//  else
//  while )
    private ParserRuleContext getBrother(TerminalNode node){
        ParserRuleContext parent = (ParserRuleContext) node.getParent();
        int index = parent.children.indexOf(node);
        return (ParserRuleContext) parent.getChild(index + 1);
    }



    public TerminalNode getRight(ParseTree ctx) {
        if(ctx instanceof TerminalNode) {
            return (TerminalNode) ctx;
        }
        else {
            return getRight( ctx.getChild(ctx.getChildCount() - 1));
        }
    }


    private String help(TerminalNode node) {//zui jin
        ParserRuleContext context = (ParserRuleContext) node.getParent();
        while (context != null) {
            if (context instanceof SysYParser.StmtContext) {
                return "stmt";
            } else if (context instanceof SysYParser.DeclContext) {
                return "decl";
            }
            context = context.getParent();
        }
        return "";
    }

    private String globalhelp(TerminalNode node) {
        ParserRuleContext context = (ParserRuleContext) node.getParent();
        while (context != null) {
            if (context instanceof SysYParser.DeclContext) {
                return "decl";
            }
            context = context.getParent();
        }
        return "";
    }


    private Boolean isUnderlined(String context) {
        return "decl".equals(context);
    }


    boolean isPrint = false;

    private boolean isLeftBraceStandalone(TerminalNode node) {
        ParserRuleContext ctx = (ParserRuleContext) node.getParent();
        if (ctx instanceof SysYParser.BlockContext) {
            ctx = ctx.getParent();
            if (ctx instanceof SysYParser.StmtContext) {
                ctx = ctx.getParent();
                if (ctx instanceof SysYParser.BlockItemContext) {
                    ctx = ctx.getParent();
                    if (ctx instanceof SysYParser.BlockContext) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Boolean isSingleBrace(TerminalNode node) {
        ParseTree parent = node.getParent();
        int childCount = parent.getChildCount();
        if (parent instanceof SysYParser.BlockContext) {
            if (childCount == 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isInDeclContext(TerminalNode node) {
        ParserRuleContext ctx = (ParserRuleContext) node.getParent();
        while (ctx != null) {
            if (ctx instanceof SysYParser.DeclContext) {
                return true;
            }
            ctx = ctx.getParent();
        }
        return false;
    }


    private int indentLevel = 0;


//    缩进：
//    初始情况不需要缩进（即缩进级别为0）
//    每一级缩进使用4个空格
//    语句块（block）内缩进级别+1
//    if、else、while等语句下单独一行、不存在花括号的情况也需要缩进+1
//    但是形如else if (cond) block的情况，else if在同一行，且block缩进保持不变，block内缩进正常+1

    /* if (){
        }

        if ()
            a = 9;

        else {

        }

        else if() {
        }

        while () {
        }

        while ()
            c = 0;


     */

    private void printIndentation() {


        for (int i = 0; i < indentLevel; i++) {
            System.out.print("    ");
            isOut = true;
        }

    }


    public TerminalNode getNextLeafNode(TerminalNode leafNode) {
        if (leafNode != null) {
            ParserRuleContext parent = (ParserRuleContext) leafNode.getParent();

            int index = parent.children.indexOf(leafNode);

            while (parent != null) {
                for (int i = index + 1; i < parent.getChildCount(); i++) {
                    if (parent.getChild(i) instanceof TerminalNode) {
                        return (TerminalNode) parent.getChild(i);
                    } else if (parent.getChild(i).getChildCount() > 0) {
                        List<ParseTree> children = new ArrayList<>();
                        for (int childIndex = 0; childIndex < parent.getChild(i).getChildCount(); childIndex++) {
                            children.add(parent.getChild(i).getChild(childIndex));
                        }

                        return getLeftmostLeafNode(children);
                    }
                }
                index = parent.parent != null ? parent.getParent().children.indexOf(parent) : -1;
                parent = parent.getParent();
            }
        }
        return null;
    }

    // 获取最左侧的叶子节点
    private TerminalNode getLeftmostLeafNode(List<ParseTree> nodeList) {
        for (ParseTree node : nodeList) {
            if (node instanceof TerminalNode) {
                return (TerminalNode) node;
            }
            if (node.getChildCount() > 0) {
                ParserRuleContext node1 = (ParserRuleContext) node;
                return getLeftmostLeafNode(new ArrayList<>(node1.children));
            }
        }
        return null;
    }

    public TerminalNode getPreviousLeafNode(TerminalNode leafNode) {
        if (leafNode != null) {
            ParserRuleContext parent = (ParserRuleContext) leafNode.getParent();


            int index = parent.children.indexOf(leafNode);


            while (parent != null && index >= 0) {
                for (int i = index - 1; i >= 0; i--) {
                    if (parent.getChild(i) instanceof TerminalNode) {
                        return (TerminalNode) parent.getChild(i);
                    } else if (parent.getChild(i).getChildCount() > 0) {
                        List<ParseTree> children = new ArrayList<>();
                        for (int j = parent.getChild(i).getChildCount() - 1; j >= 0; j--) {
                            children.add(parent.getChild(i).getChild(j));
                        }
                        TerminalNode rightmostLeaf = getRightmostLeafNode(children);
                        if (rightmostLeaf != null) {
                            return rightmostLeaf;
                        }
                    }
                }
                if (parent.parent != null) {
                    index = parent.getParent().children.indexOf(parent);
                    parent = parent.getParent();
                } else {
                    return null;
                }
            }
        }
        return null;
    }


    private TerminalNode getRightmostLeafNode(List<ParseTree> nodeList) {
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            ParseTree node = nodeList.get(i);
            if (node instanceof TerminalNode) {
                return (TerminalNode) node;
            }
            if (node.getChildCount() > 0) {
                ParserRuleContext node1 = (ParserRuleContext) node;
                TerminalNode leafNode = getRightmostLeafNode(node1.children);
                if (leafNode != null) {
                    return leafNode;
                }
            }
        }
        return null;
    }


    private TerminalNode getRightParen(TerminalNode node) {
        ParserRuleContext parent = (ParserRuleContext) node.getParent();
        int index = parent.children.indexOf(node);
        for (int i = index + 1; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) instanceof TerminalNode) {
                TerminalNode parentChild = (TerminalNode) parent.getChild(i);
                if (parentChild.getSymbol().getType() == SysYParser.R_PAREN) {
                    return parentChild;
                }
            }
        }
        return null;
    }
    public int check(TerminalNode node){
        int cut=0;
        while (true){
            if(!invisible.isEmpty() && invisible.peek().equals(node)){
                cut ++;
                invisible.pop();
            }
            else {
                return cut;
            }
        }

    }

    public void meetLL(TerminalNode node){
        if (!isSingleBrace(node)) {
            indentLevel++;
        }
        System.out.println();
        printIndentation();
        isOut =true;
    }
    public void meetLL(){
        indentLevel++;
        System.out.println();
        printIndentation();
        isOut =true;
    }

    public void meetRR(TerminalNode node){
        TerminalNode nextLeafNodeLeafNode = getNextLeafNode(node);
        if (nextLeafNodeLeafNode.getSymbol().getType() == SysYParser.R_BRACE) {
            indentLevel--;
        }
        System.out.println();
        printIndentation();
        isOut =true;
    }


    // ) || else
    public void caozuo(TerminalNode node){
        ParserRuleContext brother = getBrother(node);
        invisible.push(getRight(brother));
        baga.add(node);
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        if (node.getText().equals("<EOF>")) {
            return super.visitTerminal(node);
        }
        String color = getHilight(node);

        if (node.getSymbol().getType() == SysYParser.L_BRACE) {
            isOut = true;
            if (isLeftBraceStandalone(node)) {
                TerminalNode terminalNode = getPreviousLeafNode(node);
                TerminalNode lastNode = getNextLeafNode(node);
                if (terminalNode.getSymbol().getType() != SysYParser.SEMICOLON
                        && lastNode.getSymbol().getType() != SysYParser.L_BRACE
                        && terminalNode.getSymbol().getType() != SysYParser.L_BRACE
                        && terminalNode.getSymbol().getType() != SysYParser.R_BRACE) {
                    System.out.println();
                    printIndentation();
                }
            } else {
                if(!isintival(getPreviousLeafNode(node)))
                    System.out.print(" ");
            }
        }
        if (node.getSymbol().getType() == SysYParser.R_BRACE && !isInDeclContext(node)) {
            isOut = true;
            TerminalNode terminalNode = getPreviousLeafNode(node);
            if (terminalNode.getSymbol().getType() != SysYParser.L_BRACE && terminalNode.getSymbol().getType() != SysYParser.R_BRACE && terminalNode.getSymbol().getType() != SysYParser.SEMICOLON) {
                System.out.println();
                printIndentation();
            }

        }

        ParserRuleContext context = (ParserRuleContext) node.getParent();
        if (context instanceof SysYParser.FunctypeContext) {
            if(!isFirst(node)){
                System.out.println();
            }
        }


        TerminalNode previousLeafNode = getPreviousLeafNode(node);
        if (node.getSymbol().getType() == SysYParser.IF
                && previousLeafNode != null
                && previousLeafNode.getSymbol().getType() == SysYParser.ELSE) {
            // do nothing
        } else {

            System.out.print(fixColor(node, color));
            isOut = true;
        }


//  ************************************************************************************





        if (node.getSymbol().getType() == SysYParser.ELSE) {
            TerminalNode nodeif = getNextLeafNode(node);
            if (nodeif != null && nodeif.getSymbol().getType() == SysYParser.IF) {
                TerminalNode Rparen = getRightParen(nodeif);
                if (Rparen != null) {
                    isOut = true;
                    TerminalNode node1 = getNextLeafNode(Rparen);
                    if (node1 != null && node1.getSymbol().getType() != SysYParser.L_BRACE) {
                        caozuo(Rparen);
                        System.out.print(" " +getHilight(nodeif) + " ");
                        isPrint = true;
                    } else {
                        System.out.print(" " + getHilight(nodeif) + " ");
                    }
                }
            } else if (nodeif != null && nodeif.getSymbol().getType() != SysYParser.L_BRACE) {
                caozuo(node);
                meetLL();
            }
        }


        if (node.getSymbol().getType() == SysYParser.WHILE
                || ((previousLeafNode == null || (previousLeafNode != null && previousLeafNode.getSymbol().getType() != SysYParser.ELSE))
                && node.getSymbol().getType() == SysYParser.IF)) {
            TerminalNode Rparen = getRightParen(node);
            if (Rparen != null) {
                TerminalNode node1 = getNextLeafNode(Rparen);
                if (node1 != null && node1.getSymbol().getType() != SysYParser.L_BRACE) {
                    caozuo(Rparen);

                }
            }
        }


        if (node.getSymbol().getType() == SysYParser.SEMICOLON ||
                (node.getSymbol().getType() == SysYParser.R_BRACE && !isInDeclContext(node))
                || (node.getSymbol().getType() == SysYParser.L_BRACE && !isInDeclContext(node))) {

            if (getNextLeafNode(node).getSymbol().getType() == SysYParser.EOF) {
                return super.visitTerminal(node);
            }
//            if (node.getSymbol().getType() == SysYParser.L_BRACE &&
//                    getNextLeafNode(node).getSymbol().getType() == SysYParser.L_BRACE) {
////                //do nothing
//            }
//            else

            if (node.getSymbol().getType() == SysYParser.L_BRACE) {
                meetLL(node);
            }
            if (node.getSymbol().getType() == SysYParser.R_BRACE) {
                int cut = check(node);
                TerminalNode nextLeafNodeLeafNode = getNextLeafNode(node);
                if (nextLeafNodeLeafNode.getSymbol().getType() == SysYParser.R_BRACE) {
                    indentLevel--;
                }
                while (cut -- > 0){
                    indentLevel--;
                }
                System.out.println();
                printIndentation();
                isOut = true;

            }
            if (node.getSymbol().getType() == SysYParser.SEMICOLON) {
                int cut = check(node);
                TerminalNode nextLeafNode = getNextLeafNode(node);
                ParserRuleContext parent = (ParserRuleContext) node.getParent();
                if (nextLeafNode != null && (nextLeafNode.getSymbol().getType() == SysYParser.R_BRACE )) {
                    indentLevel--;
                }
                while (cut -- > 0){
                    indentLevel--;
                }

                System.out.println();
                printIndentation();
                isOut =true;
            }


        }

        if (node.getSymbol().getType() == SysYParser.R_PAREN && baga.contains(node)) {

                meetLL();


        }


        return super.visitTerminal(node);
    }

    private String fixColor(TerminalNode node, String color) {
        String text = node.getText();


        ParserRuleContext parent = (ParserRuleContext) node.getParent();

        if (parent instanceof SysYParser.FunctypeContext) {
            if (!isintival(getNextLeafNode(node)))
                color += " ";
        } else if (text.equals("const") || text.equals("int") || text.equals("void") || text.equals("if")
                || text.equals("else") || text.equals("while")) {
            if (text.equals("else")) {
                TerminalNode nodeif = getNextLeafNode(node);
                if (nodeif.getSymbol().getType() == SysYParser.L_BRACE) {
                    // do nothing
                }
            } else {
                if (!isintival(getNextLeafNode(node)))
                    color += " ";
            }


        }
        if (text.equals("return")) {
            TerminalNode nextLeafNode = getNextLeafNode(node);
            if (nextLeafNode != null && nextLeafNode.getSymbol().getType() == SysYParser.SEMICOLON) {
                // do nothing
            } else {
                if (!isintival(getNextLeafNode(node)))
                    color += " ";
            }
        }

        if (text.equals("*") || text.equals("/")
                || text.equals("%") || text.equals("=") || text.equals("==")
                || text.equals("!=") || text.equals("<") || text.equals(">")
                || text.equals("<=") || text.equals(">=") || text.equals("&&") || text.equals("||")) {
            if (!isintival(getNextLeafNode(node))){
                color = " " + color +" ";
            }
            else
                color = " " + color ;
        }
        if (text.equals("+") || text.equals("-") || text.equals("!")) {
            ParserRuleContext parent1 = (ParserRuleContext) node.getParent();
            if (parent1 instanceof SysYParser.UnaryOpContext) {
                //do nothing
            } else {
                if (!isintival(getNextLeafNode(node))){
                    color = " " + color+" ";
                }
                else
                    color = " " + color ;
            }

        }
        if (text.equals(",")) {
            if (!isintival(getNextLeafNode(node)))
                color += " ";
        }

        return color;
    }
    private boolean isintival(TerminalNode node){
        if(node.getSymbol().getType() != SysYParser.L_BRACE){
            return false;
        }
        ParserRuleContext parent = (ParserRuleContext) node.getParent();
        if(parent instanceof SysYParser.InitValContext){
            return true;
        }
        return false;
    }

    private boolean isFirst(TerminalNode node){
        ParserRuleContext parent = (ParserRuleContext) node.getParent();
        if(parent instanceof SysYParser.FunctypeContext){
            ParserRuleContext parent1 = (ParserRuleContext)parent.getParent();
            if(parent1 instanceof SysYParser.FuncDefContext){
                ParserRuleContext parent2 = (ParserRuleContext)parent1.getParent();
                if(parent2 instanceof SysYParser.CompUnitContext && parent2.children.indexOf(parent1) == 0){
                    return true;
                }
            }
        }
        return false;
    }


    private String getHilight(TerminalNode node) {
        String color = "";
        Vocabulary vocabulary = SysYParser.VOCABULARY;
        String currentContext = help(node);
        String globalcurrentContext = globalhelp(node);
        int type = node.getSymbol().getType();

        ParserRuleContext context = (ParserRuleContext) node.getParent();
        if (node.getText().equals("{") || node.getText().equals("(") || node.getText().equals("[")) {
            color = (color(rainbow[braket.size() % 6], node.getText(), isUnderlined(globalcurrentContext)));
            braket.push(node.getText());
        } else if (node.getText().equals("}") || node.getText().equals(")") || node.getText().equals("]")) {
            braket.pop();
            color = (color(rainbow[braket.size() % 6], node.getText(), isUnderlined(globalcurrentContext)));
        } else if (BrightCyan.contains(node.getText())) {
            color = (color(SGR_Name.LightCyan, node.getText(), isUnderlined(globalcurrentContext)));
        } else if (BrightRed.contains(node.getText())) {
            color = (color(SGR_Name.LightRed, node.getText(), isUnderlined(globalcurrentContext)));
        } else if (vocabulary.getSymbolicName(type).equals("INTEGER_CONST")) {
            color = (color(SGR_Name.Magenta, node.getText(), isUnderlined(globalcurrentContext)));
        } else if (vocabulary.getSymbolicName(type).equals("IDENT") && (context instanceof SysYParser.ExpContext || context instanceof SysYParser.FuncDefContext)) {
            color = (color(SGR_Name.LightYellow, node.getText(), isUnderlined(globalcurrentContext)));
        } else if ("stmt".equals(currentContext)) {
            color = (color(SGR_Name.White, node.getText(), isUnderlined(globalcurrentContext)));
        } else if ("decl".equals(currentContext)) {
            color = (color(SGR_Name.LightMagenta, node.getText(), isUnderlined(globalcurrentContext)));
        } else {
            color = (color(node.getText(), isUnderlined(globalcurrentContext)));
        }
        return color;
    }
}


