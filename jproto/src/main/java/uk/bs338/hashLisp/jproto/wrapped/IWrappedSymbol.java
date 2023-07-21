package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;

public interface IWrappedSymbol extends IWrappedValue {
    @NotNull IWrappedValue symbolName();
    @NotNull String symbolNameAsString();
}
