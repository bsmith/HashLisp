package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.List;
import java.util.Map;

public interface IEvaluator<V> {
    @NotNull V evaluate(@NotNull V val);
    
    /* the map is symbol -> value */
    @NotNull V evaluateWith(@NotNull Map<V, V> globals, @NotNull V val);
}
