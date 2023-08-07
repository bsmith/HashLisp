package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import uk.bs338.hashLisp.jproto.driver.MemoEvalChecker;
import uk.bs338.hashLisp.jproto.eval.LazyEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LispExamplesTest {
    HonsMachine machine;
    Reader reader;
    LazyEvaluator evaluator;
    
    @BeforeEach void setUp() {
        if (machine == null)
            machine = new HonsMachine();
        if (reader == null)
            reader = new Reader(machine, Tokeniser.getFactory(new CharClassifier()));
        evaluator = new LazyEvaluator(machine);
        evaluator.setDebug(true);
    }
    
    @AfterAll void dumpMachine() {
        machine.dumpMachine(System.out, true);
    }

    @AfterEach
    void validateHeap() {
        machine.getHeap().validateHeap();
        MemoEvalChecker.checkHeap(machine, evaluator);
    }
    
    void assertEval(@NotNull String expectedStr, @NotNull String programStr) {
        var expected = reader.read(expectedStr).getValue();
        var program = reader.read(programStr).getValue();
        var actual = evaluator.eval_one(program);
        assertEquals(machine.valueToString(expected), machine.valueToString(actual));
        assertEquals(expected, actual);
    }
    
    @Test void fstSndCons() {
        assertEval("*head", "(fst (cons *head *tail))");
        assertEval("*tail", "(snd (cons *head *tail))");
    }
    
    @Test void lambda() {
        assertEval("(*lambda (x) (add 1 x))", "(lambda (x) (add 1 x))");
        assertEval("1", "((lambda (x) (add 1 x)) 0)");
        assertEval("2", "((lambda (f) (f 1)) (lambda (x) (add 1 x)))");
    }
    
    @Test void nestedLambdas() {
        assertEval(
            "(*data 1 2)",
            "((lambda (x) (data ((lambda (x) x) 1) x)) 2)"
        );
    }
    
    @Test void laziness() {
        assertEval(
            "7",
            "((lambda (x y) y) (error \"lazy\") (add 3 4))"
        );
    }
    
    @Test void fibonacci() {
        assertEval(
            "120", 
            "((lambda (Y fib$) ((Y fib$) 5)) (lambda (f) ((lambda (x) (f (x x))) (lambda (x) (f (x x))))) (lambda (fib$$) (lambda (n) (zerop n 1 (mul n (fib$$ (add n -1)))))))"
        );
    }
    
    @Test void nameCaptureProblem() {
        assertEval(
            "(*data y z)",
            "(((lambda (x) (lambda (y) (data x y))) y) z)"
        );
    }
    
    @Test void nameCaptureProblemDeeper() {
        assertEval(
            "(*data (*data y y) z)",
            "(((lambda (x) (lambda (y) (data x y))) (data y y)) z))"
        );
    }
}
