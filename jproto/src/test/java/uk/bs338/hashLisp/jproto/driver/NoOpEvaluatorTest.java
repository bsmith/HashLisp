package uk.bs338.hashLisp.jproto.driver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;

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
    void eval_oneDoesNothing() {
        var program = makeList(machine, machine.makeSymbol("add"), machine.makeSmallInt(1), machine.makeSmallInt(2));
        var result = evaluator.eval_one(program);
        assertEquals(program, result);
    }
    
    @Test
    void eval_multi_inplaceDoesNothing() {
        var program = makeList(machine, machine.makeSymbol("add"), machine.makeSmallInt(1), machine.makeSmallInt(2));
        var list = new ArrayList<HonsValue>();
        for (int i = 0; i < 3; i++)
            list.add(program);
        var result = evaluator.eval_multi_inplace(list);
        assertEquals(list, result);
    }
}