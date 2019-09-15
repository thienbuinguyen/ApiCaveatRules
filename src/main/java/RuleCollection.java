import com.google.gson.Gson;
import com.intellij.util.containers.hash.HashMap;
import javassist.bytecode.stackmap.TypeData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RuleCollection {

    private static final Logger LOGGER = Logger.getLogger(TypeData.ClassName.class.getName());
    private HashMap<String, APIClass> classToCaveats = new HashMap<>();

    RuleCollection() {
        loadRules();
    }

    private class NonNullRule {
        private String className;
        private String api;
        private String signature;
        String[] paramTypes;
        int[] notNullIndices;
    }

    private class InvalidArgRule {
        private String className;
        private String api;
        private String signature;
        String[] paramTypes;
        RangeRule[] rangeRules;
    }

    private void loadInvalidArgRules() {
        try {
            Gson gson = new Gson();
            InputStream resourceAsStream = getClass().getResourceAsStream("exception_range_rules_filtered.json");
            if (resourceAsStream == null) {
                LOGGER.log(Level.SEVERE, "Failed to get resource as stream: exception_range_rules_filtered.json");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }

                InvalidArgRule[] invalidArgRules = gson.fromJson(stringBuilder.toString(), InvalidArgRule[].class);

//              Add all invalid argument rules
                for (InvalidArgRule r : invalidArgRules) {
                    if (classToCaveats.containsKey(r.className)) {
                        APIClass cls = classToCaveats.get(r.className);
                        APIMethod method = cls.getMethod(r.api, r.paramTypes);
                        if (method == null) {
                            APIMethod newMethod = new APIMethod(r.api, r.signature, r.paramTypes);
                            newMethod.caveats.add(new InvalidArgCaveat(r.rangeRules));
                            cls.methods.add(newMethod);
                        } else method.caveats.add(new InvalidArgCaveat(r.rangeRules));
                    } else {
                        APIClass cls = new APIClass(r.className);
                        APIMethod method = new APIMethod(r.api, r.signature, r.paramTypes);
                        method.caveats.add(new InvalidArgCaveat(r.rangeRules));
                        cls.methods.add(method);

                        classToCaveats.put(r.className, cls);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    private void loadNonNullRules() {
        try {
            Gson gson = new Gson();
            InputStream resourceAsStream = getClass().getResourceAsStream("non_null_rules.json");
            if (resourceAsStream == null) {
                LOGGER.log(Level.SEVERE, "Failed to get resource as stream: non_null_rules.json");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }

                // Add all non null caveat rules
                NonNullRule[] notNullRules = gson.fromJson(stringBuilder.toString(), NonNullRule[].class);
                for (NonNullRule r : notNullRules) {
                    if (classToCaveats.containsKey(r.className)) {
                        APIClass cls = classToCaveats.get(r.className);
                        APIMethod method = cls.getMethod(r.api, r.paramTypes);
                        if (method == null) {
                            APIMethod newMethod = new APIMethod(r.api, r.signature, r.paramTypes);
                            newMethod.caveats.add(new NonNullCaveat(r.notNullIndices));
                            cls.methods.add(newMethod);
                        } else method.caveats.add(new NonNullCaveat(r.notNullIndices));
                    } else {
                        APIClass cls = new APIClass(r.className);
                        APIMethod method = new APIMethod(r.api, r.signature, r.paramTypes);
                        method.caveats.add(new NonNullCaveat(r.notNullIndices));
                        cls.methods.add(method);

                        classToCaveats.put(r.className, cls);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    public APIMethod getApi(String className, String apiName, String[] paramTypes) {
        if (classToCaveats.containsKey(className)) {
            APIClass apiClass = classToCaveats.get(className);
            APIMethod apiMethod = apiClass.getMethod(apiName, paramTypes);

            if (apiMethod != null) return apiMethod;
        }
        return null;
    }

    private void loadRules() {
        loadInvalidArgRules();
    }
}
