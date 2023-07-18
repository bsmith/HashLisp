package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;

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

    boolean isNormalForm();
    boolean isHeadNormalForm();

    default boolean isLambda() {
        return false;
    }

    default boolean isLambdaTag() {
        return false;
    }

    default boolean isBlackholeSentinel() {
        return false;
    }

    /* XXX is this the best exception?  I just copied Optional/ReadResult */
    default ISimpleExpr asSimpleExpr() {
        throw new NoSuchElementException();
    }

    default ISymbolExpr asSymbolExpr() {
        throw new NoSuchElementException();
    }

    default IConsExpr asConsExpr() {
        throw new NoSuchElementException();
    }

    @NotNull String valueToString();
}
