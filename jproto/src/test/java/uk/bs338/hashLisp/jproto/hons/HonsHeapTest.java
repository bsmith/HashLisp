package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HonsHeapTest {
    HonsHeap heap;
    HonsValue one, two, cons, sym;

    @BeforeEach void setUp() {
        heap = new HonsHeap();
        one = HonsValue.fromSmallInt(1);
        two = HonsValue.fromSmallInt(2);
        sym = heap.makeSymbol("sym");
        cons = heap.cons(one, two);
    }
    
    @Nested
    class memoEval {
        @Test void memoIsEmptyByDefault() {
            assertEquals(Optional.empty(), heap.getMemoEval(cons));
        }
        
        @Test void memoIsEmptyForNonCons() {
            assertEquals(Optional.empty(), heap.getMemoEval(one));
        }
        
        @Test void memoStoresSomething() {
            heap.setMemoEval(cons, one);
            assertEquals(Optional.of(one), heap.getMemoEval(cons));
        }
    }
}