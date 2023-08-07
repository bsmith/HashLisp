package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.Utilities;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HonsHeapTest {
    HonsHeap heap;
    HonsValue one, two, cons;

    @BeforeEach void setUp() {
        heap = new HonsHeap(8);
        one = HonsValue.fromSmallInt(1);
        two = HonsValue.fromSmallInt(2);
        cons = heap.cons(one, two);
    }

    @AfterEach void validateHeap() {
        heap.validateHeap();
    }
    
    @Test void canGetCell() {
        var cell = heap.getCell(cons);
        assertNotNull(cell);
        assertEquals(one, cell.getFst());
        assertEquals(two, cell.getSnd());
    }
    
    @Test void canHandleHundredCells() {
        var tail = HonsValue.nil;
        for (var i = 1; i <= 100; i++) {
            tail = heap.cons(HonsValue.fromSmallInt(i), tail);
        }
        assertEquals(HonsValue.fromSmallInt(5050), Utilities.sumList(heap, tail));
    }
    
    @Test void checkForcedCollision() {
        /* XXX not complete */
        var cons2 = heap.cons(one, HonsValue.fromSmallInt(3));
        var k = cons2.toObjectHash() - cons.toObjectHash();
        System.out.println(cons);
        System.out.println(k);
        var cons3 = heap.cons(one, HonsValue.fromSmallInt(-cons.toObjectHash()/k));
        System.out.println(cons3);
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