package uk.bs338.hashLisp.jproto.wrapped;

import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.IValue;

public interface IWrappedValue<V extends IValue> extends IValue {
    V getValue();

    ConsPair<V> uncons();

    default V fst() {
        return uncons().fst();
    }

    default V snd() {
        return uncons().snd();
    }

    boolean isSymbol();

    V symbolName();

    String symbolNameAsString();
}
