package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.IExpr;

import java.util.Optional;

@FunctionalInterface
public interface IPrimitive  {
    @NotNull IExpr apply(@NotNull LazyEvaluator evaluator, @NotNull IExpr args) throws EvalException;
    
    /* Return empty to mean: recurse into this as if it were an apply */
    default @NotNull Optional<IExpr> substitute(@NotNull LazyEvaluator evaluator, @NotNull Assignments assignments, @NotNull IExpr value, @NotNull IExpr args) throws EvalException {
        return Optional.empty();
    }
}
