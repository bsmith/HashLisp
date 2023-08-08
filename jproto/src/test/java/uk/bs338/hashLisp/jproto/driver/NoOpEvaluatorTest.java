package uk.bs338.hashLisp.jproto.driver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.Utilities.makeList;

class NoOpEvaluatorTest {
    HonsMachine machine;
    NoOpEvaluator<HonsValue> evaluator;
    
    @BeforeEach void setUp() {
        machine = new HonsMachine();
        evaluator = new NoOpEvaluator<>();
    }
    
    @Test
    void evaluateDoesNothing() {
        var program = makeList(machine, machine.makeSymbol("add"), machine.makeSmallInt(1), machine.makeSmallInt(2));
        var result = evaluator.evaluate(program);
        assertEquals(program, result);
    }
    
    @Test
    void evaluateWithDoesNothing() {
        var program = makeList(machine, machine.makeSymbol("add"), machine.makeSmallInt(1), machine.makeSmallInt(2));
        var result = evaluator.evaluateWith(Map.of(machine.makeSymbol("add"), machine.makeSmallInt(99)), program);
        assertEquals(program, result);
    }
}