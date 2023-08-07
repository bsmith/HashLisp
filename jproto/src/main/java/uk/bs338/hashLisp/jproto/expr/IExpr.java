package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;

public interface IExpr {
    @NotNull HonsValue getValue();
    @NotNull HonsHeap getHeap();
    
    ExprType getType();

    @SuppressWarnings("UnusedReturnValue")
    @Contract("_->param1")
    <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor);

    boolean isNormalForm();
    
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
        return switch (value.getType()) {
            case NIL, SMALL_INT -> new ExprBase.SimpleExpr(heap, value);
            case SYMBOL_TAG -> throw new IllegalArgumentException("Cannot wrap a symbol-tag!");
            case CONS_REF ->
                heap.isSymbol(value) ?
                    new ExprBase.SymbolExpr(heap, value) :
                new ExprBase.ConsExpr(heap, value);
        };
    }
    
    static @NotNull IConsExpr cons(@NotNull IExpr left, @NotNull IExpr right) {
        HonsHeap heap = left.getHeap();
        if (heap != right.getHeap())
            throw new IllegalArgumentException("Mismatched heaps between left IExpr and right IExpr");
        return wrap(heap, heap.cons(left.getValue(), right.getValue())).asConsExpr();
    }
}
