package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;

public class ArgSpecCache {
    private final @NotNull HonsMachine machine;
    private final @NotNull Map<HonsValue, ArgSpec> cache;

    public ArgSpecCache(@NotNull HonsMachine machine) {
        this.machine = machine;
        this.cache = new HashMap<>();
    }
    
    public @NotNull ArgSpec getByValue(@NotNull HonsValue argSpec) throws EvalException {
        var value = cache.get(argSpec);
        if (value == null) {
            value = new ArgSpec(machine, IExpr.wrap(machine, argSpec));
            cache.put(argSpec, value);
        }
        return value;
    }

    public @NotNull ArgSpec get(@NotNull IExpr argSpec) throws EvalException {
        // Annoyingly this can't cope with exceptions
//        return cache.computeIfAbsent(argSpec, (spec) -> new ArgSpec(machine, spec));
        var value = cache.get(argSpec.getValue());
        if (value == null) {
            value = new ArgSpec(machine, argSpec);
            cache.put(argSpec.getValue(), value);
        }
        return value;
    }
    
    public @NotNull Assignments match(@NotNull IExpr argSpec, @NotNull IExpr args) throws EvalException {
        return get(argSpec).match(args);
    }
}
