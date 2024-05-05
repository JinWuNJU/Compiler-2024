public class VoidType extends Type{
    private static VoidType voidType = new VoidType();
    private VoidType(){}

    public static VoidType getVoidType(){
        return voidType;
    }
}
