package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.IExpr;

public interface ISubstitutor {
    /* for recursive substitution */
    @NotNull IExpr substitute(@NotNull IExpr body);

    @NotNull IExpr substitute(@NotNull Assignments assignments, @NotNull IExpr body);
    
    @NotNull Assignments getAssignments();
}
