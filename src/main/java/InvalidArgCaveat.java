import com.intellij.util.containers.hash.HashMap;

import java.util.ArrayList;
import java.util.Arrays;

public class InvalidArgCaveat implements Caveat {
    private RangeRule[] rangeRules;

    InvalidArgCaveat(RangeRule[] rangeRules) {
        this.rangeRules = rangeRules;
    }

    public ArrayList<CaveatViolation> checkViolation(String[] args) {
        ArrayList<CaveatViolation> violations = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            for (RangeRule rule : rangeRules) {
                if (rule.param == i) {
                    if (rule.constraint.equals("null")) {
                        if (args[i].equals("null")) {
                            if (rule.op.equals("=")) violations.add(new CaveatViolation(i, "must not be null"));
                            else if (rule.op.equals("!=")) violations.add(new CaveatViolation(i, "must be null"));
                        }
                    } else { // constraint is an int
                        try {
                            int arg = Integer.parseInt(args[i]);
                            int constraint = Integer.parseInt(rule.constraint);
                            if (rule.op.equals("<") && arg < constraint)
                                violations.add(new CaveatViolation(i, "is less than "+rule.constraint));
                            else if (rule.op.equals("<=") && arg <= constraint)
                                violations.add(new CaveatViolation(i, "is less than or equal to "+rule.constraint));
                            else if (rule.op.equals(">") && arg > constraint)
                                violations.add(new CaveatViolation(i, "is greater than "+rule.constraint));
                            else if (rule.op.equals(">=") && arg >= constraint)
                                violations.add(new CaveatViolation(i, "is greater than or equal to "+rule.constraint));
                            else if (rule.op.equals("=") && arg == constraint)
                                violations.add(new CaveatViolation(i, "is equal to "+rule.constraint));
                            else if (rule.op.equals("!=") && arg != constraint)
                                violations.add(new CaveatViolation(i, "is not be equal to "+rule.constraint));
                        } catch (Exception e) {
                            // do nothing if parsing fails
                        }
                    }
                }
            }
        }
        return violations;
    }

    @Override
    public String toString() {
        return "Invalid argument rules: " + Arrays.toString(rangeRules);
    }
}
