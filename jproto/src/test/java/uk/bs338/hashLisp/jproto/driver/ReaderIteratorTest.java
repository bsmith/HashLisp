package uk.bs338.hashLisp.jproto.driver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.junit.jupiter.api.TestInstance;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
class ReaderIteratorTest {
    HonsHeap heap;
    Reader reader;
    
    @BeforeAll
    void setUp() {
        heap = new HonsHeap();
        reader = new Reader(heap, Tokeniser.getFactory(new CharClassifier()));
    }
    
    @Test void simpleTestWithThreeNumbers() {
        var iterator = ReaderIterator.read(reader, "1 2 3");
        var expected = List.of(heap.makeSmallInt(1), heap.makeSmallInt(2), heap.makeSmallInt(3));
        var result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        assertIterableEquals(expected, result);
    }
    
    @Test void unterminatedList() {
        var iterator = ReaderIterator.read(reader, "(1 ");
        var result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        assertEquals(0, result.size());
        assertTrue(iterator.getCurResult().isFailure());
    }
}