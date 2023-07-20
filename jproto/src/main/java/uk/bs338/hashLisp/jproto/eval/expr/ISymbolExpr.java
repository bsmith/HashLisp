package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.wrapped.IWrappedSymbol2;

public interface ISymbolExpr extends ISimpleExpr, IWrappedSymbol2 {
    @Override
    default boolean isSymbol() {
        return ISimpleExpr.super.isSymbol();
    }

    @NotNull IConsExpr symbolName();
    @NotNull String symbolNameAsString();
    
    boolean isDataHead();
    
    @Override
    default ISymbolExpr asSymbol() {
        return this;
    }
    
    ISymbolExpr makeDataHead();
    
    @Override
    default boolean isNormalForm() {
        return true;
    }

    @Override
    default boolean isHeadNormalForm() {
        return true;
    }
}
