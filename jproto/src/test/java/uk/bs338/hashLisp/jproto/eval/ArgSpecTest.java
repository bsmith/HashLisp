package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.expr.ExprUtilities;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArgSpecTest {
    HonsHeap heap;
    ExprFactory exprFactory;
    ArgSpec argSpec;
    
    @BeforeEach
    void setUp() throws EvalException {
        heap = new HonsHeap();
        exprFactory = new ExprFactory(heap);
        argSpec = new ArgSpec(exprFactory, ExprUtilities.makeListWithDot(exprFactory,
            List.of(exprFactory.makeSymbol("a"),
            exprFactory.makeSymbol("b"),
            exprFactory.makeSymbol("rest"))));
    }
    
    @Test void fourArgsMatchAndFillsRest() {
        var args = ExprUtilities.intList(exprFactory, new int[]{1, 2, 3, 4});
        var expectedRest = args.asCons().snd().asCons().snd();
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(heap.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(heap.makeSymbol("b")));
        assertEquals(expectedRest.getValue(), assignments.get(heap.makeSymbol("rest")));
    }
    
    @Test void twoArgsMatchAndNilRest() {
        var args = ExprUtilities.intList(exprFactory, new int[]{1, 2});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(heap.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(heap.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("rest")));
    }
    
    @Test void oneArgMatchesAndSecondIsNilAndNilRest() {
        var args = ExprUtilities.intList(exprFactory, new int[]{1});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(heap.makeSymbol("a")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(heap.makeSymbol("rest")));
    }
    
    @Test void noArgsAndAllAreNil() {
        var args = IExpr.nil;
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