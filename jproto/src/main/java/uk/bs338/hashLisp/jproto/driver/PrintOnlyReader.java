package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.*;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

public class PrintOnlyReader<V extends IValue> implements IReader<V> {
    private final @NotNull IMachine<V> heap;
    private final @NotNull IReader<V> reader;
    private final @NotNull V nil;
    private final @NotNull V ioPrintSym;
    private final @NotNull V quoteSym;

    public PrintOnlyReader(@NotNull IMachine<V> heap, @NotNull IReader<V> reader) {
        this.heap = heap;
        this.reader = reader;
        nil = heap.nil();
        ioPrintSym = heap.makeSymbol("io-print!");
        quoteSym = heap.makeSymbol("quote");
    }

    @Override
    public @NotNull ReadResult<V> read(@NotNull String str) {
        /* wrap in (io-print! (quote <val>)) */
        var result = reader.read(str);
        return result.mapValueIfSuccess((val) -> {
            var quote = heap.cons(quoteSym, heap.cons(val, nil));
            return heap.cons(ioPrintSym, heap.cons(quote, nil));
        });
    }
}
