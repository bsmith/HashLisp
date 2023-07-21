package uk.bs338.hashLisp.jproto.expr;

public interface IExprVisitor {
    void visitSimple(ISimpleExpr simpleExpr);

    default void visitSymbol(ISymbolExpr symbolExpr) {
        visitSimple(symbolExpr);
    }

    void visitCons(IConsExpr consExpr);
}
