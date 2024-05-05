public class IntType extends Type{
    private static IntType intType = new IntType();

    private IntType(){

    }
    public static IntType getInt32(){
        return intType;
    }
}
