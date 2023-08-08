package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

class ArgSpecTest {
    HonsMachine machine;
    ArgSpec argSpec;
    
    @BeforeEach
    void setUp() throws EvalException {
        machine = new HonsMachine();
        argSpec = ArgSpec.parse(machine, Utilities.makeListWithDot(machine,
            machine.makeSymbol("a"),
            machine.makeSymbol("b"),
            machine.makeSymbol("rest")));
    }
    
    @Test void fourArgsMatchAndFillsRest() {
        var args = Utilities.intList(machine, new int[]{1, 2, 3, 4});
        var expectedRest = machine.snd(machine.snd(args));
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(machine.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(machine.makeSymbol("b")));
        assertEquals(expectedRest, assignments.get(machine.makeSymbol("rest")));
    }
    
    @Test void twoArgsMatchAndNilRest() {
        var args = Utilities.intList(machine, new int[]{1, 2});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(machine.makeSymbol("a")));
        assertEquals(HonsValue.fromSmallInt(2), assignments.get(machine.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("rest")));
    }
    
    @Test void oneArgMatchesAndSecondIsNilAndNilRest() {
        var args = Utilities.intList(machine, new int[]{1});
        var assignments = argSpec.match(args);
        assertEquals(HonsValue.fromSmallInt(1), assignments.get(machine.makeSymbol("a")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("b")));
        assertEquals(HonsValue.nil, assignments.get(machine.makeSymbol("rest")));
    }
    
    @Test void noArgsAndAllAreNil() {
        var args = HonsValue.nil;
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