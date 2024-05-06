import java.util.*;

public class OutputHelper {

    // 创建一个映射，将索引映射到对应的值
    static Map<Integer,Integer> map = new HashMap<>();
    public static boolean isFalse = false;

    public static void printSemanticError(ErrorType errorType, int line, String text) {
        isFalse = true;

        if(checkCut(line,errorType.getType())){
            System.err.println("Error type " + errorType.getType() + " at Line " + line + ":[" + text + ":" + errorType + "].");
        }



        //Error type [errorTypeNo] at Line [lineNo]:[errorMessage]
    }

    private static boolean checkCut(int line, int value) {
        if(!map.containsKey(line)){

            map.put(line,value);
            return true;
        }
        if(map.get(line) == value){
            return true;
        }
        return false;
    }
}
