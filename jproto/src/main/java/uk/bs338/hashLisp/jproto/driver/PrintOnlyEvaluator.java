package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IValue;

public class PrintOnlyEvaluator<V extends IValue> implements IEvaluator<V> {
    private final @NotNull IHeap<V> heap;
    private final @NotNull V nil;
    private final @NotNull V ioPrintSym;
    private final @NotNull V quoteSym;

    public PrintOnlyEvaluator(@NotNull IHeap<V> heap) {
        this.heap = heap;
        nil = heap.nil();
        ioPrintSym = heap.makeSymbol("io-print!");
        quoteSym = heap.makeSymbol("quote");
    }

    @Override
    public @NotNull V eval_hnf(@NotNull V val) {
        /* quote evaluates to its argument, but doesn't evaluate itself */
        if (val.isConsRef() && heap.fst(val).equals(quoteSym))
            return val;
        /* wrap in (io-print! (quote <val>)) */
        var quote = heap.cons(quoteSym, heap.cons(val, nil));
        return heap.cons(ioPrintSym, heap.cons(quote, nil));
    }

    @Override
    public @NotNull V apply_hnf(@NotNull V val) {
        if (val.isConsRef() && heap.fst(val).equals(quoteSym))
            return heap.fst(heap.snd(val));
        return val;
    }
}
