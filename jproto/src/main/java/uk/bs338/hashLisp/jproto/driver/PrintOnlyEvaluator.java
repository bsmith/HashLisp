package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IValue;

public class PrintOnlyEvaluator<V extends IValue> implements IEvaluator<V> {
    private final IHeap<V> heap;
    private final @NotNull V ioPrintSym;

    public PrintOnlyEvaluator(@NotNull IHeap<V> heap) {
        this.heap = heap;
        ioPrintSym = heap.makeSymbol("io-print!");
    }

    @Override
    public @NotNull V eval_hnf(@NotNull V val) {
        /* wrap in (io-print! <val>) */
        return heap.cons(ioPrintSym, heap.cons(val, heap.nil()));
    }

    @Override
    public @NotNull V apply_hnf(@NotNull V val) {
        return val;
    }
}
