package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import uk.bs338.hashLisp.jproto.driver.MemoEvalChecker;
import uk.bs338.hashLisp.jproto.hons.HeapValidationError;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.Utilities.*;

@TestInstance(Lifecycle.PER_CLASS)
public class LazyEvaluatorTest {
    HonsHeap heap;
    LazyEvaluator eval;

    /* share a heap with all tests */
    @BeforeAll
    void setUpHeap() {
        heap = new HonsHeap();
    }

    @AfterEach
    void validateHeap() {
        try {
            heap.validateHeap();
        }
        catch (HeapValidationError e) {
            /* If the heap didn't validate, we should get a new heap */
            heap = new HonsHeap();
            throw e;
        }
    }

    @BeforeEach
    void setUpEvaluator() {
        eval = new LazyEvaluator(heap);
    }

    @AfterEach
    void tearDownEvaluator() {
        try {
            MemoEvalChecker.checkHeap(heap, eval);
        }
        catch (HeapValidationError e) {
            heap = new HonsHeap();
            eval = null;
            throw e;
        }
        eval = null;
    }
    
    void assertEvalsTo(HonsValue expected, HonsValue program) {
        HonsValue rv;
        try {
            rv = eval.eval_one(program);
        }
        catch (Exception e) {
            System.err.println("Exception during eval_one of " + heap.valueToString(program) + " (e: " + e + ")");
            throw e;
        }
        assertEquals(expected, rv);
    }

    @Nested
    class ThingsAlreadyInNormalForm {
        @Test void nil() {
            var nil = heap.nil();
            assertEvalsTo(nil, nil);
        }

        @Test void smallInt() {
            var intval = heap.makeSmallInt(17);
            assertEvalsTo(intval, intval);
        }

        @Test void symbol() {
            var symval = heap.makeSymbol("example");
            assertEvalsTo(symval, symval);
        }

        @Test void string() {
            var val = heap.cons(heap.makeSymbol("*string"), stringAsList(heap, "example string"));
            assertEvalsTo(val, val);
        }
        
        @Test void hasDataHeadButContainsCode() {
            var code = makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            var val = heap.cons(heap.makeSymbol("*UNKNOWN"), heap.cons(code, heap.nil()));
            assertEvalsTo(val, val);
        }
        
        @Test void lambda() {
            var args = makeList(heap, heap.makeSymbol("a"), heap.makeSymbol("b"));
            var body = makeList(heap, heap.makeSymbol("add"), heap.makeSymbol("a"), heap.makeSymbol("b"));
            var lambda = makeList(heap, heap.makeSymbol("lambda"), args, body);
            var expected = makeList(heap, heap.makeSymbol("*lambda"), args, body);
            assertEvalsTo(expected, lambda);
        }
    }
    
    @Nested
    class ApplySimpleThingsNotLambdas {
        @Test void simplePrimitive() {
            var code = makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = heap.makeSmallInt(3);
            assertEvalsTo(expected, code);
        }
        
        @Test void unknownSymbolIsStrictDataConstructor() {
            var code = makeList(heap, heap.makeSymbol("UNKNOWN"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = makeList(heap, heap.makeSymbol("*UNKNOWN"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            assertEvalsTo(expected, code);
        }
        
        @Test void errorPrimitiveThrowsException() {
            var code = makeList(heap, heap.makeSymbol("error"));
            assertEvalsTo(code, code);
        }
    }
    
    @Nested
    class ApplyLambdas {
        HonsValue addLambda;
        
        @BeforeEach void setUp() {
            var args = makeList(heap, heap.makeSymbol("a"), heap.makeSymbol("b"));
            var body = makeList(heap, heap.makeSymbol("add"), heap.makeSymbol("a"), heap.makeSymbol("b"));
            addLambda = makeList(heap, heap.makeSymbol("lambda"), args, body);
        }
        
        @Test void applyLambda() {
            var code = makeList(heap, addLambda, heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = heap.makeSmallInt(3);
            assertEvalsTo(expected, code);
        }
        
        @Test void nestedLambdaUsingSameArgLetter() {
            var args = makeList(heap, heap.makeSymbol("a"), heap.makeSymbol("b"));
            var body = makeList(heap, addLambda, heap.makeSmallInt(3), heap.makeSmallInt(4));
            var lambda2 = makeList(heap, heap.makeSymbol("lambda"), args, body);
            var code = makeList(heap, lambda2, heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = heap.makeSmallInt(7);
            assertEvalsTo(expected, code);
        }
    }
}
