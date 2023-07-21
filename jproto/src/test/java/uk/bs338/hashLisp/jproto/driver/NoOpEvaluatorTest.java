package uk.bs338.hashLisp.jproto.driver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.Utilities.makeList;

class NoOpEvaluatorTest {
    HonsHeap heap;
    NoOpEvaluator<HonsValue> evaluator;
    
    @BeforeEach void setUp() {
        heap = new HonsHeap();
        evaluator = new NoOpEvaluator<>();
    }
    
    @Test
    void evaluateDoesNothing() {
        var program = makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
        var result = evaluator.evaluate(program);
        assertEquals(program, result);
    }
    
    @Test
    void evaluateWithDoesNothing() {
        var program = makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
        var result = evaluator.evaluateWith(Map.of(heap.makeSymbol("add"), heap.makeSmallInt(99)), program);
        assertEquals(program, result);
    }
}