package uk.bs338.hashLisp.jproto.wrapped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

class WrappedValueTest {
    HonsMachine machine;
    
    @BeforeEach void setUp() {
        machine = new HonsMachine();
    }
    
    @Test void canWrapHeapAndValue() {
        var val = HonsValue.fromSmallInt(123);
        var wrap = WrappedValue.wrap(machine, val);
        assertEquals(machine, wrap.getMachine());
        assertEquals(val, wrap.getValue());
    }
    
    @Test void unConsWrapsParts() {
        var val1 = HonsValue.fromSmallInt(123);
        var val2 = HonsValue.fromSmallInt(456);
        var cons = WrappedValue.wrap(machine, machine.cons(val1, val2));
        var pair = cons.uncons();
        assertEquals(val1, pair.fst().getValue());
        assertEquals(val2, pair.snd().getValue());
        assertEquals(machine, pair.fst().getMachine());
        assertEquals(machine, pair.snd().getMachine());
    }
    
    @Test void convertsValuesToStrings() {
        var val1 = HonsValue.fromSmallInt(123);
        var val2 = HonsValue.fromSmallInt(456);
        var cons = WrappedValue.wrap(machine, machine.cons(val1, val2));
        assertEquals("(123 . 456)", cons.toString());
    }
    
    @Test void wrappingTheSameThingsComparesEqual() {
        var val = HonsValue.fromSmallInt(123);
        var wrap = WrappedValue.wrap(machine, val);
        var wrap2 = WrappedValue.wrap(machine, val);
        assertEquals(wrap, wrap2);
        assertEquals(wrap.hashCode(), wrap2.hashCode());
    }
}