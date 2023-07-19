package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

import java.util.Optional;

public interface IWrappedCons2<V extends IValue, W extends IWrappedValue2<V, W>> extends IWrappedValue2<V, W> {
    @NotNull W fst();
    @NotNull W snd();

    @NotNull Optional<W> getMemoEval();

    void setMemoEval(W expr); /* XXX SHOULD USE Optional */
}
