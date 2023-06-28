package uk.bs338.hashLisp.jproto.reader;

import org.junit.jupiter.api.*;

import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

import static uk.bs338.hashLisp.jproto.Symbols.*;
import static uk.bs338.hashLisp.jproto.Utilities.*;

class ReaderTest {
    HonsHeap heap;
    CharClassifier charClassifier;
    Function<String, Tokeniser> tokeniserFactory;
    Reader reader;

    @BeforeEach
    void setUp() {
        /* reuse heap */
        if (heap == null)
            heap = new HonsHeap();
        if (charClassifier == null)
            charClassifier = new CharClassifier();
        tokeniserFactory = (String str) -> new Tokeniser(str, charClassifier); 
        reader = new Reader(heap, tokeniserFactory);
    }
    
    @Nested
    class SimpleValues {
        @Test void shortInt() {
            var input = "123";
            var expected = ReadResult.successfulRead("", HonsValue.fromSmallInt(123));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }

        @Test void symbol() {
            var input = "abc";
            var expected = ReadResult.successfulRead("", makeSymbol(heap, "abc"));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void nilAsHash() {
            var input = "#0";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
    }
    
    @Nested
    class ConsValues {
        @Test void emptyListIsNil() {
            var input = "()";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void pairOfInts() {
            var input = "(123 . 345)";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromSmallInt(123),
                    HonsValue.fromSmallInt(345)
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void oneElementList() {
            var input = "(123)";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromSmallInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void twoElementList() {
            var input = "(123 456)";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromSmallInt(123),
                    heap.cons(
                        HonsValue.fromSmallInt(456),
                        HonsValue.nil
                    )
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }

        @Test void oneElementListWrittenAsPair() {
            var input = "(123 . ())";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromSmallInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void dotCannotAppearAtStartOfList(TestReporter testReporter) {
            var input = "( . 123)";
            var actual = reader.read(input);
            assertTrue(actual.getValue().isEmpty());
            testReporter.publishEntry(actual.getMessage());
            assertEquals(input, actual.getRemaining());
        }
    }
    
    @Nested
    class StringValues {
        void assertStringRead(String expectedStr, String input) {
            var actual = reader.read(input);
            var expected = stringAsList(heap, expectedStr);
            assertEquals(Optional.of(expected), actual.getValue());
        }
        
        @Test void emptyString() {
            assertStringRead("", "\"\"");
        }
        
        @Test void stringWithSpaces() {
            assertStringRead("   ", "\"   \"");
        }
        
        @Test void multilineString() {
            assertStringRead("abc\nmno\nxyz", "\"abc\nmno\nxyz\"");
        }
        
        @Test void backquoteString() {
            assertStringRead("abc\"xyz", "\"abc\\\"xyz\"");
        }
        
        @Test void emojiString() {
            /* TODO */
        }
    }
    
    @Test
    void read(TestReporter testReporter) {
        var input = "(add (add 1 2) 3 4)";
        var addSym = makeSymbol(heap, "add");
        var expected = makeList(heap,
            addSym,
            makeList(heap,
                addSym,
                HonsValue.fromSmallInt(1),
                HonsValue.fromSmallInt(2)
            ),
            HonsValue.fromSmallInt(3),
            HonsValue.fromSmallInt(4)
            );
        ReadResult rv = reader.read(input);
        assertEquals(Optional.of(expected), rv.getValue());
        testReporter.publishEntry(rv.toString());
        assertEquals("", rv.getRemaining());
    }

}