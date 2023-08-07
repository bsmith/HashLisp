package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.NotNull;

public interface ISymbolExpr extends IExpr {
    @NotNull IConsExpr symbolName();
    @NotNull String symbolNameAsString();
    
    boolean isDataHead();
    
    @Override
    default ISymbolExpr asSymbolExpr() {
        return this;
    }
    
    ISymbolExpr makeDataHead();
}
