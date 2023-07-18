package uk.bs338.hashLisp.jproto.eval.expr;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

class ExprFactoryTest {
    HonsHeap heap;
    ExprFactory factory;
    IExpr nil, smallInt, sym, cons;
    
    @BeforeEach
    void setUp() {
        heap = new HonsHeap();
        factory = new ExprFactory(heap);
        nil = factory.wrap(HonsValue.nil);
        smallInt = factory.wrap(HonsValue.fromSmallInt(123));
        sym = factory.wrap(heap.makeSymbol("symbol"));
        cons = factory.wrap(heap.cons(sym.getValue(), smallInt.getValue()));
    }
    
    @AfterEach
    void validateHeap() {
        heap.validateHeap();
    }
    
    @Nested
    class Equals {
        @Test
        void equalsSameFactory() {
            assertEquals(nil, factory.wrap(HonsValue.nil));
            assertEquals(smallInt, factory.wrap(HonsValue.fromSmallInt(123)));
            assertEquals(sym, factory.wrap(heap.makeSymbol("symbol")));
            assertEquals(cons, factory.wrap(heap.cons(sym.getValue(), smallInt.getValue())));
        }
        
        @Test
        void equalsDifferentFactory() {
            var factory2 = new ExprFactory(heap);
            assertEquals(factory2, factory);
            assertEquals(nil, factory2.wrap(HonsValue.nil));
            assertEquals(smallInt, factory2.wrap(HonsValue.fromSmallInt(123)));
            assertEquals(sym, factory2.wrap(heap.makeSymbol("symbol")));
            assertEquals(cons, factory2.wrap(heap.cons(sym.getValue(), smallInt.getValue())));
        }

        @Test
        void hashCodeSameFactory() {
            assertEquals(nil.hashCode(), factory.wrap(HonsValue.nil).hashCode());
            assertEquals(smallInt.hashCode(), factory.wrap(HonsValue.fromSmallInt(123)).hashCode());
            assertEquals(sym.hashCode(), factory.wrap(heap.makeSymbol("symbol")).hashCode());
            assertEquals(cons.hashCode(), factory.wrap(heap.cons(sym.getValue(), smallInt.getValue())).hashCode());
        }

        @Test
        void hashCodeDifferentFactory() {
            var factory2 = new ExprFactory(heap);
            assertEquals(factory2.hashCode(), factory.hashCode());
            assertEquals(nil.hashCode(), factory2.wrap(HonsValue.nil).hashCode());
            assertEquals(smallInt.hashCode(), factory2.wrap(HonsValue.fromSmallInt(123)).hashCode());
            assertEquals(sym.hashCode(), factory2.wrap(heap.makeSymbol("symbol")).hashCode());
            assertEquals(cons.hashCode(), factory2.wrap(heap.cons(sym.getValue(), smallInt.getValue())).hashCode());
        }
    }
    
    @Nested
    class Basics {
        @Test
        void nil() {
            assertTrue(nil.isSimple());
            assertFalse(nil.isSymbol());
            assertFalse(nil.isCons());
            assertEquals(HonsValue.nil, nil.getValue());
        }

        @Test
        void smallInt() {
            assertTrue(smallInt.isSimple());
            assertFalse(smallInt.isSymbol());
            assertFalse(smallInt.isCons());
            assertEquals(123, smallInt.getValue().toSmallInt());
        }

        @Test
        void symbol() {
            assertTrue(sym.isSimple());
            assertTrue(sym.isSymbol());
            assertFalse(sym.isCons());
            assertEquals("symbol", heap.symbolNameAsString(sym.getValue()));
        }

        @Test
        void cons() {
            assertFalse(cons.isSimple());
            assertFalse(cons.isSymbol());
            assertTrue(cons.isCons());
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
        class MockVisitor implements IExprVisitor2 {
            String type;
            IExpr expr;
            
            @Override
            public void visitSimple(ISimpleExpr simpleExpr) {
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