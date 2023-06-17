package uk.bs338.hashLisp.jproto;

import javax.annotation.Nonnull;

public interface IHeap<V extends IValue> extends IValueFactory<V> {
    @Nonnull
    V cons(@Nonnull V fst, @Nonnull V snd);
    
    @Nonnull
    Pair<V> uncons(@Nonnull V cons) throws Exception;

    @Nonnull
    default V fst(V val) throws Exception {
        return uncons(val).fst;
    }

    @Nonnull
    default V snd(V val) throws Exception {
        return uncons(val).snd;
    }

    V makeSymbol(V name) throws Exception;

    boolean isSymbol(V symbol);

    V symbolName(V symbol) throws Exception;

    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
