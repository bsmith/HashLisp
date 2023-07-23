package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.IExpr;

import java.util.Optional;

@FunctionalInterface
public interface IPrimitive  {
    @NotNull IExpr apply(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException;
    
    default @NotNull Optional<IExpr> substitute(@NotNull ISubstitutor substitutor, @NotNull IExpr args) throws EvalException {
        return Optional.empty();
    }
}
