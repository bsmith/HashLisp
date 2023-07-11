package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

public interface IReader<T> {
    @NotNull ReadResult<T> read(@NotNull String str);
}
