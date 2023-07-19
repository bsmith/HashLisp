package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

public interface IWrappedSymbol2<V extends IValue, W extends IWrappedValue2<V, W>> extends IWrappedValue2<V, W> {
    @NotNull W symbolName();
    @NotNull String symbolNameAsString();
}
