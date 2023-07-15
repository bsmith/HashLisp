package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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
        heap.validateHeap();
    }

    @BeforeEach
    void setUpEvaluator() {
        eval = new LazyEvaluator(heap);
    }

    @AfterEach
    void tearDownEvaluator() {
        eval = null;
    }

    @Nested
    class ThingsAlreadyInNormalForm {
        @Test void nil() {
            var nil = heap.nil();
            var rv = eval.eval_one(nil);
            assertEquals(nil, rv);
        }

        @Test void smallInt() {
            var intval = heap.makeSmallInt(17);
            var rv = eval.eval_one(intval);
            assertEquals(intval, rv);
        }

        @Test void symbol() {
            var symval = heap.makeSymbol("example");
            var rv = eval.eval_one(symval);
            assertEquals(symval, rv);
        }

        @Test void string() {
            var val = heap.cons(heap.makeSymbol("*string"), stringAsList(heap, "example string"));
            var rv = eval.eval_one(val);
            assertEquals(val, rv);
        }
        
        @Test void hasDataHeadButContainsCode() {
            var code = makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            var val = heap.cons(heap.makeSymbol("*UNKNOWN"), heap.cons(code, heap.nil()));
            var rv = eval.eval_one(val);
            assertEquals(val, rv);
        }
        
        @Test void lambda() {
            var args = makeList(heap, heap.makeSymbol("a"), heap.makeSymbol("b"));
            var body = makeList(heap, heap.makeSymbol("add"), heap.makeSymbol("a"), heap.makeSymbol("b"));
            var lambda = makeList(heap, heap.makeSymbol("lambda"), args, body); 
            var rv = eval.eval_one(lambda);
            assertEquals(lambda, rv);
        }
    }
    
    @Nested
    class applySimpleThingsNotLambdas {
        @Test void simplePrimitive() {
            var code = makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = heap.makeSmallInt(3);
            var rv = eval.eval_one(code);
            assertEquals(expected, rv);
        }
        
        @Test void unknownSymbolIsStrictDataConstructor() {
            var code = makeList(heap, heap.makeSymbol("UNKNOWN"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = makeList(heap, heap.makeSymbol("*UNKNOWN"), heap.makeSmallInt(1), heap.makeSmallInt(2));
            var rv = eval.eval_one(code);
            assertEquals(expected, rv);
        }
    }
    
    @Nested
    class applyLambdas {
        HonsValue addLambda;
        
        @BeforeEach void setUp() {
            var args = makeList(heap, heap.makeSymbol("a"), heap.makeSymbol("b"));
            var body = makeList(heap, heap.makeSymbol("add"), heap.makeSymbol("a"), heap.makeSymbol("b"));
            addLambda = makeList(heap, heap.makeSymbol("lambda"), args, body);
        }
        
        @Test void applyLambda() {
            var code = makeList(heap, addLambda, heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = heap.makeSmallInt(3);
            var rv = eval.eval_one(code);
            assertEquals(expected, rv);
        }
        
        @Test void nestedLambdaUsingSameArgLetter() {
            var args = makeList(heap, heap.makeSymbol("a"), heap.makeSymbol("b"));
            var body = makeList(heap, addLambda, heap.makeSmallInt(3), heap.makeSmallInt(4));
            var lambda2 = makeList(heap, heap.makeSymbol("lambda"), args, body);
            var code = makeList(heap, lambda2, heap.makeSmallInt(1), heap.makeSmallInt(2));
            var expected = heap.makeSmallInt(7);
            var rv = eval.eval_one(code);
            assertEquals(expected, rv);
        }
    }
}
