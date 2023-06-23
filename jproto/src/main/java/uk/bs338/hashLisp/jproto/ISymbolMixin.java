package uk.bs338.hashLisp.jproto;

/* This mixin extends a heap with symbol support */
public interface ISymbolMixin<V extends IValue> extends IHeap<V> {
    default V makeSymbol(V name) {
        return cons(symbolTag(), name);
    }

    default boolean isSymbol(V symbol) {
        try {
            return fst(symbol).isSymbolTag();
        } catch (Exception e) {
            return false;
        }
    }

    default V symbolName(V symbol) {
        return snd(symbol);
    }
}
