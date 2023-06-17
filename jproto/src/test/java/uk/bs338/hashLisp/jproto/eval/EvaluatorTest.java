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
import static uk.bs338.hashLisp.jproto.Symbols.makeSymbol;

@TestInstance(Lifecycle.PER_CLASS)
public class EvaluatorTest {
    HonsHeap heap;
    Evaluator eval;
    
    /* share a heap with all tests */
    @BeforeAll void setUpHeap(TestReporter testReporter) {
        testReporter.publishEntry("setUpHeap");
        heap = new HonsHeap();
    }

    @BeforeEach void setUpEvaluator() throws Exception {
        System.out.println("setUpEvaluator");
        eval = new Evaluator(heap);
    }
    @AfterEach void tearDownEvaluator() {
        eval = null;
    }

    @Test void testEval(TestReporter testReporter) {
        testReporter.publishEntry("testEval says Hello");
    }
    
    @Test void nilEvalsToNil() throws Exception {
        var nil = heap.nil();
        var rv = eval.eval(nil);
        assertEquals(nil, rv);
    }
    
    @Test void intEvalsToInt() throws Exception {
        var intval = heap.makeShortInt(17);
        var rv = eval.eval(intval);
        assertEquals(intval, rv);
    }
    
    @Test void symbolEvalsToSymbol() throws Exception {
        var symval = makeSymbol(heap, "example");
        var rv = eval.eval(symval);
        assertEquals(symval, rv);
    }
}
