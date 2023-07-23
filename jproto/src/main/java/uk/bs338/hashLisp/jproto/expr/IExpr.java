package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.wrapped.IWrappedSymbol;
import uk.bs338.hashLisp.jproto.wrapped.IWrappedValue;

import java.util.NoSuchElementException;

public interface IExpr extends IWrappedValue, IWrappedValue.IGetValue<HonsValue> {
    
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

        @Override
        public IWrappedSymbol makeSymbol() {
            throw new UnsupportedOperationException("Cannot create a symbol from nil");
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

            @Override
            public IWrappedSymbol makeSymbol() {
                throw new UnsupportedOperationException("Cannot create a symbol from SmallInt");
            }
        };
    }
}
