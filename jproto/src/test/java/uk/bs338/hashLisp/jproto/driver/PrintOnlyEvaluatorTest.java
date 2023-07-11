package uk.bs338.hashLisp.jproto.driver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.IValue;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.wrapped.WrappedHeap;
import uk.bs338.hashLisp.jproto.wrapped.WrappedValue;

import static org.junit.jupiter.api.Assertions.*;

class PrintOnlyEvaluatorTest {
    WrappedHeap heap;
    PrintOnlyEvaluator<WrappedValue> evaluator;
    WrappedValue exampleValue;
    WrappedValue wrappedExampleValue;
    
    @BeforeEach void setUp() {
        heap = WrappedHeap.wrap(new HonsHeap());
        evaluator = new PrintOnlyEvaluator<>(heap);
        exampleValue = Utilities.makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
        wrappedExampleValue = Utilities.makeList(heap, heap.makeSymbol("io-print!"),
            Utilities.makeList(heap, heap.makeSymbol("quote"), exampleValue));
    }

    @Test
    void eval_one() {
        var retval = evaluator.eval_one(exampleValue);
        assertEquals(wrappedExampleValue, retval);
    }

    @Test
    void eval_hnf() {
        var retval = evaluator.eval_hnf(exampleValue);
        assertEquals(wrappedExampleValue, retval);
    }

    @Test
    void apply() {
        var retval = evaluator.apply_hnf(evaluator.eval_hnf(exampleValue));
        assertEquals(wrappedExampleValue, retval);
    }
}