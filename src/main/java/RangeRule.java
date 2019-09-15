public class RangeRule {
    public int param;
    public String op;
    public String constraint;

    @Override
    public String toString() {
        return "{paramIndex: "+param+", operator: "+op+", constraint: "+constraint+"}";
    }
}
