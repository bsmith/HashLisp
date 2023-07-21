package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;

import java.util.List;
import java.util.Map;

public class NoOpEvaluator<V> implements IEvaluator<V> {
    @Override
    public @NotNull V evaluate(@NotNull V val) {
        return val;
    }

    @Override
    public @NotNull V evaluateWith(@NotNull Map<V, V> globals, @NotNull V val) {
        return val;
    }
}
