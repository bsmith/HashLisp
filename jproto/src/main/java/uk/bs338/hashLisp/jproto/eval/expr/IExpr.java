package uk.bs338.hashLisp.jproto.eval.expr;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface IExpr {
    HonsValue getValue();
    
    default boolean isSimple() {
        return false;
    }

    default boolean isSymbol() {
        return false;
    }

    default boolean isCons() {
        return false;
    }

    void visit(IExprVisitor2 visitor);
}
