package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface ISymbolExpr extends ISimpleExpr {
    @NotNull HonsValue symbolName();
    @NotNull String symbolNameAsString();
    
    @Override
    default ISymbolExpr asSymbolExpr() {
        return (ISymbolExpr)this;
    }
}
