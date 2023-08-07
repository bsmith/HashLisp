package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.ValueType;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;
import java.util.Optional;

abstract class ExprBase implements IExpr {
    protected final @NotNull HonsMachine machine;
    protected final @NotNull HonsValue value;

    private ExprBase(@NotNull HonsMachine machine, @NotNull HonsValue value) {
        this.machine = machine;
        this.value = value;
    }

    public @NotNull HonsMachine getMachine() {
        return machine;
    }

    public @NotNull HonsValue getValue() {
        return value;
    }

    protected IExpr wrap(HonsValue value) {
        return IExpr.wrap(machine, value);
    }

    @Contract("null -> null; !null -> !null")
    protected HonsValue unwrap(IExpr wrapped) {
        if (wrapped == null)
            return null;
        if (machine != wrapped.getMachine())
            throw new IllegalArgumentException("Mismatched machine between IExpr objects");
        return wrapped.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprBase exprBase = (ExprBase) o;
        return Objects.equals(getMachine(), exprBase.getMachine()) && Objects.equals(getValue(), exprBase.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMachine(), getValue());
    }

    @Override
    public @NotNull String valueToString() {
        return machine.valueToString(value);
    }


    public static class SimpleExpr extends ExprBase {
        SimpleExpr(@NotNull HonsMachine machine, @NotNull HonsValue value) {
            super(machine, value);
        }

        @Override
        public ExprType getType() {
            return switch (value.getType()) {
                case NIL -> ExprType.NIL;
                case SMALL_INT -> ExprType.SMALL_INT;
                default -> throw new IllegalStateException("SimpleExpr cannot wrap a " + value.getType());
            };
        }

        @Override
        public boolean isNormalForm() {
            return true;
        }

        @Override public <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor) {
            visitor.visitSimple(this);
            return visitor;
        }
    }

    public static class SymbolExpr extends ExprBase implements ISymbolExpr {
        SymbolExpr(@NotNull HonsMachine machine, @NotNull HonsValue value) {
            super(machine, value);
            assert machine.isSymbol(value);
        }

        @Override
        public ExprType getType() {
            return ExprType.SYMBOL;
        }

        @Override
        public boolean isNormalForm() {
            return true;
        }

        @Override public <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor) {
            visitor.visitSymbol(this);
            return visitor;
        }

        @Override
        public @NotNull IConsExpr symbolName() {
            return (IConsExpr)wrap(machine.symbolName(value));
        }

        @Override
        public @NotNull String symbolNameAsString() {
            return machine.symbolNameAsString(value);
        }

        @Override
        public boolean isDataHead() {
            return machine.fst(machine.symbolName(value)).toSmallInt() == '*';
        }

        @Override
        public ISymbolExpr makeDataHead() {
            if (isDataHead())
                return this;
            return wrap(machine.makeSymbol(machine.cons(HonsValue.fromSmallInt('*'), machine.symbolName(value)))).asSymbolExpr();
        }

        @Override
        public boolean isTag(Tag tag) {
            /* XXX slow implementation: add Tag cache to IMachine! */
            return value.equals(machine.makeSymbol(tag.getSymbolStr()));
        }
    }

    public static class ConsExpr extends ExprBase implements IConsExpr {
        private final ConsPair<HonsValue> uncons;
        private IExpr fst;
        private IExpr snd;

        ConsExpr(@NotNull HonsMachine machine, @NotNull HonsValue value) {
            super(machine, value);
            assert value.getType() == ValueType.CONS_REF;
            uncons = machine.uncons(value);
            /* Be lazy about further wrapping */
            fst = null;
            snd = null;
        }

        @Override
        public ExprType getType() {
            return ExprType.CONS;
        }

        @Override public <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor) {
            visitor.visitCons(this);
            return visitor;
        }

        @Override
        public @NotNull IExpr fst() {
            if (fst == null)
                fst = wrap(uncons.fst());
            return fst;
        }

        @Override
        public @NotNull IExpr snd() {
            if (snd == null)
                snd = wrap(uncons.snd());
            return snd;
        }

        @Override
        public @NotNull Optional<IExpr> getMemoEval() {
            return machine.getMemoEval(value).map(this::wrap);
        }

        @Override
        public void setMemoEval(@Nullable IExpr expr) {
            var memo = expr == null ? null : unwrap(expr);
            machine.setMemoEval(value, memo);
        }

        @Override
        public boolean hasHeadTag(Tag tag) {
            if (fst().getType() == ExprType.SYMBOL)
                return fst().asSymbolExpr().isTag(tag);
            else
                return false;
        }
    }
}
