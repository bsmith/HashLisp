package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;

@FunctionalInterface
public interface IPrimitive<T>  {
    @NotNull T apply(@NotNull IEvaluator<T> evaluator, @NotNull T args) throws EvalException;
}
