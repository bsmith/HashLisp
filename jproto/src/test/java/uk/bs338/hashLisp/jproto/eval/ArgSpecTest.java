package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.expr.ExprUtilities;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArgSpecTest {
    HonsMachine machine;
    ArgSpec argSpec;
    
    @BeforeEach
    void setUp() throws EvalException {
        machine = new HonsMachine();
        argSpec = new ArgSpec(machine, ExprUtilities.makeListWithDot(List.of(
            IExpr.makeSymbol(machine, "a"),
            IExpr.makeSymbol(machine, "b"),
            IExpr.makeSymbol(machine, "rest"))));
    }
    
    @Test void fourArgsMatchAndFillsRest() {
        var args = ExprUtilities.intList(machine, new int[]{1, 2, 3, 4});
        var expectedRest = args.asConsExpr().snd().asConsExpr().snd();
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(machine.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(machine.makeSymbol("b")));
        assertEquals(expectedRest.getValue(), assignments.get(machine.makeSymbol("rest")));
    }
    
    @Test void twoArgsMatchAndNilRest() {
        var args = ExprUtilities.intList(machine, new int[]{1, 2});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(machine.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(machine.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("rest")));
    }
    
    @Test void oneArgMatchesAndSecondIsNilAndNilRest() {
        var args = ExprUtilities.intList(machine, new int[]{1});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(machine.makeSymbol("a")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("rest")));
    }
    
    @Test void noArgsAndAllAreNil() {
        var args = IExpr.nil(machine);
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("a")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("rest")));
    }
    
    @Test void hasToString() {
        String expected = "ArgSpec{argNames=[a, b], slurpyName=rest}";
        assertEquals(expected, argSpec.toString());
    }
}