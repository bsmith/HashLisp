package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;

import static uk.bs338.hashLisp.jproto.Utilities.*;

@ParametersAreNonnullByDefault
public final class Symbols {
    private Symbols() {
        throw new AssertionError("No Symbols instances for you!");
    }
    
    public static <V extends IValue> V makeSymbol(IHeap<V> heap, String name) {
        return heap.makeSymbol(stringAsList(heap, name));
    }
    
    public static <V extends IValue> String symbolName(IHeap<V> heap, V symbol) {
        return listAsString(heap, heap.symbolName(symbol));
    }
}
