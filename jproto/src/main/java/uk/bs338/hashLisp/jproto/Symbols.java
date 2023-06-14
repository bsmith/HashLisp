package uk.bs338.hashLisp.jproto;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

import javax.annotation.ParametersAreNonnullByDefault;

import static uk.bs338.hashLisp.jproto.Utilities.stringAsList;

@ParametersAreNonnullByDefault
public final class Symbols {
    private Symbols() {
        throw new AssertionError("No Symbols instances for you!");
    }
    
    public static HonsValue makeSymbol(IHeap heap, String name) throws Exception {
        return heap.cons(HonsValue.tagSymbol, stringAsList(heap, name));
    }
}
