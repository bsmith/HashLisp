package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.reader.ReadResult;

public class Context extends WrappedHeap implements IReader<WrappedValue>, IEvaluator<WrappedValue> {
    private final IReader<HonsValue> reader;
    private final IEvaluator<HonsValue> evaluator;
    
    public Context(HonsHeap heap, IReader<HonsValue> reader, IEvaluator<HonsValue> evaluator) {
        super(heap);
        this.reader = reader;
        this.evaluator = evaluator;
    }

    @Override
    public @NotNull WrappedValue eval_one(@NotNull WrappedValue val) {
        return wrap(evaluator.eval_one(unwrap(val)));
    }

    @Override
    public @NotNull ReadResult<WrappedValue> read(@NotNull String str) {
        return reader.read(str).mapValueIfSuccess(this::wrap);
    }
}
