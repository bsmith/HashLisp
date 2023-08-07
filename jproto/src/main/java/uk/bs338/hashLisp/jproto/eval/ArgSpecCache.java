package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;

public class ArgSpecCache {
    private final @NotNull HonsHeap heap;
    private final @NotNull Map<HonsValue, ArgSpec> cache;

    public ArgSpecCache(@NotNull HonsHeap heap) {
        this.heap = heap;
        this.cache = new HashMap<>();
    }

    public @NotNull ArgSpec get(@NotNull HonsValue argSpec) throws EvalException {
        // Annoyingly this can't cope with exceptions
//        return cache.computeIfAbsent(argSpec, (spec) -> new ArgSpec(heap, spec));
        var value = cache.get(argSpec);
        if (value == null) {
            value = new ArgSpec(heap, argSpec);
            cache.put(argSpec, value);
        }
        return value;
    }
    
    public @NotNull Assignments match(@NotNull HonsValue argSpec, @NotNull HonsValue args) throws EvalException {
        return get(argSpec).match(args);
    }
}
