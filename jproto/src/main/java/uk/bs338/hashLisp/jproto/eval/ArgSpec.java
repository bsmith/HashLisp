package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;

public class ArgSpec {
    private final @NotNull HonsHeap heap;
    private final @NotNull HonsValue argSpec;

    public ArgSpec(@NotNull HonsHeap heap, @NotNull HonsValue argSpec) {
        this.heap = heap;
        this.argSpec = argSpec;
    }
    
    public static @NotNull Assignments match(@NotNull HonsHeap heap, @NotNull HonsValue argSpec, @NotNull HonsValue args) {
        return new ArgSpec(heap, argSpec).match(args);
    }

    public @NotNull Assignments match(@NotNull HonsValue args) {
        if (heap.isSymbol(argSpec)) {
//            return makeList(heap, heap.makeSymbol("error"), heap.makeSymbol("slurpy argSpec not implemented"));
            throw new RuntimeException("Not implemented");
        }
        else if (argSpec.isConsRef()) {
            var assignmentsMap = new HashMap<HonsValue, HonsValue>();
            var curSpec = argSpec;
            var curArg = args;
            while (!curSpec.isNil()) {
                assignmentsMap.put(heap.fst(curSpec), heap.fst(curArg));
                curSpec = heap.snd(curSpec);
                curArg = heap.snd(curArg);
            }
            return new Assignments(heap, assignmentsMap);
        }
        else
            throw new RuntimeException("Not implemented");
    }
}
