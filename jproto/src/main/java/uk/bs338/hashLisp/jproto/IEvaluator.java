package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IEvaluator<V> {
    @NotNull V evaluate(@NotNull V val);
    
    /* the map is symbol -> value */
    @NotNull V evaluateWith(@NotNull Map<V, V> globals, @NotNull V val);
}
