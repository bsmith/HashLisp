package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.PrettyPrinter;

public interface IMachine<V extends IValue> extends IHeap<V>, ISymbols<V>, IValueFactory<V> {
    default @NotNull String valueToString(V val) {
        return PrettyPrinter.valueToString(this, val);
    }
}
