package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Optional;

@FunctionalInterface
public interface IPrimitive  {
    @NotNull HonsValue apply(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException;
    
    default @NotNull HonsValue apply(@NotNull LazyEvaluator evaluator, @NotNull HonsValue value, @NotNull HonsValue args) throws EvalException {
        return this.apply(evaluator, args);
    }
    
    /* Return empty to mean: recurse into this as if it were an apply */
    default @NotNull Optional<HonsValue> substitute(@NotNull LazyEvaluator evaluator, @NotNull Assignments assignments, @NotNull HonsValue value, @NotNull HonsValue args) {
        return Optional.empty();
    }
}
