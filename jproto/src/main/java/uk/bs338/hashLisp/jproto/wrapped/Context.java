package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Context extends WrappedHeap implements IReader<WrappedValue>, IEvaluator<WrappedValue> {
    protected final IReader<HonsValue> reader;
    protected final IEvaluator<HonsValue> evaluator;
    
    public Context(HonsMachine machine, IReader<HonsValue> reader, IEvaluator<HonsValue> evaluator) {
        super(machine);
        this.reader = reader;
        this.evaluator = evaluator;
    }
    
    public Context(Context context) {
        this(context.machine, context.reader, context.evaluator);
    }

    public IReader<HonsValue> getReader() {
        return reader;
    }

    public IEvaluator<HonsValue> getEvaluator() {
        return evaluator;
    }

    @Override
    public @NotNull WrappedValue evaluate(@NotNull WrappedValue val) {
        return wrap(evaluator.evaluate(unwrap(val)));
    }

    @Override
    public @NotNull WrappedValue evaluateWith(@NotNull Map<WrappedValue, WrappedValue> globals, @NotNull WrappedValue val) {
        Set<Map.Entry<HonsValue, HonsValue>> entrySet = globals.entrySet().stream().map(entry -> Map.entry(unwrap(entry.getKey()), unwrap(entry.getValue()))).collect(Collectors.toSet());
        Map<HonsValue, HonsValue> unwrappedGlobals = new AbstractMap<>() {
            @NotNull
            @Override
            public Set<Entry<HonsValue, HonsValue>> entrySet() {
                return entrySet;
            }
        };
        return wrap(evaluator.evaluateWith(unwrappedGlobals, unwrap(val)));
    }

    @Override
    public @NotNull ReadResult<WrappedValue> read(@NotNull String str) {
        return reader.read(str).mapValueIfSuccess(this::wrap);
    }
}
