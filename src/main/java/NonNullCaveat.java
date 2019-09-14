import java.util.ArrayList;
import java.util.Arrays;

public class NonNullCaveat implements Caveat {
    private int[] nonNullIndices;

    NonNullCaveat(int[] nonNullIndices) {
        this.nonNullIndices = nonNullIndices;
    }

    public ArrayList<Integer> checkViolation(String[] argTypes) {
        ArrayList<Integer> violationIndices = new ArrayList<>();
        for (int i : nonNullIndices) {
            if (argTypes[i].equals("null")) violationIndices.add(i);
        }
        return violationIndices;
    }

    @Override
    public String toString() {
        return "Non null indices: " + Arrays.toString(nonNullIndices);
    }
}
