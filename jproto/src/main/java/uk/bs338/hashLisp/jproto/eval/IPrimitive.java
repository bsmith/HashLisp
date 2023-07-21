package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;

import java.util.Optional;

@FunctionalInterface
public interface IPrimitive<T>  {
    @NotNull T apply(@NotNull IEvaluator<T> evaluator, @NotNull T args) throws EvalException;
    
    default @NotNull Optional<T> substitute(@NotNull ISubstitutor<T> substitutor, @NotNull T args) {
        return Optional.empty();
    }
}
