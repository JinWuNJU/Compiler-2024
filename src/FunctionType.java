import java.util.ArrayList;
import java.util.List;

public class FunctionType extends Type{
    Type retTy;//返回值类型
    List<Type> paramsType;//参数类型

    public FunctionType(Type retTy, List<Type> paramsType) {
        this.retTy = retTy;
        this.paramsType = paramsType;
    }
}
