import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

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
            public void visitNewExpression(PsiNewExpression expression) {
                PsiJavaCodeReferenceElement classReference = expression.getClassReference();
                if (classReference != null) {
                    String className = classReference.getQualifiedName();
                    String apiName = classReference.getReferenceName();

                    PsiType[] parameterTypes = expression.getArgumentList().getExpressionTypes();
                    String[] paramTypes = new String[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) paramTypes[i] = parameterTypes[i].getPresentableText();


                    APIMethod apiMethod = rules.getApi(className, apiName, paramTypes);
                    if (apiMethod != null) {
                        InvalidArgCaveat caveat = (InvalidArgCaveat) apiMethod.getCaveat(InvalidArgCaveat.class);
                        if (caveat != null) {
                            PsiExpression[] expressions = expression.getArgumentList().getExpressions();
                            String[] args = new String[expressions.length];
                            for (int i = 0; i < expressions.length; i++) args[i] = expressions[i].getText();
                            ArrayList<CaveatViolation> lst = caveat.checkViolation(args);

                            for (CaveatViolation cv : lst) {
                                holder.registerProblem(expression,
                                        "Parameter number " + (cv.index + 1) +" " + cv.reason);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                PsiMethod method = expression.resolveMethod();
                if (method != null) {
                    String apiName = method.getName();

                    if (method.getContainingClass() != null) {
                        String className = method.getContainingClass().getQualifiedName();

                        PsiParameter[] parameters = method.getParameterList().getParameters();
                        String[] paramTypes = new String[parameters.length];

                        for (int i = 0; i < parameters.length; i++) {
                            paramTypes[i] = parameters[i].getType().getPresentableText();
                        }

                        APIMethod apiMethod = rules.getApi(className, apiName, paramTypes);
                        if (apiMethod != null) {

                            InvalidArgCaveat caveat = (InvalidArgCaveat) apiMethod.getCaveat(InvalidArgCaveat.class);
                            if (caveat != null) {
                                PsiExpression[] expressions = expression.getArgumentList().getExpressions();
                                String[] args = new String[expressions.length];
                                for (int i = 0; i < expressions.length; i++) args[i] = expressions[i].getText();
                                System.out.println(caveat);
                                ArrayList<CaveatViolation> lst = caveat.checkViolation(args);

                                for (CaveatViolation cv : lst) {
                                    holder.registerProblem(expression,
                                            "Parameter \"" + parameters[cv.index].getName() +"\" " + cv.reason + ": \n" + apiMethod.signature);
                                }
                            }
                        }
                    }

                }
            }
        };
    }
}
