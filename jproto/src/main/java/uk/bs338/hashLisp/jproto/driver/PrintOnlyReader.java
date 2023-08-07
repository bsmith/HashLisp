package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.*;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

public class PrintOnlyReader<V extends IValue> implements IReader<V> {
    private final @NotNull IMachine<V> machine;
    private final @NotNull IReader<V> reader;
    private final @NotNull V nil;
    private final @NotNull V ioPrintSym;
    private final @NotNull V quoteSym;

    public PrintOnlyReader(@NotNull IMachine<V> machine, @NotNull IReader<V> reader) {
        this.machine = machine;
        this.reader = reader;
        nil = machine.nil();
        ioPrintSym = machine.makeSymbol("io-print!");
        quoteSym = machine.makeSymbol("quote");
    }

    @Override
    public @NotNull ReadResult<V> read(@NotNull String str) {
        /* wrap in (io-print! (quote <val>)) */
        var result = reader.read(str);
        return result.mapValueIfSuccess((val) -> {
            var quote = machine.cons(quoteSym, machine.cons(val, nil));
            return machine.cons(ioPrintSym, machine.cons(quote, nil));
        });
    }
}
