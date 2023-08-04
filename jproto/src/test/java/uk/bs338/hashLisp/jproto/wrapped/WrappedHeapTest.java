package uk.bs338.hashLisp.jproto.wrapped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

/* Just tests the interesting bits, not every delegation */
class WrappedHeapTest {
    HonsMachine machine;
    WrappedHeap wrap;
    
    @BeforeEach void setUp() {
        machine = new HonsMachine();
        wrap = WrappedHeap.wrap(machine);
    }
    
    @Test void wrappingTheSameHeapComparesEqual() {
        var wrap2 = WrappedHeap.wrap(machine);
        assertEquals(wrap, wrap2);
        assertEquals(wrap.hashCode(), wrap2.hashCode());
    }
    
    @Test void nilIsCached() {
        var nil1 = wrap.nil();
        var nil2 = wrap.nil();
        assertSame(nil1, nil2);
    }

    @Test void symbolTagIsCached() {
        var nil1 = wrap.symbolTag();
        var nil2 = wrap.symbolTag();
        assertSame(nil1, nil2);
    }
    
    @Test void unwrapChecksHeapMatchesOrThrowsException() {
        WrappedHeap wrap2 = WrappedHeap.wrap(new HonsMachine());
        var val1 = wrap.wrap(HonsValue.fromSmallInt(123));
        var val2 = wrap2.wrap(HonsValue.fromSmallInt(456));
        assertThrows(IllegalArgumentException.class, () -> wrap2.unwrap(val1));
        assertThrows(IllegalArgumentException.class, () -> wrap.unwrap(val2));
        assertEquals(123, wrap.unwrap(val1).toSmallInt());
        assertEquals(456, wrap2.unwrap(val2).toSmallInt());
    }
}