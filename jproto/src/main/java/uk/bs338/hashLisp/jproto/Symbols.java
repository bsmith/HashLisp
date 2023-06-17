package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;

import static uk.bs338.hashLisp.jproto.Utilities.*;

@ParametersAreNonnullByDefault
public final class Symbols {
    private Symbols() {
        throw new AssertionError("No Symbols instances for you!");
    }
    
    public static <V extends IValue> V makeSymbol(IHeap<V> heap, String name) throws Exception {
        return heap.makeSymbol(stringAsList(heap, name));
    }
    
    public static <V extends IValue> String symbolNameAsString(IHeap<V> heap, V symbol) throws Exception {
        return listAsString(heap, heap.symbolName(symbol));
    }
}
