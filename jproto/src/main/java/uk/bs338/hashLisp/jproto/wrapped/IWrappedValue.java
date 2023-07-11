package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.IValue;

public interface IWrappedValue<V extends IValue, W extends IWrappedValue<V, W>> extends IValue {
    @NotNull V getValue();

    @NotNull ConsPair<W> uncons();

    default @NotNull W fst() {
        return uncons().fst();
    }

    default @NotNull W snd() {
        return uncons().snd();
    }

    boolean isSymbol();

    @NotNull W symbolName();

    @NotNull String symbolNameAsString();
}
