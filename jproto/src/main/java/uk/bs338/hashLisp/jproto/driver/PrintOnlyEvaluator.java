package uk.bs338.hashLisp.jproto.driver;

import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IValue;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public class PrintOnlyEvaluator<V extends IValue> implements IEvaluator<V> {
    private final IHeap<V> heap;
    private final V ioPrintSym;

    public PrintOnlyEvaluator(IHeap<V> heap) {
        this.heap = heap;
        ioPrintSym = heap.makeSymbol("io-print!");
    }

    @Override
    public V eval_one(V val) {
        /* wrap in (io-print! <val>) */
        return heap.cons(ioPrintSym, heap.cons(val, heap.nil()));
    }

    @Override
    public V eval_hnf(V val) {
        return val;
    }

    @Override
    public V apply(V head, V args) {
        return eval_one(heap.cons(head, args));
    }
}
