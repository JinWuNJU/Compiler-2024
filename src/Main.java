import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.io.IOException;
import java.util.List;

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

        List<? extends Token> myTokens = sysYLexer.getAllTokens();

        Vocabulary vocabulary = sysYLexer.getVocabulary();

        if (ErrorListener.isError == true) {
            ErrorListener.printLexerErrorInformation();
        } else {
            for (Token t : myTokens) {
                printSysYTokenInformation(t, vocabulary);
            }
        }


    }

    public static void printSysYTokenInformation(Token t, Vocabulary vocabulary) {
        String text = t.getText();
        int line = t.getLine();
        int type = t.getType();
        String sym = vocabulary.getSymbolicName(type);
        if (sym.equals("INTEGR_CONST")) {
            if (text.charAt(0) == '0' && text.length() >= 2) {
                if (text.startsWith("0x") || text.startsWith("0X")) {
                    text = String.valueOf(Long.parseLong(text.substring(2), 16));
                } else {
                    text = String.valueOf(Long.parseLong(text.substring(1), 8));
                }
            }
        }
        System.err.println(sym + " " + text + " at Line " + line + ".");
    }


}