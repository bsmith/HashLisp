package uk.bs338.hashLisp.jproto;

import javax.annotation.Nonnull;

public interface IHeap<V extends IValue> extends IValueFactory<V> {
    @Nonnull
    V cons(@Nonnull V fst, @Nonnull V snd);
    
    @Nonnull
    ConsPair<V> uncons(@Nonnull V cons);

    @Nonnull
    default V fst(V val) {
        return uncons(val).fst();
    }

    @Nonnull
    default V snd(V val) {
        return uncons(val).snd();
    }

    V makeSymbol(V name);

    boolean isSymbol(V symbol);

    V symbolName(V symbol);

    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
