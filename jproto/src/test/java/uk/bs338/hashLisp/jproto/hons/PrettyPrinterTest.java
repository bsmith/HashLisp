package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.Utilities.*;

class PrettyPrinterTest {
    HonsHeap heap;
    PrettyPrinter<HonsValue> prettyPrinter;

    @BeforeEach
    void setUp() {
        heap = new HonsHeap();
        prettyPrinter = new PrettyPrinter<>(heap);
    }
    
    @Test void positiveSmallInt() {
        assertEquals("123", prettyPrinter.valueToString(HonsValue.fromSmallInt(123)));
    }
    
    @Test void negativeSmallInt() {
        assertEquals("-123", prettyPrinter.valueToString(HonsValue.fromSmallInt(-123)));
    }
    
    @Test void symbol() {
        assertEquals("symbol", prettyPrinter.valueToString(heap.makeSymbol("symbol")));
    }
    
    @Test void nil() {
        assertEquals("nil", prettyPrinter.valueToString(HonsValue.nil));
    }
    
    @Test void symbolTag() {
        assertEquals("#1:symbol", prettyPrinter.valueToString(HonsValue.symbolTag));
    }
    
    @Test void pair() {
        assertEquals("(1 . 2)",
            prettyPrinter.valueToString(heap.cons(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2))));
    }
    
    @Test void oneElementList() {
        assertEquals("(1)",
            prettyPrinter.valueToString(heap.cons(HonsValue.fromSmallInt(1), HonsValue.nil)));
    }
    
    @Test void twoElementList() {
        assertEquals("(1 2)",
            prettyPrinter.valueToString(intList(heap,new int[]{1, 2})));
    }
    
    @Test void dottedList() {
        assertEquals("(1 2 . 3)", 
            prettyPrinter.valueToString(makeListWithDot(heap, HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2), HonsValue.fromSmallInt(3))));
    }
    
    @Test void dottedListEndsInSymbol() {
        var val = heap.cons(HonsValue.fromSmallInt(1), heap.makeSymbol("abc"));
        assertEquals("(1 . abc)",
            prettyPrinter.valueToString(val));
    }
    
    @Test void nestedLists() {
        var val = makeList(heap,
            makeList(heap,
                heap.makeSymbol("lambda"),
                HonsValue.nil,
                HonsValue.fromSmallInt(123)
                ),
            HonsValue.fromSmallInt(456));
        assertEquals("((lambda nil 123) 456)",
            prettyPrinter.valueToString(val));
    }
    
    @Test void pairOfNil() {
        assertEquals("(nil)", prettyPrinter.valueToString(heap.cons(HonsValue.nil, HonsValue.nil)));
    }
    
    @Test void string() {
        assertEquals("\"\\\\\\\"abc\"", prettyPrinter.valueToString(heap.cons(heap.makeSymbol("*string"), stringAsList(heap, "\\\"abc"))));
    }
    
    @Test void stringWithMoreEscapes() {
        /* \t, \b, \n, \r, \f, \' */
        var input = "\t\b\n\r\f'";
        var expected = "\"\\t\\b\\n\\r\\f\\'\"";
        assertEquals(expected, prettyPrinter.valueToString(heap.cons(heap.makeSymbol("*string"), stringAsList(heap, input))));
    }
}