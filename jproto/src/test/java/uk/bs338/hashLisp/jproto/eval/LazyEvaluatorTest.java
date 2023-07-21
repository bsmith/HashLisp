package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import uk.bs338.hashLisp.jproto.driver.MemoEvalChecker;
import uk.bs338.hashLisp.jproto.hons.HeapValidationError;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.Utilities.*;

@TestInstance(Lifecycle.PER_CLASS)
public class LazyEvaluatorTest {
    HonsMachine machine;
    LazyEvaluator eval;

    /* share a heap with all tests */
    @BeforeAll
    void setUpHeap() {
        machine = new HonsMachine();
    }

    @AfterEach
    void validateHeap() {
        try {
            machine.getHeap().validateHeap();
        }
        catch (HeapValidationError e) {
            /* If the heap didn't validate, we should get a new heap */
            machine = new HonsMachine();
            throw e;
        }
    }

    @BeforeEach
    void setUpEvaluator() {
        eval = new LazyEvaluator(machine);
    }

    @AfterEach
    void tearDownEvaluator() {
        try {
            MemoEvalChecker.checkHeap(machine, eval);
        }
        catch (HeapValidationError e) {
            machine = new HonsMachine();
            eval = null;
            throw e;
        }
        eval = null;
    }
    
    void assertEvalsTo(HonsValue expected, HonsValue program) {
        HonsValue rv;
        try {
            rv = eval.evaluate(program);
        }
        catch (Exception e) {
            System.err.println("Exception during eval_one of " + machine.valueToString(program) + " (e: " + e + ")");
            throw e;
        }
        assertEquals(expected, rv);
    }

    @Nested
    class ThingsAlreadyInNormalForm {
        @Test void nil() {
            var nil = machine.nil();
            assertEvalsTo(nil, nil);
        }

        @Test void smallInt() {
            var intval = machine.makeSmallInt(17);
            assertEvalsTo(intval, intval);
        }

        @Test void symbol() {
            var symval = machine.makeSymbol("example");
            assertEvalsTo(symval, symval);
        }

        @Test void string() {
            var val = machine.cons(machine.makeSymbol("*string"), stringAsList(machine, "example string"));
            assertEvalsTo(val, val);
        }
        
        @Test void hasDataHeadButContainsCode() {
            var code = makeList(machine, machine.makeSymbol("add"), machine.makeSmallInt(1), machine.makeSmallInt(2));
            var val = machine.cons(machine.makeSymbol("*UNKNOWN"), machine.cons(code, machine.nil()));
            assertEvalsTo(val, val);
        }
        
        @Test void lambda() {
            var args = makeList(machine, machine.makeSymbol("a"), machine.makeSymbol("b"));
            var body = makeList(machine, machine.makeSymbol("add"), machine.makeSymbol("a"), machine.makeSymbol("b"));
            var lambda = makeList(machine, machine.makeSymbol("lambda"), args, body);
            var expected = makeList(machine, machine.makeSymbol("*lambda"), args, body);
            assertEvalsTo(expected, lambda);
        }
    }
    
    @Nested
    class ApplySimpleThingsNotLambdas {
        @Test void simplePrimitive() {
            var code = makeList(machine, machine.makeSymbol("add"), machine.makeSmallInt(1), machine.makeSmallInt(2));
            var expected = machine.makeSmallInt(3);
            assertEvalsTo(expected, code);
        }
        
        @Test void unknownSymbolIsStrictDataConstructor() {
            var code = makeList(machine, machine.makeSymbol("UNKNOWN"), machine.makeSmallInt(1), machine.makeSmallInt(2));
            var expected = makeList(machine, machine.makeSymbol("*UNKNOWN"), machine.makeSmallInt(1), machine.makeSmallInt(2));
            assertEvalsTo(expected, code);
        }
        
        @Test void errorPrimitiveThrowsException() {
            var code = makeList(machine, machine.makeSymbol("error"));
            assertEvalsTo(code, code);
        }
    }
    
    @Nested
    class ApplyLambdas {
        HonsValue addLambda;
        
        @BeforeEach void setUp() {
            var args = makeList(machine, machine.makeSymbol("a"), machine.makeSymbol("b"));
            var body = makeList(machine, machine.makeSymbol("add"), machine.makeSymbol("a"), machine.makeSymbol("b"));
            addLambda = makeList(machine, machine.makeSymbol("lambda"), args, body);
        }
        
        @Test void applyLambda() {
            var code = makeList(machine, addLambda, machine.makeSmallInt(1), machine.makeSmallInt(2));
            var expected = machine.makeSmallInt(3);
            assertEvalsTo(expected, code);
        }
        
        @Test void nestedLambdaUsingSameArgLetter() {
            var args = makeList(machine, machine.makeSymbol("a"), machine.makeSymbol("b"));
            var body = makeList(machine, addLambda, machine.makeSmallInt(3), machine.makeSmallInt(4));
            var lambda2 = makeList(machine, machine.makeSymbol("lambda"), args, body);
            var code = makeList(machine, lambda2, machine.makeSmallInt(1), machine.makeSmallInt(2));
            var expected = machine.makeSmallInt(7);
            assertEvalsTo(expected, code);
        }
    }
}
