package uk.bs338.hashLisp.jproto;

import static uk.bs338.hashLisp.jproto.Utilities.*;

/* This mixin extends a heap with symbol support */
public interface ISymbolMixin<V extends IValue> extends IHeap<V> {
    default V makeSymbol(V name) {
        return cons(symbolTag(), name);
    }

    default V makeSymbol(String name) {
        return makeSymbol(stringAsList(this, name));
    }

    default boolean isSymbol(V symbol) {
        try {
            return fst(symbol).isSymbolTag();
        } catch (Exception e) {
            return false;
        }
    }

    default V symbolName(V symbol) {
        ConsPair<V> uncons = uncons(symbol);
        if (!uncons.fst().isSymbolTag())
            throw new IllegalArgumentException("Cannot get symbolName of non-symbol");
        return uncons.snd();
    }

    default String symbolNameAsString(V symbol) {
        return listAsString(this, snd(symbol));
    }
}
