package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

class ArgSpecTest {
    HonsHeap heap;
    ExprFactory exprFactory;
    ArgSpec argSpec;
    
    @BeforeEach
    void setUp() throws EvalException {
        heap = new HonsHeap();
        exprFactory = new ExprFactory(heap);
        argSpec = new ArgSpec(exprFactory, Utilities.makeListWithDot(heap,
            heap.makeSymbol("a"),
            heap.makeSymbol("b"),
            heap.makeSymbol("rest")));
    }
    
    @Test void fourArgsMatchAndFillsRest() {
        var args = Utilities.intList(heap, new int[]{1, 2, 3, 4});
        var expectedRest = heap.snd(heap.snd(args));
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(heap.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(heap.makeSymbol("b")));
        assertEquals(expectedRest, assignments.get(heap.makeSymbol("rest")));
    }
    
    @Test void twoArgsMatchAndNilRest() {
        var args = Utilities.intList(heap, new int[]{1, 2});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(heap.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(heap.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("rest")));
    }
    
    @Test void oneArgMatchesAndSecondIsNilAndNilRest() {
        var args = Utilities.intList(heap, new int[]{1});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(heap.makeSymbol("a")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("rest")));
    }
    
    @Test void noArgsAndAllAreNil() {
        var args = HonsValue.nil;
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("a")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("rest")));
    }
    
    @Test void hasToString() {
        String expected = "ArgSpec{argNames=[a, b], slurpyName=rest}";
        assertEquals(expected, argSpec.toString());
    }
}