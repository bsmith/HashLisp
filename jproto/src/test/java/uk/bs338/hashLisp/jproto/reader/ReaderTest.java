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
        if (heap == null) /* reuse heap */
            heap = new HonsHeap();
        if (charClassifier == null)
            charClassifier = new CharClassifier();
        tokeniserFactory = (String str) -> new Tokeniser(str, charClassifier); 
        reader = new Reader(heap, tokeniserFactory);
    }
    
    @Nested
    class SimpleValues {
        @Test void shortInt() throws Exception {
            var input = "123";
            var expected = ReadResult.successfulRead("", HonsValue.fromShortInt(123));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }

        @Test void symbol() throws Exception {
            var input = "abc";
            var expected = ReadResult.successfulRead("", makeSymbol(heap,"abc"));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void nilAsHash() throws Exception {
            var input = "#0";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
    }
    
    @Nested
    class ConsValues {
        @Test void emptyListIsNil() throws Exception {
            var input = "()";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void pairOfInts() throws Exception {
            var input = "(123 . 345)";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromShortInt(123),
                    HonsValue.fromShortInt(345)
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void oneElementList() throws Exception {
            var input = "(123)";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromShortInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void twoElementList() throws Exception {
            var input = "(123 456)";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromShortInt(123),
                    heap.cons(
                        HonsValue.fromShortInt(456),
                        HonsValue.nil
                    )
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }

        @Test void oneElementListWrittenAsPair() throws Exception {
            var input = "(123 . ())";
            var expected = ReadResult.successfulRead("",
                heap.cons(
                    HonsValue.fromShortInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void dotCannotAppearAtStartOfList(TestReporter testReporter) throws Exception {
            var input = "( . 123)";
            var actual = reader.read(input);
            assertTrue(actual.getValue().isEmpty());
            testReporter.publishEntry(actual.getMessage());
            assertEquals(input, actual.getRemaining());
        }
    }
    
    @Test
    void read(TestReporter testReporter) throws Exception {
        var input = "(add (add 1 2) 3 4)";
        var addSym = makeSymbol(heap,"add");
        var expected = makeList(heap,
            addSym,
            makeList(heap,
                addSym,
                HonsValue.fromShortInt(1),
                HonsValue.fromShortInt(2)
            ),
            HonsValue.fromShortInt(3),
            HonsValue.fromShortInt(4)
            );
        ReadResult rv = reader.read(input);
        assertEquals(Optional.of(expected), rv.getValue());
        testReporter.publishEntry(rv.toString());
        assertEquals("", rv.getRemaining());
    }

}