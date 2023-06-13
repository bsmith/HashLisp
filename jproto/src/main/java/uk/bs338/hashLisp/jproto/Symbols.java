package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;

import static uk.bs338.hashLisp.jproto.Utilities.intList;
import static uk.bs338.hashLisp.jproto.Utilities.stringAsList;

@ParametersAreNonnullByDefault
public final class Symbols {
    private Symbols() {
        throw new AssertionError("No Symbols instances for you!");
    }
    
    public static LispValue makeSymbol(IHeap heap, String name) throws Exception {
        return heap.hons(LispValue.tagSymbol, stringAsList(heap, name));
    }
}
