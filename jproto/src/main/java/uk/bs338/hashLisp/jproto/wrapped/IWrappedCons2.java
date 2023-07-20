package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

import java.util.Optional;

public interface IWrappedCons2 extends IWrappedValue2 {
    @NotNull IWrappedValue2 fst();
    @NotNull IWrappedValue2 snd();

    @NotNull Optional<? extends IWrappedValue2> getMemoEval();

    void setMemoEval(IWrappedValue2 expr); /* XXX SHOULD USE Optional */
}
