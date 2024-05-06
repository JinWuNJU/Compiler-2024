import java.util.*;

public class OutputHelper {

    // 创建一个映射，将索引映射到对应的值
    static Set<Index> set = new HashSet<>();
    public static boolean isFalse = false;

    public static void printSemanticError(ErrorType errorType, int line, String text) {
        isFalse = true;
        if(errorType.getType() == 6){
            if(!checkCut(6,line)){
                System.err.println("Error type " + errorType.getType() + " at Line " + line + ":[" + text + ":" + errorType + "].");
            }
        }
        else{
            System.err.println("Error type " + errorType.getType() + " at Line " + line + ":[" + text + ":" + errorType + "].");
        }

        //Error type [errorTypeNo] at Line [lineNo]:[errorMessage]
    }

    private static boolean checkCut(int value, int line) {
        Index index = new Index(value,line);
        if (set.contains(index)) {
            return true;
        }
        set.add(index);
        return false;
    }
}
