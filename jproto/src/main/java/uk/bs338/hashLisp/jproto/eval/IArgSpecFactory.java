package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface IArgSpecFactory {
    @NotNull ArgSpec get(@NotNull HonsValue argSpec) throws EvalException;

    @NotNull Assignments match(@NotNull HonsValue argSpec, @NotNull HonsValue args) throws EvalException;
}
