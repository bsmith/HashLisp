package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IWrappedCons extends IWrappedValue {
    @NotNull IWrappedValue fst();
    @NotNull IWrappedValue snd();
    
    @NotNull Optional<? extends IWrappedValue> getMemoEval();

    <V extends IWrappedValue> void setMemoEval(@Nullable V expr);

    IWrappedSymbol makeSymbol();
}
