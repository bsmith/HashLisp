package uk.bs338.hashLisp.jproto.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static uk.bs338.hashLisp.jproto.Symbols.*;
import static uk.bs338.hashLisp.jproto.Utilities.makeList;


class ReaderTest {
    HonsHeap heap;
    Reader reader;
    
    @BeforeEach
    void setUp() {
        if (heap == null) /* reuse heap */
            heap = new HonsHeap();
        reader = new Reader(heap);
    }
    
    @Nested
    @Disabled(value="Unimplemented")
    class SimpleValues {
        @Test void shortInt() {
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
        
        @Test void nilAsHash() {
            var input = "#0";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
    }
    
    @Nested
    @Disabled(value="Unimplemented")
    class ConsValues {
        @Test void emptyListIsNil() {
            var input = "()";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void pairOfInts() throws Exception {
            var input = "(123 . 345)";
            var expected = ReadResult.successfulRead("",
                heap.hons(
                    HonsValue.fromShortInt(123),
                    HonsValue.fromShortInt(345)
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void oneElementList() throws Exception {
            var input = "(123)";
            var expected = ReadResult.successfulRead("",
                heap.hons(
                    HonsValue.fromShortInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void twoElementList() throws Exception {
            var input = "(123 456)";
            var expected = ReadResult.successfulRead("",
                heap.hons(
                    HonsValue.fromShortInt(123),
                    heap.hons(
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
                heap.hons(
                    HonsValue.fromShortInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
    }
    
    @Test
    @Disabled(value="Unimplemented")
    void read() throws Exception {
        var input = "(add (add 1 2) 3 4)";
        var addSym = makeSymbol(heap, "add");
        var expected = makeList(heap,
            addSym,
            makeList(heap,
                addSym,
                HonsValue.fromShortInt(2),
                HonsValue.fromShortInt(3)
            ),
            HonsValue.fromShortInt(3),
            HonsValue.fromShortInt(4)
            );
        ReadResult rv = reader.read(input);
        assertEquals(Optional.of(expected), rv.getValue());
        assertEquals("", rv.getRemaining());
    }

}