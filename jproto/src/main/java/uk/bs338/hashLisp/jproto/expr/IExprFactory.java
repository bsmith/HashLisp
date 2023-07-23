package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface IExprFactory {
    @NotNull IExpr wrap(HonsValue val);
    
    @NotNull ISimpleExpr nil();
    
    @NotNull ISimpleExpr makeSmallInt(int num);

    @NotNull IConsExpr cons(@NotNull IExpr left, @NotNull IExpr right);

    @NotNull ISymbolExpr makeSymbol(@NotNull IConsExpr name);

    @NotNull ISymbolExpr makeSymbol(@NotNull String name);

    @NotNull ISymbolExpr makeSymbol(@NotNull Tag tag);
}
