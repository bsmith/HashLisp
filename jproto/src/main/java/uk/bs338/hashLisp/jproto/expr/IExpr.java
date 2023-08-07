package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.NoSuchElementException;

public interface IExpr {
    @NotNull HonsValue getValue();
    default @NotNull HonsMachine getMachine() { throw new NoSuchElementException(); }

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

    /* classify the Expr for the purposes of the evaluator
     *   simple: nil, smallInt, symbol
     *   application: any other cons-ref
     */
    static @NotNull IExpr wrap(@NotNull HonsMachine machine, @NotNull HonsValue value) {
        if (value.isConsRef()) {
            if (machine.isSymbol(value))
                return new ExprBase.SymbolExpr(machine, value);
            return new ExprBase.ConsExpr(machine, value);
        }
        return new ExprBase.SimpleExpr(machine, value);
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
