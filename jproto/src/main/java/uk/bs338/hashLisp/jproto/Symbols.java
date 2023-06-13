package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;

import static uk.bs338.hashLisp.jproto.Utilities.intList;

@ParametersAreNonnullByDefault
public final class Symbols {
    private Symbols() {
        throw new AssertionError("No Symbols instances for you!");
    }
    
    public static LispValue makeSymbol(IHeap heap, String name) throws Exception {
//        var list = name.codePoints().mapToObj(ch -> LispValue.fromShortInt(ch)).toArray();
        return heap.hons(LispValue.tagSymbol, intList(heap, name.codePoints().toArray()));
    }
}
