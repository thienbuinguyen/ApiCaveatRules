import java.util.ArrayList;

public class APIClass {
    private String name;
    ArrayList<APIMethod> methods;

    APIClass(String name) {
        this.name = name;
        methods = new ArrayList<>();
    }

    public APIMethod getMethod(String methodName, String[] paramTypes) {
        for (APIMethod method : methods) {
            if (method.name.equals(methodName) && method.paramTypes.length == paramTypes.length) {

                boolean paramTypesMatch = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!paramTypes[i].equals(method.paramTypes[i])) {
                        paramTypesMatch = false;
                        break;
                    }
                }

                if (paramTypesMatch) {
                    return method;
                }
            }
        }
        return null;
    }
}