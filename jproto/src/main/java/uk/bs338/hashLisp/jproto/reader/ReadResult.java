package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.IReadResult;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

public sealed abstract class ReadResult implements IReadResult<HonsValue> {
    protected final String remaining;

    protected ReadResult(String remaining) {
        this.remaining = remaining;
    }
    
    public @NotNull String getRemaining() {
        return remaining;
    }
    
    public @NotNull HonsValue getValue(){
        throw new NoSuchElementException();
    }
    
    public @NotNull String getFailureMessage() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    public static @NotNull ReadResult failedRead(String remaining, String message) {
        return new Failed(remaining, message);
    }
    
    public static @NotNull ReadResult successfulRead(String remaining, HonsValue value) {
        return new Successful(remaining, value);
    }

    @Override
    public <R extends HonsValue> ReadResult replaceValueIfSuccess(R val) {
        if (isSuccess())
            return successfulRead(this.remaining, val);
        return this;
    }

    @Override
    public ReadResult mapValueIfSuccess(Function<? super HonsValue, ? extends HonsValue> mapper) {
        if (isSuccess())
            return replaceValueIfSuccess(mapper.apply(this.getValue()));
        return this;
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

    private final static class Failed extends ReadResult {
        private final String message;
        
        public Failed(String remaining, String message) {
            super(remaining);
            this.message = message;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public @NotNull String getFailureMessage() {
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
    
    private final static class Successful extends ReadResult {
        private final HonsValue value;
        
        public Successful(String remaining, HonsValue value) {
            super(remaining);
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public @NotNull HonsValue getValue() {
            return value;
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
