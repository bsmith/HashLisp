package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.wrapped.IWrappedValue2;

import java.util.NoSuchElementException;

public interface IExpr extends IWrappedValue2, IWrappedValue2.IGetValue<HonsValue> {
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

    @Contract("_->param1")
    <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor);

    boolean isNormalForm();
    boolean isHeadNormalForm();

    default boolean hasHeadTag(Tag tag) {
        return false;
    }

    default boolean isTag(Tag tag) {
        return false;
    }
    
    /* XXX is this the best exception?  I just copied Optional/ReadResult */
    default ISimpleExpr asSimple() {
        throw new NoSuchElementException();
    }

    default ISymbolExpr asSymbol() {
        throw new NoSuchElementException();
    }

    default IConsExpr asCons() {
        throw new NoSuchElementException();
    }

    @NotNull String valueToString();
}
