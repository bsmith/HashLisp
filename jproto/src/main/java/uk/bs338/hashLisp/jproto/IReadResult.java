package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.Function;

public interface IReadResult<T> {
    @NotNull String getRemaining();
    
    default boolean isSuccess() {
        return false;
    }
    
    default boolean isFailure() {
        return false;
    }
    
    default @NotNull T getValue() throws NoSuchElementException {
        throw new NoSuchElementException();
    }
    
    default @NotNull String getFailureMessage() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    /* These are not generic over T because ReadResult is specialised specifically for HonsValue */
    //    <R> IReadResult<R> fmap(Function<? super T, R> func);
    <R extends T> IReadResult<T> replaceValueIfSuccess(R val);
    default IReadResult<T> mapValueIfSuccess(Function<? super T, ? extends T> mapper) {
        if (isSuccess())
            return replaceValueIfSuccess(mapper.apply(getValue()));
        return this;
    }
}
