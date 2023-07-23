package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;

public interface IExpr {
    HonsValue getValue();
    default HonsHeap getHeap() { throw new NoSuchElementException(); }

    /* XXX: use enum instead of isSimple+isSymbol+isCons as a only-one-may-be-true */
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

    IExpr nil = new ISimpleExpr() {
        @Override
        public HonsValue getValue() {
            return HonsValue.nil;
        }

        @Override
        public <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor) {
            visitor.visitSimple(this);
            return visitor;
        }

        @Override
        public @NotNull String valueToString() {
            return HonsValue.nil.toString();
        }
    };
    
    static IExpr ofSmallInt(int num) {
        return new ISimpleExpr() {
            @Override
            public HonsValue getValue() {
                return HonsValue.fromSmallInt(num);
            }

            @Override
            public <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor) {
                visitor.visitSimple(this);
                return visitor;
            }

            @Override
            public @NotNull String valueToString() {
                return getValue().toString();
            }
        };
    }
}
