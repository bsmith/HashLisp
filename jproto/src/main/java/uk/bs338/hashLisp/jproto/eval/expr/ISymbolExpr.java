package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.wrapped.IWrappedSymbol;

public interface ISymbolExpr extends ISimpleExpr, IWrappedSymbol {
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

}
