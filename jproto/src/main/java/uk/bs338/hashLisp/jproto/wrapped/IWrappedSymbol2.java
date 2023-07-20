package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

public interface IWrappedSymbol2 extends IWrappedValue2 {
    @NotNull IWrappedValue2 symbolName();
    @NotNull String symbolNameAsString();
}
