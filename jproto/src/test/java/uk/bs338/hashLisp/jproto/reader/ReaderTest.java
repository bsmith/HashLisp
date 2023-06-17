package uk.bs338.hashLisp.jproto.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReaderTest {
    HonsHeap heap;
    Reader reader;
    
    @BeforeEach
    void setUp() {
        if (heap == null) /* reuse heap */
            heap = new HonsHeap();
        reader = new Reader(heap);
    }
    
    @Test
    void read() {
        ReadResult rv = reader.read("(add (add 1 2) 3 4)");
        assertEquals(Optional.empty(), rv.getValue());
        assertEquals("Not implemented", rv.getMessage());
    }

}