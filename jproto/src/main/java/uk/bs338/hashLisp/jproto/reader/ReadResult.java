package uk.bs338.hashLisp.jproto.reader;

import uk.bs338.hashLisp.jproto.LispValue;

import java.util.NoSuchElementException;
import java.util.Optional;

public abstract class ReadResult {
    private final String remaining;

    protected ReadResult(String remaining) {
        this.remaining = remaining;
    }
    
    public String getRemaining() {
        return remaining;
    }
    
    public abstract Optional<LispValue> getValue();
    public String getMessage() throws NoSuchElementException {
        throw new NoSuchElementException();
    };

    public static ReadResult failedRead(String remaining, String message) {
        return new Failed(remaining, message);
    }
    
    public static ReadResult successfulRead(String remaining, LispValue value) {
        return new Successful(remaining, value);
    }
    
    private static class Failed extends ReadResult {
        private final String message;
        
        public Failed(String remaining, String message) {
            super(remaining);
            this.message = message;
        }

        @Override
        public Optional<LispValue> getValue() {
            return Optional.empty();
        }
        
        @Override
        public String getMessage() {
            return message;
        }
    }
    
    private static class Successful extends ReadResult {
        private final LispValue value;
        
        public Successful(String remaining, LispValue value) {
            super(remaining);
            this.value = value;
        }

        @Override
        public Optional<LispValue> getValue() {
            return Optional.of(value);
        }
    }
}
