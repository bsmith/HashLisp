package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class LazyEvaluatorTest {
    HonsHeap heap;
    LazyEvaluator eval;
    
    /* share a heap with all tests */
    @BeforeAll void setUpHeap() {
        heap = new HonsHeap();
    }

    @BeforeEach void setUpEvaluator() {
        eval = new LazyEvaluator(heap);
    }
    
    @AfterEach void tearDownEvaluator() {
        eval = null;
    }
    
    @Test void nilEvalsToNil() {
        var nil = heap.nil();
        var rv = eval.eval(nil);
        assertEquals(nil, rv);
    }
    
    @Test void intEvalsToInt() {
        var intval = heap.makeSmallInt(17);
        var rv = eval.eval(intval);
        assertEquals(intval, rv);
    }
    
    @Test void symbolEvalsToSymbol() {
        var symval = heap.makeSymbol("example");
        var rv = eval.eval(symval);
        assertEquals(symval, rv);
    }
}
