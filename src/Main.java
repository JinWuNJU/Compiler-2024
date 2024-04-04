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
        visitor.visit(tree);
    }
}
