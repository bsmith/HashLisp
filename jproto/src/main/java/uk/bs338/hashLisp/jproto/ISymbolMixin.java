package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import static uk.bs338.hashLisp.jproto.Utilities.*;

/* This mixin extends a heap with symbol support */
public interface ISymbolMixin<V extends IValue> extends IMachine<V> {
    default @NotNull V makeSymbol(@NotNull V name) {
        return cons(symbolTag(), name);
    }

    default @NotNull V makeSymbol(@NotNull String name) {
        return makeSymbol(stringAsList(this, name));
    }

    default boolean isSymbol(@NotNull V symbol) {
        return symbol.getType() == ValueType.CONS_REF && fst(symbol).getType() == ValueType.SYMBOL_TAG;
    }

    default @NotNull V symbolName(@NotNull V symbol) {
        ConsPair<V> uncons = uncons(symbol);
        if (uncons.fst().getType() != ValueType.SYMBOL_TAG)
            throw new IllegalArgumentException("Cannot get symbolName of non-symbol");
        return uncons.snd();
    }

    default @NotNull String symbolNameAsString(@NotNull V symbol) {
        return listAsString(this, snd(symbol));
    }
}
