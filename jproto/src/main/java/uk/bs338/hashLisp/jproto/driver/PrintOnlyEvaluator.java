package uk.bs338.hashLisp.jproto.driver;

import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public class PrintOnlyEvaluator implements IEvaluator<HonsValue> {
    private HonsHeap heap;
    private HonsValue ioPrintSym;

    public PrintOnlyEvaluator(HonsHeap heap) {
        this.heap = heap;
        ioPrintSym = heap.makeSymbol("io-print!");
    }

    @Override
    public HonsValue eval_one(HonsValue val) {
        /* wrap in (io-print! <val>) */
        return heap.cons(ioPrintSym, heap.cons(val, HonsValue.nil));
    }

    @Override
    public HonsValue eval_hnf(HonsValue val) {
        return val;
    }

    @Override
    public HonsValue apply(HonsValue head, HonsValue args) {
        return eval_one(heap.cons(head, args));
    }
}
