package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface ISymbolExpr extends ISimpleExpr {
    @NotNull IConsExpr symbolName();
    @NotNull String symbolNameAsString();
    
    boolean isDataHead();
    
    @Override
    default ISymbolExpr asSymbolExpr() {
        return this;
    }
}
