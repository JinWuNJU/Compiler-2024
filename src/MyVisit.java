import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashSet;
import java.util.Set;

public class MyVisit extends SysYParserBaseVisitor {
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

    private String color(SGR_Name sgr_name, String s, boolean isUnderlined) {
        if (isUnderlined) {
            return "\033[4m" + "\033[" + sgr_name.SGR + "m" + s + "\033[0m";
        }
        return "\033[" + sgr_name.SGR + "m" + s + "\033[0m";
    }


    private String help(TerminalNode node) {
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

    private Boolean isUnderlined(String context) {
        return "decl".equals(context);
    }


    @Override
    public Object visitTerminal(TerminalNode node) {


        Vocabulary vocabulary = SysYParser.VOCABULARY;
        String currentContext = help(node);
        int type = node.getSymbol().getType();
        if (BrightCyan.contains(node.getText())) {
            System.out.println(color(SGR_Name.LightCyan, node.getText(), isUnderlined(currentContext)));
        } else if (BrightRed.contains(node.getText())) {
            System.out.println(color(SGR_Name.LightRed, node.getText(), isUnderlined(currentContext)));
        } else if (vocabulary.getSymbolicName(type).equals("INTEGER_CONST")) {
            System.out.println(color(SGR_Name.Magenta, node.getText(), isUnderlined(currentContext)));
        } else if ("stmt".equals(currentContext)) {
            System.out.println(color(SGR_Name.White, node.getText(), isUnderlined(currentContext)));
        } else if ("decl".equals(currentContext)) {
            System.out.println(color(SGR_Name.LightMagenta, node.getText(), isUnderlined(currentContext)));
        }
        return super.visitTerminal(node);
    }
}


