import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class ErrorListener extends BaseErrorListener {

    public static List<String> list = new ArrayList<>();
    public static boolean isError = false;


    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        isError = true;
        list.add("Error type A at Line " + line + ": baga");
    }

    public static void printLexerErrorInformation(){
        for(String s : list){
            System.err.println(s);
        }

    }
}
