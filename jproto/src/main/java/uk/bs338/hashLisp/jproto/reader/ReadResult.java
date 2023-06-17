package uk.bs338.hashLisp.jproto.reader;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

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
    
    public abstract Optional<HonsValue> getValue();
    public String getMessage() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    public static ReadResult failedRead(String remaining, String message) {
        return new Failed(remaining, message);
    }
    
    public static ReadResult successfulRead(String remaining, HonsValue value) {
        return new Successful(remaining, value);
    }
    
    private static class Failed extends ReadResult {
        private final String message;
        
        public Failed(String remaining, String message) {
            super(remaining);
            this.message = message;
        }

        @Override
        public Optional<HonsValue> getValue() {
            return Optional.empty();
        }
        
        @Override
        public String getMessage() {
            return message;
        }
    }
    
    private static class Successful extends ReadResult {
        private final HonsValue value;
        
        public Successful(String remaining, HonsValue value) {
            super(remaining);
            this.value = value;
        }

        @Override
        public Optional<HonsValue> getValue() {
            return Optional.of(value);
        }
    }
}
