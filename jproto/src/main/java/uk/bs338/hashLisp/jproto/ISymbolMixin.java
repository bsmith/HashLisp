package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import static uk.bs338.hashLisp.jproto.Utilities.*;

/* This mixin extends a heap with symbol support */
public interface ISymbolMixin<V extends IValue> extends IHeap<V> {
    default @NotNull V makeSymbol(@NotNull V name) {
        return cons(symbolTag(), name);
    }

    default @NotNull V makeSymbol(@NotNull String name) {
        return makeSymbol(stringAsList(this, name));
    }

    default boolean isSymbol(@NotNull V symbol) {
        try {
            return fst(symbol).isSymbolTag();
        } catch (Exception e) {
            return false;
        }
    }

    default @NotNull V symbolName(@NotNull V symbol) {
        ConsPair<? extends V> uncons = uncons(symbol);
        if (!uncons.fst().isSymbolTag())
            throw new IllegalArgumentException("Cannot get symbolName of non-symbol");
        return uncons.snd();
    }

    default @NotNull String symbolNameAsString(@NotNull V symbol) {
        return listAsString(this, snd(symbol));
    }
}
