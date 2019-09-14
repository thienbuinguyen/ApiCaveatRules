import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class InvalidArgInspection extends AbstractBaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.InvalidArgInspection");
    private RuleCollection rules = new RuleCollection();

    /**
     * This method is overridden to provide a custom visitor
     * that inspects expressions with relational operators '==' and '!='
     * The visitor must not be recursive and must be thread-safe.
     *
     * @param holder     object for visitor to register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return non-null visitor for this inspection.
     * @see JavaElementVisitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                PsiMethod method = expression.resolveMethod();
                String apiName = method.getName();
                String className = method.getContainingClass().getQualifiedName();

                PsiParameter[] parameters = method.getParameterList().getParameters();
                String[] paramTypes = new String[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    paramTypes[i] = parameters[i].getType().getPresentableText();
                }

                APIMethod apiMethod = rules.getApi(className, apiName, paramTypes);
                if (apiMethod != null) {
                    NonNullCaveat caveat = (NonNullCaveat) apiMethod.getCaveat(NonNullCaveat.class);
                    if (caveat != null) {
                        PsiType[] psiArgTypes = expression.getArgumentList().getExpressionTypes();
                        String[] argTypes = new String[psiArgTypes.length];
                        for (int i = 0; i < psiArgTypes.length; i++) argTypes[i] = psiArgTypes[i].getPresentableText();
                        ArrayList<Integer> lst = caveat.checkViolation(argTypes);

                        for (Integer j : lst) {
                            holder.registerProblem(expression,
                                    "Parameter \"" + parameters[j].getName() +"\" must not be null in:\n" + apiMethod.signature);
                        }
                    }
                }


            }
        };
    }
}
