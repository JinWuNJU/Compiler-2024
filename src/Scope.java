import java.util.HashMap;
import java.util.Map;

public abstract class Scope {
    private final Map<String , Type>symbols = new HashMap<>();
    private final Scope enclosingScope;



    public Scope(Scope enclosingScope){
        this.enclosingScope = enclosingScope;
    }

    public boolean find(String symbolName) {
        return symbols.containsKey(symbolName);
    }

    public void define(String name, Type type){
        this.symbols.put(name,type);
    }

    public Map<String, Type> getSymbols() {
        return this.symbols;
    }

    public Scope getEnclosingScope() {
        return this.enclosingScope;
    }

    public Type resolve(String name){
        if(this.find(name)){
            return this.symbols.get(name);
        }
        if(this.enclosingScope != null){
            return this.enclosingScope.resolve(name);
        }
        return null;
    }
}
