package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestReporter;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class LazyEvaluatorTest {
    HonsHeap heap;
    LazyEvaluator eval;
    
    /* share a heap with all tests */
    @BeforeAll void setUpHeap(TestReporter testReporter) {
        testReporter.publishEntry("setUpHeap");
        heap = new HonsHeap();
    }

    @BeforeEach void setUpEvaluator() {
        System.out.println("setUpEvaluator");
        eval = new LazyEvaluator(heap);
    }
    @AfterEach void tearDownEvaluator() {
        eval = null;
    }

    @Test void testEval(TestReporter testReporter) {
        testReporter.publishEntry("testEval says Hello");
    }
    
    @Test void nilEvalsToNil() {
        var nil = heap.nil();
        var rv = eval.eval(nil);
        assertEquals(nil, rv);
    }
    
    @Test void intEvalsToInt() {
        var intval = heap.makeShortInt(17);
        var rv = eval.eval(intval);
        assertEquals(intval, rv);
    }
    
    @Test void symbolEvalsToSymbol() {
        var symval = heap.makeSymbol("example");
        var rv = eval.eval(symval);
        assertEquals(symval, rv);
    }
}
