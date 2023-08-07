package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

class IHeapVisitorTest {
    HonsMachine machine;
    HonsValue one, two, cons, sym;

    @BeforeEach
    void setUp() {
        machine = new HonsMachine();
        one = HonsValue.fromSmallInt(1);
        two = HonsValue.fromSmallInt(2);
        sym = machine.makeSymbol("sym");
        cons = machine.cons(one, two);
    }

        static class HeapVisitor implements IHeapVisitor<HonsValue> {
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
            var visitor = new HeapVisitor() {
                public boolean visitCalled = false;
                public void visitNil(@NotNull HonsValue visited) {
                    assertEquals(value, visited);
                    visitCalled = true;
                }
            };
            visitor.visitValue(machine, value);
            assertTrue(visitor.visitCalled);
        }

        @Test void canVisitSmallInt() {
            HonsValue value = HonsValue.fromSmallInt(123);
            var visitor = new HeapVisitor() {
                public boolean visitCalled = false;
                public void visitSmallInt(@NotNull HonsValue visited, int num) {
                    assertEquals(value, visited);
                    assertEquals(123, num);
                    visitCalled = true;
                }
            };
            visitor.visitValue(machine, value);
            assertTrue(visitor.visitCalled);
        }

        @Test void canVisitSymbol() {
            var visitor = new HeapVisitor() {
                public boolean visitCalled = false;
                public void visitSymbol(@NotNull HonsValue visited, @NotNull HonsValue val) {
                    assertEquals(sym, visited);
                    assertEquals(machine.symbolName(sym), val);
                    visitCalled = true;
                }
            };
            visitor.visitValue(machine, sym);
            assertTrue(visitor.visitCalled);
        }

        @Test void canVisitCons() {
            var visitor = new HeapVisitor() {
                public boolean visitCalled = false;
                public void visitCons(@NotNull HonsValue visited, @NotNull HonsValue fst, @NotNull HonsValue snd) {
                    assertEquals(cons, visited);
                    assertEquals(one, fst);
                    assertEquals(two, snd);
                    visitCalled = true;
                }
            };
            visitor.visitValue(machine, cons);
            assertTrue(visitor.visitCalled);
        }
}