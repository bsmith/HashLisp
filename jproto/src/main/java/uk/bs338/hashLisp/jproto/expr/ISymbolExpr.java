package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.Tag;

public interface ISymbolExpr extends IExpr {
    @NotNull IConsExpr symbolName();
    @NotNull String symbolNameAsString();
    
    boolean isDataHead();

    boolean isTag(Tag tag);

    @Override
    default ISymbolExpr asSymbolExpr() {
        return this;
    }
    
    ISymbolExpr makeDataHead();
}
