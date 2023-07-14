package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArgSpec {
    private final @NotNull HonsHeap heap;
    private final @NotNull HonsValue origArgSpec;
    private List<HonsValue> argNames;
    private HonsValue slurpyName;

    public ArgSpec(@NotNull HonsHeap heap, @NotNull HonsValue argSpec) throws EvalException {
        this.heap = heap;
        this.origArgSpec = argSpec;
        
        parseArgSpec(argSpec);
    }
    
    public static @NotNull Assignments match(@NotNull HonsHeap heap, @NotNull HonsValue argSpec, @NotNull HonsValue args) throws EvalException {
        return new ArgSpec(heap, argSpec).match(args);
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
        return new Assignments(heap, assignmentsMap);
    }

    @Override
    public String toString() {
        return "ArgSpec{" +
            "argNames=" + argNames.stream().map(heap::valueToString).toList() +
            ", slurpyName=" + heap.valueToString(slurpyName) +
            '}';
    }
}
