import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.HashMap;
import java.util.Map;

public class llvmSymbolTable {
    private Map<String, LLVMValueRef> symbols = new HashMap<>();
    private llvmSymbolTable parent = null;

    public llvmSymbolTable(llvmSymbolTable parent) {
        this.parent = parent;
    }

    public void define(String name, LLVMValueRef type) {
        symbols.put(name, type);
    }

    public LLVMValueRef resolve(String name) {//用于变量使用
        LLVMValueRef type = symbols.get(name);
        if (type != null) {
            return type;
        } else if (parent != null) {
            return parent.resolve(name);
        } else {
            return null;
        }
    }

    public LLVMValueRef resolveForDecl(String name){
        return symbols.get(name);
    }

    public llvmSymbolTable getParent() {
        return parent;
    }
}
