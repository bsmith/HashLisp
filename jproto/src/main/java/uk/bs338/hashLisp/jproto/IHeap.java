package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.hons.PrettyPrinter;

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

    @NotNull
    V makeSymbol(@NotNull V name);
    
    @NotNull
    V makeSymbol(@NotNull String name);

    boolean isSymbol(@NotNull V symbol);

    @NotNull
    V symbolName(@NotNull V symbol);

    @NotNull
    String symbolNameAsString(@NotNull V symbol);
    
    default @NotNull String valueToString(@NotNull V val) {
        return PrettyPrinter.valueToString(this, val);
    }

    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
