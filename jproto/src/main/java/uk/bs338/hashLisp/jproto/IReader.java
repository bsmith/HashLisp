package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

public interface IReader<T> {
    @NotNull IReadResult<T> read(@NotNull String str);
}
