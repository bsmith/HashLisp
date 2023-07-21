package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.wrapped.IWrappedCons;

import java.util.Optional;

public interface IConsExpr extends IExpr, IWrappedCons {
    @Override
    default boolean isCons() {
        return true;
    }

    @NotNull IExpr fst();
    @NotNull IExpr snd();
    
    @NotNull Optional<IExpr> getMemoEval();

    <V extends IExpr> void setMemoEval(@Nullable V expr);
    
    @Override
    default boolean isNormalForm() {
        return fst().isSymbol() && fst().asSymbol().isDataHead();
    }

    @Override
    default boolean isHeadNormalForm() {
        return fst().isNormalForm();
    }

    @Override
    default IConsExpr asCons() {
        return this;
    }
}
