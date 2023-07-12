package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

public interface IHeap<V extends IValue> extends IValueFactory<V> {
    @NotNull
    V cons(@NotNull V fst, @NotNull V snd);
    
    @NotNull
    ConsPair<V> uncons(@NotNull V cons);

    @NotNull
    default V fst(@NotNull V val) {
        return uncons(val).fst();
    }

    @NotNull
    default V snd(@NotNull V val) {
        return uncons(val).snd();
    }

    V makeSymbol(V name);

    boolean isSymbol(V symbol);

    V symbolName(V symbol);

    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
