package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

public interface IExprVisitor<V extends IValue, R> {
    /* nil and small int */
    @NotNull R visitConstant(@NotNull V visited);
    @NotNull R visitSymbol(@NotNull V visited);
    @NotNull R visitLambda(@NotNull V visited, @NotNull V argSpec, @NotNull V body);
    @NotNull R visitApply(@NotNull V visited, @NotNull V head, @NotNull V args);
}
