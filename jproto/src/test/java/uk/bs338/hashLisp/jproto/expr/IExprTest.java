package uk.bs338.hashLisp.jproto.expr;

import org.junit.jupiter.api.*;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

class IExprTest {
    HonsHeap heap;
    IExpr nil, smallInt, sym, cons;
    
    @BeforeEach
    void setUp() {
        heap = new HonsHeap();
        nil = IExpr.wrap(heap, HonsValue.nil);
        smallInt = IExpr.wrap(heap, HonsValue.fromSmallInt(123));
        sym = IExpr.wrap(heap, heap.makeSymbol("symbol"));
        cons = IExpr.wrap(heap, heap.cons(sym.getValue(), smallInt.getValue()));
    }
    
    @AfterEach
    void validateHeap() {
        heap.validateHeap();
    }
    
    @Nested
    class Equals {
        @Test
        void equalsTheSameWrappedValue() {
            assertEquals(nil, IExpr.wrap(heap, HonsValue.nil));
            assertEquals(smallInt, IExpr.wrap(heap, HonsValue.fromSmallInt(123)));
            assertEquals(sym, IExpr.wrap(heap, heap.makeSymbol("symbol")));
            assertEquals(cons, IExpr.wrap(heap, heap.cons(sym.getValue(), smallInt.getValue())));
        }

        @Test
        void hashCodeTheSameForTheSameWrappedValue() {
            assertEquals(nil.hashCode(), IExpr.wrap(heap, HonsValue.nil).hashCode());
            assertEquals(smallInt.hashCode(), IExpr.wrap(heap, HonsValue.fromSmallInt(123)).hashCode());
            assertEquals(sym.hashCode(), IExpr.wrap(heap, heap.makeSymbol("symbol")).hashCode());
            assertEquals(cons.hashCode(), IExpr.wrap(heap, heap.cons(sym.getValue(), smallInt.getValue())).hashCode());
        }
        
        @Test
        void notEqualsWhenHeapDifferent() {
            HonsHeap heap2 = new HonsHeap();
            assertNotEquals(nil, IExpr.wrap(heap2, HonsValue.nil));
            assertNotEquals(smallInt, IExpr.wrap(heap2, HonsValue.fromSmallInt(123)));
            assertNotEquals(sym, IExpr.wrap(heap2, heap2.makeSymbol("symbol")));
            assertNotEquals(cons, IExpr.wrap(heap2, heap2.cons(sym.getValue(), smallInt.getValue())));
        }

        @Test
        void differentHashCodeWhenHeapDifferent() {
            HonsHeap heap2 = new HonsHeap();
            assertNotEquals(nil.hashCode(), IExpr.wrap(heap2, HonsValue.nil).hashCode());
            assertNotEquals(smallInt.hashCode(), IExpr.wrap(heap2, HonsValue.fromSmallInt(123)).hashCode());
            assertNotEquals(sym.hashCode(), IExpr.wrap(heap2, heap2.makeSymbol("symbol")).hashCode());
            assertNotEquals(cons.hashCode(), IExpr.wrap(heap2, heap2.cons(sym.getValue(), smallInt.getValue())).hashCode());
        }
    }
    
    @Nested
    class Basics {
        @Test
        void nil() {
            assertEquals(ExprType.NIL, nil.getType());
            assertEquals(HonsValue.nil, nil.getValue());
        }

        @Test
        void smallInt() {
            assertEquals(ExprType.SMALL_INT, smallInt.getType());
            assertEquals(123, smallInt.getValue().toSmallInt());
        }

        @Test
        void symbol() {
            assertEquals(ExprType.SYMBOL, sym.getType());
            assertEquals("symbol", heap.symbolNameAsString(sym.getValue()));
        }

        @Test
        void cons() {
            assertEquals(ExprType.CONS, cons.getType());
            assertEquals(heap.cons(sym.getValue(), smallInt.getValue()), cons.getValue());
        }
    }
    
    @Nested
    class Symbols {
        @Test
        void symbolName() {
            assertInstanceOf(ISymbolExpr.class, sym);
            var symExpr = (ISymbolExpr)sym;
            assertEquals(Utilities.stringAsList(heap, "symbol"), symExpr.symbolName().getValue());
        }
        
        @Test
        void symbolNameAsString() {
            assertInstanceOf(ISymbolExpr.class, sym);
            var symExpr = (ISymbolExpr)sym;
            assertEquals("symbol", symExpr.symbolNameAsString());
        }
    }
    
    @Nested
    class Cons {
        @Test
        void fst() {
            assertInstanceOf(IConsExpr.class, cons);
            var consExpr = (IConsExpr)cons;
            assertEquals(sym.getValue(), consExpr.fst().getValue());
        }

        @Test
        void snd() {
            assertInstanceOf(IConsExpr.class, cons);
            var consExpr = (IConsExpr)cons;
            assertEquals(smallInt.getValue(), consExpr.snd().getValue());
        }
    }
    
    @Nested
    class Visitor {
        class MockVisitor implements IExprVisitor {
            String type;
            IExpr expr;
            
            @Override
            public void visitSimple(IExpr simpleExpr) {
                type = "simple";
                expr = simpleExpr;
            }

            @Override
            public void visitSymbol(ISymbolExpr symbolExpr) {
                type = "symbol";
                expr = symbolExpr;
            }

            @Override
            public void visitCons(IConsExpr consExpr) {
                type = "cons";
                expr = consExpr;
            }
        }
        
        MockVisitor visitor;
        
        @BeforeEach void setUp() {
            visitor = new MockVisitor();
        }
        
        @Test void nil() {
            nil.visit(visitor);
            assertEquals("simple", visitor.type);
            assertEquals(nil, visitor.expr);
        }

        @Test void smallInt() {
            smallInt.visit(visitor);
            assertEquals("simple", visitor.type);
            assertEquals(smallInt, visitor.expr);
        }

        @Test void sym() {
            sym.visit(visitor);
            assertEquals("symbol", visitor.type);
            assertEquals(sym, visitor.expr);
        }

        @Test void cons() {
            cons.visit(visitor);
            assertEquals("cons", visitor.type);
            assertEquals(cons, visitor.expr);
        }
    }
}