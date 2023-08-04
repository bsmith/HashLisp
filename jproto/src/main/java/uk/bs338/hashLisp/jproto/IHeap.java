package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IHeap<V extends IValue> {
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

    @NotNull Optional<V> getMemoEval(@NotNull V val);

    void setMemoEval(@NotNull V val, @Nullable V evalResult);
    
    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
