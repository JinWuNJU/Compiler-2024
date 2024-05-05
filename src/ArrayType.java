import java.util.List;

public class ArrayType extends Type{
    private int dimension;

    public ArrayType(int dimension) {
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }
}
