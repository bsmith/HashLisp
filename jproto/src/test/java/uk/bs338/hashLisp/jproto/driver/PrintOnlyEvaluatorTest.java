package uk.bs338.hashLisp.jproto.driver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.wrapped.WrappedHeap;
import uk.bs338.hashLisp.jproto.wrapped.WrappedValue;

import static org.junit.jupiter.api.Assertions.*;

class PrintOnlyEvaluatorTest {
    WrappedHeap heap;
    PrintOnlyEvaluator evaluator;
    WrappedValue exampleValue;
    WrappedValue wrappedExampleValue;
    
    @BeforeEach void setUp() {
        var honsHeap = new HonsHeap();
        heap = WrappedHeap.wrap(honsHeap);
        evaluator = new PrintOnlyEvaluator(honsHeap);
        exampleValue = Utilities.makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
        wrappedExampleValue = Utilities.makeList(heap, heap.makeSymbol("io-print!"), exampleValue);
    }

    @Test
    void eval_one() {
        var retval = heap.wrap(evaluator.eval_one(exampleValue.getValue()));
        assertEquals(wrappedExampleValue, retval);
    }

    @Test
    void eval_hnf() {
        var retval = heap.wrap(evaluator.eval_hnf(exampleValue.getValue()));
        assertEquals(exampleValue, retval);
    }

    @Test
    void apply() {
        var head = heap.makeSymbol("apply");
        var retval = heap.wrap(evaluator.apply(head.getValue(), exampleValue.getValue()));
        assertEquals(Utilities.makeList(heap, heap.makeSymbol("io-print!"),
                heap.cons(head, exampleValue)),
            retval);
    }
}