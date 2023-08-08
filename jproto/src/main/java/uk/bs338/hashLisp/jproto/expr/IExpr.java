package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;

public interface IExpr {
    @NotNull HonsValue getValue();
    @NotNull HonsMachine getMachine();
    
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
    static @NotNull IExpr wrap(@NotNull HonsMachine machine, @NotNull HonsValue value) {
        return switch (value.getType()) {
            case NIL, SMALL_INT -> new ExprBase.SimpleExpr(machine, value);
            case SYMBOL_TAG -> throw new IllegalArgumentException("Cannot wrap a symbol-tag!");
            case CONS_REF ->
                machine.isSymbol(value) ?
                    new ExprBase.SymbolExpr(machine, value) :
                new ExprBase.ConsExpr(machine, value);
        };
    }
    
    static @NotNull IExpr nil(@NotNull HonsMachine machine) {
        return IExpr.wrap(machine, HonsValue.nil);
    }
    
    static @NotNull IExpr ofSmallInt(@NotNull HonsMachine machine, int num) {
        return IExpr.wrap(machine, HonsValue.fromSmallInt(num));
    }
    
    static @NotNull IConsExpr cons(@NotNull IExpr left, @NotNull IExpr right) {
        HonsMachine machine = left.getMachine();
        if (machine != right.getMachine())
            throw new IllegalArgumentException("Mismatched machines between left IExpr and right IExpr");
        return wrap(machine, machine.cons(left.getValue(), right.getValue())).asConsExpr();
    }
}
