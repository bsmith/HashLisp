package uk.bs338.hashLisp.jproto.eval.expr;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface ISimpleExpr extends IExpr {
    @Override
    default boolean isNormalForm() {
        return true;
    }

    @Override
    default boolean isHeadNormalForm() {
        return true;
    }

    @Override
    default ISimpleExpr asSimpleExpr() {
        return (ISimpleExpr)this;
    }
}
