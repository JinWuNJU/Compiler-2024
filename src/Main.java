import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);


        sysYLexer.removeErrorListeners();
        sysYLexer.addErrorListener(new ErrorListener());


        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);

        sysYParser.removeErrorListeners();
        sysYParser.addErrorListener(new ErrorListener());

        ParseTree tree = sysYParser.program();

        if (ErrorListener.isError == true) {
            ErrorListener.printLexerErrorInformation();
            return;
        }
        MyVisit visitor = new MyVisit();
        visitor.init();
        //const,int,void,if,else,while,break,continue,return


        visitor.visit(tree);


    }


}


//            for (Token t : myTokens) {
//                String text = t.getText();
//                int line = t.getLine();
//                int type = t.getType();
//                String sym = vocabulary.getSymbolicName(type);
//                if (sym.equals("INTEGER_CONST")) {
//                    if (text.charAt(0) == '0' && text.length() >= 2) {
//                        if (text.startsWith("0x") || text.startsWith("0X")) {
//                            text = String.valueOf(Long.parseLong(text.substring(2), 16));
//                        } else {
//                            text = String.valueOf(Long.parseLong(text.substring(1), 8));
//                        }
//                    }
//                }
//                System.out.println(sym + " " + text + " at Line " + line + ".");
//            }