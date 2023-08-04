package uk.bs338.hashLisp.jproto.expr;

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
        return this;
    }
}
