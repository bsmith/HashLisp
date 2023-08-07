package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;

public interface IExpr {
    @NotNull HonsValue getValue();
    default @NotNull HonsHeap getHeap() { throw new NoSuchElementException(); }

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

    @SuppressWarnings("UnusedReturnValue")
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

    /* classify the Expr for the purposes of the evaluator
     *   simple: nil, smallInt, symbol
     *   application: any other cons-ref
     */
    static @NotNull IExpr wrap(@NotNull HonsHeap heap, @NotNull HonsValue value) {
        if (value.isConsRef()) {
            if (heap.isSymbol(value))
                return new ExprBase.SymbolExpr(heap, value);
            return new ExprBase.ConsExpr(heap, value);
        }
        return new ExprBase.SimpleExpr(heap, value);
    }
    
    static @NotNull IConsExpr cons(@NotNull IExpr left, @NotNull IExpr right) {
        HonsHeap heap = left.getHeap();
        if (heap != right.getHeap())
            throw new IllegalArgumentException("Mismatched heaps between left IExpr and right IExpr");
        return wrap(heap, heap.cons(left.getValue(), right.getValue())).asConsExpr();
    }
}
