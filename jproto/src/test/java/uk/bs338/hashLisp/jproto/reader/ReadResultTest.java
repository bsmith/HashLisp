package uk.bs338.hashLisp.jproto.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.LispValue;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReadResultTest {
    @Nested
    class FailedTest {
        ReadResult result;
        
        @BeforeEach void setUp() {
            result = ReadResult.failedRead("remaining", "message");
        }
        
        @Test void getRemaining() {
            assertEquals("remaining", result.getRemaining());
        }
        
        @Test void getValue() {
            assertEquals(Optional.empty(), result.getValue());
        }
        
        @Test void getMessage() {
            assertEquals("message", result.getMessage());
        }
    }
    
    @Nested
    class SuccessfulTest {
        ReadResult result;

        @BeforeEach void setUp() {
            result = ReadResult.successfulRead("remaining", LispValue.nil);
        }

        @Test void getRemaining() {
            assertEquals("remaining", result.getRemaining());
        }

        @Test void getValue() {
            assertEquals(Optional.of(LispValue.nil), result.getValue());
        }

        @Test void getMessage() {
            assertThrows(NoSuchElementException.class, () -> result.getMessage());
        }
    }
}