package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ArgSpec {
    private final @NotNull ExprFactory exprFactory;
    private final @NotNull HonsHeap heap;
    private final @NotNull HonsValue origArgSpec;
    private List<HonsValue> argNames;
    private HonsValue slurpyName;

    public ArgSpec(@NotNull ExprFactory exprFactory, @NotNull HonsValue argSpec) throws EvalException {
        this.exprFactory = exprFactory;
        this.heap = exprFactory.getHeap();
        this.origArgSpec = argSpec;
        
        parseArgSpec(argSpec);
    }

    public @NotNull HonsValue getOrigArgSpec() {
        return origArgSpec;
    }

    private void parseArgSpec(HonsValue argSpec) throws EvalException {
        argNames = new ArrayList<>();
        slurpyName = null;

        var curSpec = argSpec;
        while (!curSpec.isNil()) {
            /* XXX Would a visitor make sense here? Or an iterator? */
            if (heap.isSymbol(curSpec)) {
                slurpyName = curSpec;
                break;
            } else if (curSpec.isConsRef()) {
                var uncons = heap.uncons(curSpec);
                if (!heap.isSymbol(uncons.fst())) {
                    throw new EvalException("Found non-symbol " + uncons.fst() + " in argSpec: " + heap.valueToString(argSpec));
                }
                argNames.add(uncons.fst());
                curSpec = uncons.snd();
            } else {
                throw new EvalException("Cannot parse argSpec at: " + heap.valueToString(curSpec));
            }
        }
    }

    public @NotNull Assignments match(@NotNull HonsValue args) {
        var assignmentsMap = new HashMap<HonsValue, HonsValue>();
        var curArg = args;
        /* XXX would an iterator make sense here? */
        for (var argName : argNames) {
            HonsValue value = HonsValue.nil;
            if (curArg.isConsRef()) {
                var uncons = heap.uncons(curArg);
                value = uncons.fst();
                curArg = uncons.snd();
            } else {
                curArg = HonsValue.nil;
            }
            assignmentsMap.put(argName, value);
        }
        if (slurpyName != null)
            assignmentsMap.put(slurpyName, curArg);
        return new Assignments(exprFactory, assignmentsMap);
    }

    @Override
    public String toString() {
        return "ArgSpec{" +
            "argNames=" + argNames.stream().map(heap::valueToString).toList() +
            ", slurpyName=" + heap.valueToString(slurpyName) +
            '}';
    }
    
    public @NotNull Set<HonsValue> getBoundVariables() {
        Set<HonsValue> names = new HashSet<>(argNames.size() + 1);

        if (slurpyName != null)
            names.add(slurpyName);

        names.addAll(argNames);

        return names;
    }
    
    public @NotNull Assignments alphaConversion(int uniqNumber) {
        Map<HonsValue, HonsValue> oldNameToNewName = new HashMap<>(argNames.size() + 1);
        String prefix = "$%x$".formatted(uniqNumber);

        Consumer<HonsValue> addPrefix = (old) -> {
            var oldName = heap.symbolName(old);
            if (heap.fst(oldName).toSmallInt() == '$')
                return;
            var newName = heap.symbolName(old);
            for (int idx = prefix.length() - 1; idx >= 0; idx--) {
                newName = heap.cons(HonsValue.fromSmallInt(prefix.charAt(idx)), newName);
            }
            oldNameToNewName.put(old, heap.makeSymbol(newName));
        };
        
        if (slurpyName != null)
            addPrefix.accept(slurpyName);
        
        for (var name : argNames)
            addPrefix.accept(name);
        
        return new Assignments(exprFactory, oldNameToNewName);
    }
}
