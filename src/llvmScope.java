import java.util.HashMap;


public class llvmScope {
    public llvmScope parent;
    public HashMap<String, Integer> map;

    public llvmScope(){
        parent = null;
        map = new HashMap<String, Integer>();
    }

    public llvmScope(llvmScope parent_){
        parent = parent_;
        map = new HashMap<String, Integer>();
    }
    public Integer replace(String key, int val){
        if(!map.containsKey(key)){
            return parent.replace(key, val);
        }
        return map.replace(key, val);
    }

    public int find(String s){
        if(map.containsKey(s)){
            return map.get(s);
        }
        while (parent != null){
            if(parent.map.containsKey(s)){
                return parent.map.get(s);
            }
            parent = parent.parent;
        }
        return 0;
    }


}