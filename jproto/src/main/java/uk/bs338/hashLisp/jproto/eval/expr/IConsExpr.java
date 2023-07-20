package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.wrapped.IWrappedCons2;

import java.util.Optional;

public interface IConsExpr extends IExpr, IWrappedCons2 {
    @Override
    default boolean isCons() {
        return true;
    }

    @NotNull IExpr fst();
    @NotNull IExpr snd();
    
    @NotNull Optional<IExpr> getMemoEval();
    
    void setMemoEval(IExpr expr);
    
    /* XXX: not sure this is needed, nor correct? */
    /* normal form means: something that could be applied because it is fully evaluated
     * if the head is not a * symbol, then we could call eval and have it try to apply
     */
    @Override
    default boolean isNormalForm() {
        return fst().isSymbol() && fst().asSymbol().isDataHead();
//        return fst().isSymbol() && fst().asSymbolExpr().symbolNameAsString().startsWith("*");
//        return fst().isSymbol() && fst().asSymbolExpr().symbolName().fst().toSmallInt() == '*'; // XXX
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
