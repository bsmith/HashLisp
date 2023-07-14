package uk.bs338.hashLisp.jproto.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ReadResultTest {
    @Nested
    class FailedTest {
        ReadResult<?> result;
        
        @BeforeEach void setUp() {
            result = ReadResult.failedRead("remaining", "message");
        }
        
        @Test void is() {
            assertTrue(result.isFailure());
            assertFalse(result.isSuccess());
        }
        
        @Test void getRemaining() {
            assertEquals("remaining", result.getRemaining());
        }
        
        @Test void getValue() {
            assertThrows(NoSuchElementException.class, () -> result.getValue());
        }
        
        @Test void getFailureMessage() {
            assertEquals("message", result.getFailureMessage());
        }
    }
    
    @Nested
    class SuccessfulTest {
        ReadResult<HonsValue> result;

        @BeforeEach void setUp() {
            result = ReadResult.successfulRead("remaining", HonsValue.nil);
        }

        @Test void is() {
            assertFalse(result.isFailure());
            assertTrue(result.isSuccess());
        }

        @Test void getRemaining() {
            assertEquals("remaining", result.getRemaining());
        }

        @Test void getValue() {
            assertEquals(HonsValue.nil, result.getValue());
        }

        @Test void getMessage() {
            assertThrows(NoSuchElementException.class, () -> result.getFailureMessage());
        }
    }
}