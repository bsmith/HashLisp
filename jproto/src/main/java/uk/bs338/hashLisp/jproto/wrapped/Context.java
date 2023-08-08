package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

import java.util.List;

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
    public @NotNull WrappedValue eval_one(@NotNull WrappedValue val) {
        return wrap(evaluator.eval_one(unwrap(val)));
    }

    @Override
    public @NotNull List<WrappedValue> eval_multi_inplace(@NotNull List<WrappedValue> vals) {
        vals.replaceAll(this::eval_one);
        return vals;
    }

    @Override
    public @NotNull ReadResult<WrappedValue> read(@NotNull String str) {
        return reader.read(str).mapValueIfSuccess(this::wrap);
    }
}
