package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public abstract class ReadResult {
    protected final String remaining;

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

    public static @NotNull ReadResult failedRead(String remaining, String message) {
        return new Failed(remaining, message);
    }
    
    public static @NotNull ReadResult successfulRead(String remaining, HonsValue value) {
        return new Successful(remaining, value);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadResult that = (ReadResult) o;
        return Objects.equals(remaining, that.remaining);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remaining);
    }

    private static class Failed extends ReadResult {
        private final String message;
        
        public Failed(String remaining, String message) {
            super(remaining);
            this.message = message;
        }

        @Override
        public @NotNull Optional<HonsValue> getValue() {
            return Optional.empty();
        }
        
        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Failed failed = (Failed) o;
            return Objects.equals(message, failed.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), message);
        }

        @Override
        public @NotNull String toString() {
            return "Failed{" +
                "message='" + message + '\'' +
                ", remaining='" + remaining + '\'' +
                '}';
        }
    }
    
    private static class Successful extends ReadResult {
        private final HonsValue value;
        
        public Successful(String remaining, HonsValue value) {
            super(remaining);
            this.value = value;
        }

        @Override
        public @NotNull Optional<HonsValue> getValue() {
            return Optional.of(value);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Successful that = (Successful) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), value);
        }

        @Override
        public @NotNull String toString() {
            return "Successful{" +
                "value=" + value +
                ", remaining='" + remaining + '\'' +
                '}';
        }
    }
}
