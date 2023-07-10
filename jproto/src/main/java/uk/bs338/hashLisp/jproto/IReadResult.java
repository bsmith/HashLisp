package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public interface IReadResult<T> {
    @NotNull String getRemaining();
    boolean isSuccess();
    boolean isFailure();
    @NotNull T getValue() throws NoSuchElementException;
    @NotNull String getFailureMessage() throws NoSuchElementException;
}
