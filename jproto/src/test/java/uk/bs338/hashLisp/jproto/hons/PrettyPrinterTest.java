package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.Utilities.*;

class PrettyPrinterTest {
    HonsMachine machine;
    PrettyPrinter<HonsValue> prettyPrinter;

    @BeforeEach
    void setUp() {
        machine = new HonsMachine();
        prettyPrinter = new PrettyPrinter<>(machine);
    }
    
    @Test void positiveSmallInt() {
        assertEquals("123", prettyPrinter.valueToString(HonsValue.fromSmallInt(123)));
    }
    
    @Test void negativeSmallInt() {
        assertEquals("-123", prettyPrinter.valueToString(HonsValue.fromSmallInt(-123)));
    }
    
    @Test void symbol() {
        assertEquals("symbol", prettyPrinter.valueToString(machine.makeSymbol("symbol")));
    }
    
    @Test void nil() {
        assertEquals("nil", prettyPrinter.valueToString(HonsValue.nil));
    }
    
    @Test void symbolTag() {
        assertEquals("#1:symbol", prettyPrinter.valueToString(HonsValue.symbolTag));
    }
    
    @Test void pair() {
        assertEquals("(1 . 2)",
            prettyPrinter.valueToString(machine.cons(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2))));
    }
    
    @Test void oneElementList() {
        assertEquals("(1)",
            prettyPrinter.valueToString(machine.cons(HonsValue.fromSmallInt(1), HonsValue.nil)));
    }
    
    @Test void twoElementList() {
        assertEquals("(1 2)",
            prettyPrinter.valueToString(intList(machine,new int[]{1, 2})));
    }
    
    @Test void dottedList() {
        assertEquals("(1 2 . 3)", 
            prettyPrinter.valueToString(makeListWithDot(machine, HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2), HonsValue.fromSmallInt(3))));
    }
    
    @Test void dottedListEndsInSymbol() {
        var val = machine.cons(HonsValue.fromSmallInt(1), machine.makeSymbol("abc"));
        assertEquals("(1 . abc)",
            prettyPrinter.valueToString(val));
    }
    
    @Test void nestedLists() {
        var val = makeList(machine,
            makeList(machine,
                machine.makeSymbol("lambda"),
                HonsValue.nil,
                HonsValue.fromSmallInt(123)
                ),
            HonsValue.fromSmallInt(456));
        assertEquals("((lambda nil 123) 456)",
            prettyPrinter.valueToString(val));
    }
    
    @Test void pairOfNil() {
        assertEquals("(nil)", prettyPrinter.valueToString(machine.cons(HonsValue.nil, HonsValue.nil)));
    }
    
    @Test void string() {
        assertEquals("\"\\\\\\\"abc\"", prettyPrinter.valueToString(machine.cons(machine.makeSymbol("*string"), stringAsList(machine, "\\\"abc"))));
    }
    
    @Test void stringWithMoreEscapes() {
        /* \t, \b, \n, \r, \f, \' */
        var input = "\t\b\n\r\f'";
        var expected = "\"\\t\\b\\n\\r\\f\\'\"";
        assertEquals(expected, prettyPrinter.valueToString(machine.cons(machine.makeSymbol("*string"), stringAsList(machine, input))));
    }
    
    @Test void stringWithEmojis() {
        /* "🇬🇧" == "\\u{1F1EC}\\u{1f1e7}" */
        /* Build the string explicitly using codepoints not UTF-16 */
        var stringVal = machine.cons(machine.makeSymbol("*string"), intList(machine, new int[]{0x1f1ec, 0x1f1e7}));
        var expected = "\"\\u{1f1ec}\\u{1f1e7}\"";
        assertEquals(expected, prettyPrinter.valueToString(stringVal));
    }
}