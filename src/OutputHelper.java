import java.util.*;

public class OutputHelper {

    // 创建一个映射，将索引映射到对应的值
    static Set<Index> set = new HashSet<>();
    public static boolean isFalse = false;

    public static void printSemanticError(ErrorType errorType, int line, String text) {
        isFalse = true;

            System.err.println("Error type " + errorType.getType() + " at Line " + line + ":[" + text + ":" + errorType + "].");

        //Error type [errorTypeNo] at Line [lineNo]:[errorMessage]
    }

    private static boolean checkCut(int line, String text) {
        Index index = new Index(line, text);
        if (set.contains(index)) {
            return true;
        }
        set.add(index);
        return false;
    }
}
