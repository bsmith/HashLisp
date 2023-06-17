package uk.bs338.hashLisp.jproto;

import static uk.bs338.hashLisp.jproto.Utilities.*;

import java.security.InvalidParameterException;

/* This mixin extends a heap with symbol support */
public interface ISymbolMixin<V extends IValue> extends IHeap<V> {
    default V makeSymbol(V name) throws Exception {
        return cons(symbolTag(), name);
    }

    default V makeSymbol(String name) throws Exception {
        return makeSymbol(stringAsList(this, name));
    }

    default boolean isSymbol(V symbol) {
        try {
            return fst(symbol).isSymbolTag();
        } catch (Exception e) {
            return false;
        }
    }

    default V symbolName(V symbol) throws Exception {
        Pair<V> pair = uncons(symbol);
        if (!pair.fst.isSymbolTag())
            throw new InvalidParameterException("Cannot get symbolName of non-symbol");
        return pair.snd;
    }

    default String symbolNameAsString(V symbol) throws Exception {
        return listAsString(this, snd(symbol));
    }
}
