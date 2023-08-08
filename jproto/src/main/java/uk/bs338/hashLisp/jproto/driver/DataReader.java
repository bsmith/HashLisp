package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.*;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

public class DataReader<V extends IValue> implements IReader<V> {
    private final @NotNull IMachine<V> machine;
    private final @NotNull IReader<V> reader;
    private final @NotNull V nil;
    private final @NotNull V dataTag;

    public DataReader(@NotNull IMachine<V> machine, @NotNull IReader<V> reader) {
        this.machine = machine;
        this.reader = reader;
        nil = machine.nil();
        dataTag = machine.makeSymbol(Tag.DATA.getSymbolStr());
    }

    @Override
    public @NotNull ReadResult<V> read(@NotNull String str) {
        /* wrap in (*data <val>) */
        var result = reader.read(str);
        return result.mapValueIfSuccess((val) -> machine.cons(dataTag, machine.cons(val, nil)));
    }
}
