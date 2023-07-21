package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;

import java.util.List;

public class NoOpEvaluator<V> implements IEvaluator<V> {
    @Override
    public @NotNull V eval_one(@NotNull V val) {
        return val;
    }

    @Override
    public @NotNull List<V> eval_multi_inplace(@NotNull List<V> vals) {
        return vals;
    }
}
