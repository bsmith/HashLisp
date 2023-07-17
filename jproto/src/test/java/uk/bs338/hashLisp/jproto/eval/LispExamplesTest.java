package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LispExamplesTest {
    HonsHeap heap;
    Reader reader;
    LazyEvaluator evaluator;
    
    @BeforeEach void setUp() {
        if (heap == null)
            heap = new HonsHeap();
        if (reader == null)
            reader = new Reader(heap, Tokeniser.getFactory(new CharClassifier()));
        evaluator = new LazyEvaluator(heap);
        evaluator.setDebug(true);
    }
    
    @AfterAll void dumpHeap() {
        heap.dumpHeap(System.out, true);
    }
    
    void assertEval(@NotNull String expectedStr, @NotNull String programStr) {
        var expected = reader.read(expectedStr).getValue();
        var program = reader.read(programStr).getValue();
        var actual = evaluator.eval_one(program);
        assertEquals(heap.valueToString(expected), heap.valueToString(actual));
        assertEquals(expected, actual);
    }
    
    @Test void fstSndCons() {
        assertEval("1", "(fst (cons 1 2))");
        assertEval("2", "(snd (cons 1 2))");
    }
    
    @Test void lambda() {
        assertEval("(*lambda (x) (add 1 x))", "(lambda (x) (add 1 x))");
        assertEval("1", "((lambda (x) (add 1 x)) 0)");
        assertEval("2", "((lambda (f) (f 1)) (lambda (x) (add 1 x)))");
    }
    
    @Test void nestedLambdas() {
        assertEval(
            "(1 . 2)",
            "((lambda (x) (cons ((lambda (x) x) 1) x)) 2)"
        );
    }
    
    /* XXX not actually lazy! */
    @Test void laziness() {
        assertEval(
            "7",
            "((lambda (x y) y) (add 1 2) (add 3 4))"
        );
    }
    
    @Test void fibonacci() {
        assertEval(
            "120", 
            "((lambda (Y fib$) ((Y fib$) 5)) (lambda (f) ((lambda (x) (f (x x))) (lambda (x) (f (x x))))) (lambda (fib$$) (lambda (n) (zerop n 1 (mul n (fib$$ (add n -1)))))))"
        );
    }
}
