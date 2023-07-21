package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.*;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

public class DataReader<V extends IValue> implements IReader<V> {
    private final @NotNull IHeap<V> heap;
    private final @NotNull IReader<V> reader;
    private final @NotNull V nil;
    private final @NotNull V dataTag;

    public DataReader(@NotNull IHeap<V> heap, @NotNull IReader<V> reader) {
        this.heap = heap;
        this.reader = reader;
        nil = heap.nil();
        dataTag = heap.makeSymbol(Tag.DATA.getSymbolStr());
    }

    @Override
    public @NotNull ReadResult<V> read(@NotNull String str) {
        /* wrap in (*data <val>) */
        var result = reader.read(str);
        return result.mapValueIfSuccess((val) -> {
            return heap.cons(dataTag, heap.cons(val, nil));
        });
    }
}
