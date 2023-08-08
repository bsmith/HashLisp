package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IConsExpr extends IExpr {
    @NotNull IExpr fst();
    @NotNull IExpr snd();
    
    @NotNull Optional<IExpr> getMemoEval();
    
    void setMemoEval(IExpr expr);
    
    /* normal form means: something that could be applied because it is fully evaluated
     * if the head is not a * symbol, then we could call eval and have it try to apply
     */
    @Override
    default boolean isNormalForm() {
        return fst().getType() == ExprType.SYMBOL && fst().asSymbolExpr().isDataHead();
    }

    @Override
    default @NotNull IConsExpr asConsExpr() {
        return this;
    }
    
    @NotNull ISymbolExpr makeSymbol();
}
