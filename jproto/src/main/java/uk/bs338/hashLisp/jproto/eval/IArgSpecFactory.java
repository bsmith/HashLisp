package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface IArgSpecFactory {
    @NotNull ArgSpec getByValue(@NotNull HonsValue argSpec) throws EvalException;
    @NotNull ArgSpec get(@NotNull IExpr argSpec) throws EvalException;

    @NotNull Assignments match(@NotNull IExpr argSpec, @NotNull IExpr args) throws EvalException;
}
