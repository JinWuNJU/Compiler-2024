import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);


        sysYLexer.removeErrorListeners();
        sysYLexer.addErrorListener(new ErrorListener());

        List<? extends Token> myTokens = sysYLexer.getAllTokens();



//        if (myErrorListener has ErrorInformation) {
//            // 假设myErrorListener有一个错误信息输出函数printLexerErrorInformation.
//            myErrorListener.printLexerErrorInformation();
//        } else {
//            for (Token t : myTokens) {
//                printSysYTokenInformation(t);
//            }
//        }


    }




}