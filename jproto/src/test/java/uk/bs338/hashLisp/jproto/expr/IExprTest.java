package uk.bs338.hashLisp.jproto.expr;

import org.junit.jupiter.api.*;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

class IExprTest {
    HonsMachine machine;
    IExpr nil, smallInt, sym, cons;
    
    @BeforeEach
    void setUp() {
        machine = new HonsMachine();
        nil = IExpr.wrap(machine, HonsValue.nil);
        smallInt = IExpr.wrap(machine, HonsValue.fromSmallInt(123));
        sym = IExpr.makeSymbol(machine, "symbol");
        cons = IExpr.wrap(machine, machine.cons(sym.getValue(), smallInt.getValue()));
    }
    
    @AfterEach
    void validateHeap() {
        machine.getHeap().validateHeap();
    }
    
    @Nested
    class Equals {
        @Test
        void equalsTheSameWrappedValue() {
            assertEquals(nil, IExpr.wrap(machine, HonsValue.nil));
            assertEquals(smallInt, IExpr.wrap(machine, HonsValue.fromSmallInt(123)));
            assertEquals(sym, IExpr.makeSymbol(machine, "symbol"));
            assertEquals(cons, IExpr.wrap(machine, machine.cons(sym.getValue(), smallInt.getValue())));
        }

        @Test
        void hashCodeTheSameForTheSameWrappedValue() {
            assertEquals(nil.hashCode(), IExpr.wrap(machine, HonsValue.nil).hashCode());
            assertEquals(smallInt.hashCode(), IExpr.wrap(machine, HonsValue.fromSmallInt(123)).hashCode());
            assertEquals(sym.hashCode(), IExpr.makeSymbol(machine, "symbol").hashCode());
            assertEquals(cons.hashCode(), IExpr.wrap(machine, machine.cons(sym.getValue(), smallInt.getValue())).hashCode());
        }
        
        @Test
        void notEqualsWhenHeapDifferent() {
            HonsMachine machine2 = new HonsMachine();
            assertNotEquals(nil, IExpr.wrap(machine2, HonsValue.nil));
            assertNotEquals(smallInt, IExpr.wrap(machine2, HonsValue.fromSmallInt(123)));
            assertNotEquals(sym, IExpr.wrap(machine2, machine2.makeSymbol("symbol")));
            assertNotEquals(cons, IExpr.wrap(machine2, machine2.cons(sym.getValue(), smallInt.getValue())));
        }

        @Test
        void differentHashCodeWhenHeapDifferent() {
            HonsMachine machine2 = new HonsMachine();
            assertNotEquals(nil.hashCode(), IExpr.wrap(machine2, HonsValue.nil).hashCode());
            assertNotEquals(smallInt.hashCode(), IExpr.wrap(machine2, HonsValue.fromSmallInt(123)).hashCode());
            assertNotEquals(sym.hashCode(), IExpr.wrap(machine2, machine2.makeSymbol("symbol")).hashCode());
            assertNotEquals(cons.hashCode(), IExpr.wrap(machine2, machine2.cons(sym.getValue(), smallInt.getValue())).hashCode());
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
            assertEquals("symbol", machine.symbolNameAsString(sym.getValue()));
        }

        @Test
        void cons() {
            assertEquals(ExprType.CONS, cons.getType());
            assertEquals(machine.cons(sym.getValue(), smallInt.getValue()), cons.getValue());
        }
    }
    
    @Nested
    class Symbols {
        @Test
        void symbolName() {
            assertInstanceOf(ISymbolExpr.class, sym);
            var symExpr = (ISymbolExpr)sym;
            assertEquals(Utilities.stringAsList(machine, "symbol"), symExpr.symbolName().getValue());
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