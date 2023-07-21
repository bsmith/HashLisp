package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;

public class ArgSpecCache implements IArgSpecFactory {
    private final ExprFactory exprFactory;
    private final Map<HonsValue, ArgSpec> cache;

    public ArgSpecCache(ExprFactory exprFactory) {
        this.exprFactory = exprFactory;
        this.cache = new HashMap<>();
    }

    @Override
    public @NotNull ArgSpec get(@NotNull HonsValue argSpec) throws EvalException {
        // Annoyingly this can't cope with exceptions
//        return cache.computeIfAbsent(argSpec, (spec) -> new ArgSpec(heap, spec));
        var value = cache.get(argSpec);
        if (value == null) {
            value = new ArgSpec(exprFactory, argSpec);
            cache.put(argSpec, value);
        }
        return value;
    }
    
    @Override
    public @NotNull Assignments match(@NotNull HonsValue argSpec, @NotNull HonsValue args) throws EvalException {
        return get(argSpec).match(args);
    }
}
