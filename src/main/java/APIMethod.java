import java.util.ArrayList;

public class APIMethod {
    String name;
    String signature;
    String[] paramTypes;
    ArrayList<Caveat> caveats;

    APIMethod(String name, String signature, String[] paramTypes) {
        this.name = name;
        this.signature = signature;
        this.paramTypes = paramTypes;
        this.caveats = new ArrayList<>();
    }

    public Caveat getCaveat(Class<?> cls) {
        for(Caveat c : caveats) {
            if (c.getClass() == cls) return c;
        }
        return null;
    }
}