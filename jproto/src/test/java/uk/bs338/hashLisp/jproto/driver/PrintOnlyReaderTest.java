package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.reader.ReadResult;
import uk.bs338.hashLisp.jproto.wrapped.WrappedHeap;
import uk.bs338.hashLisp.jproto.wrapped.WrappedValue;

import static org.junit.jupiter.api.Assertions.*;

class PrintOnlyReaderTest {
    WrappedHeap heap;
    MockReader<WrappedValue> mockReader;
    PrintOnlyReader<WrappedValue> printOnlyReader;
    WrappedValue exampleValue;
    WrappedValue wrappedExampleValue;
    
    static class MockReader<V> implements IReader<V> {
        private V value;

        public void setValue(V readValue) {
            this.value = readValue;
        }

        @Override
        public @NotNull ReadResult<V> read(@NotNull String str) {
            if (str.equals("success")) {
                return ReadResult.successfulRead("", value);
            } else {
                return ReadResult.failedRead(str, "failed");
            }
        }
    }
    
    @BeforeEach void setUp() {
        heap = WrappedHeap.wrap(new HonsHeap());
        mockReader = new MockReader<>();
        printOnlyReader = new PrintOnlyReader<>(heap, mockReader);
        exampleValue = Utilities.makeList(heap, heap.makeSymbol("add"), heap.makeSmallInt(1), heap.makeSmallInt(2));
        wrappedExampleValue = Utilities.makeList(heap, heap.makeSymbol("io-print!"),
            Utilities.makeList(heap, heap.makeSymbol("quote"), exampleValue));
        mockReader.setValue(exampleValue);
    }

    @Test
    void readSuccess() {
        var result = printOnlyReader.read("success");
        assertTrue(result.isSuccess());
        assertEquals(wrappedExampleValue, result.getValue());
    }
    
    @Test
    void readFailure() {
        var result = printOnlyReader.read("failure");
        assertTrue(result.isFailure());
        assertEquals("failed", result.getFailureMessage());
    }
}