package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.IHeapVisitor;
import uk.bs338.hashLisp.jproto.Utilities;

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

    @Nested
    class HeapVisitor {
        class heapVisitor implements IHeapVisitor<HonsValue> {
            @Override
            public void visitNil(@NotNull HonsValue visited) {
                throw new AssertionError("visitNil shouldn't have been called");
            }

            @Override
            public void visitSmallInt(@NotNull HonsValue visited, int num) {
                throw new AssertionError("visitSmallInt shouldn't have been called");
            }

            @Override
            public void visitSymbol(@NotNull HonsValue visited, @NotNull HonsValue val) {
                throw new AssertionError("visitSymbol shouldn't have been called");
            }

            @Override
            public void visitCons(@NotNull HonsValue visited, @NotNull HonsValue fst, @NotNull HonsValue snd) {
                throw new AssertionError("visitCons shouldn't have been called");
            }
        }
        
        @Test void canVisitNil() {
            HonsValue value = HonsValue.nil;
            var visitor = new heapVisitor() {
                public boolean visitCalled = false;
                public void visitNil(@NotNull HonsValue visited) {
                    assertEquals(value, visited);
                    visitCalled = true;
                }
            };
            heap.visitValue(value, visitor);
            assertTrue(visitor.visitCalled);
        }
        
        @Test void canVisitSmallInt() {
            HonsValue value = HonsValue.fromSmallInt(123);
            var visitor = new heapVisitor() {
                public boolean visitCalled = false;
                public void visitSmallInt(@NotNull HonsValue visited, int num) {
                    assertEquals(value, visited);
                    assertEquals(123, num);
                    visitCalled = true;
                }
            };
            heap.visitValue(value, visitor);
            assertTrue(visitor.visitCalled);
        }
        
        @Test void canVisitSymbol() {
            var visitor = new heapVisitor() {
                public boolean visitCalled = false;
                public void visitSymbol(@NotNull HonsValue visited, @NotNull HonsValue val) {
                    assertEquals(sym, visited);
                    assertEquals(heap.symbolName(sym), val);
                    visitCalled = true;
                }
            };
            heap.visitValue(sym, visitor);
            assertTrue(visitor.visitCalled);
        }
        
        @Test void canVisitCons() {
            var visitor = new heapVisitor() {
                public boolean visitCalled = false;
                public void visitCons(@NotNull HonsValue visited, @NotNull HonsValue fst, @NotNull HonsValue snd) {
                    assertEquals(cons, visited);
                    assertEquals(one, fst);
                    assertEquals(two, snd);
                    visitCalled = true;
                }
            };
            heap.visitValue(cons, visitor);
            assertTrue(visitor.visitCalled);
        }
    }
}