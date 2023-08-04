package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.eval.Tag;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;
import java.util.Optional;

abstract class ExprBase implements IExpr {
    protected final @NotNull HonsHeap heap;
    protected final @NotNull HonsValue value;

    private ExprBase(@NotNull HonsHeap heap, @NotNull HonsValue value) {
        this.heap = heap;
        this.value = value;
    }

    public @NotNull HonsHeap getHeap() {
        return heap;
    }

    public @NotNull HonsValue getValue() {
        return value;
    }

    protected IExpr wrap(HonsValue value) {
        return IExpr.wrap(heap, value);
    }

    @Contract("null -> null; !null -> !null")
    protected HonsValue unwrap(IExpr wrapped) {
        if (wrapped == null)
            return null;
        if (wrapped.isSimple())
            return wrapped.getValue();
        if (heap != wrapped.getHeap())
            throw new IllegalArgumentException("Mismatched heap between IExpr objects");
        return wrapped.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprBase exprBase = (ExprBase) o;
        return Objects.equals(getHeap(), exprBase.getHeap()) && Objects.equals(getValue(), exprBase.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHeap(), getValue());
    }

    @Override
    public @NotNull String valueToString() {
        return heap.valueToString(value);
    }


    public static class SimpleExpr extends ExprBase implements ISimpleExpr {
        SimpleExpr(@NotNull HonsHeap heap, @NotNull HonsValue value) {
            super(heap, value);
        }

        @Override public boolean isSimple() {
            return true;
        }

        @Override public <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor) {
            visitor.visitSimple(this);
            return visitor;
        }
    }

    public static class SymbolExpr extends SimpleExpr implements ISymbolExpr {
        SymbolExpr(@NotNull HonsHeap heap, @NotNull HonsValue value) {
            super(heap, value);
            assert heap.isSymbol(value);
        }

        @Override public boolean isSymbol() {
            return true;
        }

        @Override public <V extends IExprVisitor> @NotNull V visit(@NotNull V visitor) {
            visitor.visitSymbol(this);
            return visitor;
        }

        @Override
        public @NotNull IConsExpr symbolName() {
            return (IConsExpr)wrap(heap.symbolName(value));
        }

        @Override
        public @NotNull String symbolNameAsString() {
            return heap.symbolNameAsString(value);
        }

        @Override
        public boolean isDataHead() {
            return heap.fst(heap.symbolName(value)).toSmallInt() == '*';
        }

        @Override
        public ISymbolExpr makeDataHead() {
            if (isDataHead())
                return this;
            return wrap(heap.makeSymbol(heap.cons(HonsValue.fromSmallInt('*'), heap.symbolName(value)))).asSymbolExpr();
        }

        @Override
        public boolean isTag(Tag tag) {
            /* XXX slow implementation */
            return value.equals(heap.makeSymbol(tag.getSymbolStr()));
//            throw new Error("unimplemented");
//            return value.equals(makeSymbol(tag).getValue());
        }
    }

    /* XXX how is this different from ConsPair?! */
    public static class ConsExpr extends ExprBase implements IConsExpr {
        private final ConsPair<HonsValue> uncons;
        private IExpr fst;
        private IExpr snd;

        ConsExpr(@NotNull HonsHeap heap, @NotNull HonsValue value) {
            super(heap, value);
            assert value.isConsRef();
            /* We can't do this because ExprBase doesn't implement IValue and ConsPair is strict */
//            var uncons = heap.uncons(value).<ExprBase>fmap(ExprFactory.this::of);
            uncons = heap.uncons(value);
            /* Be lazy about further wrapping */
            fst = null;
            snd = null;
        }

        @Override public boolean isCons() {
            return true;
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
            return heap.getMemoEval(value).map(this::wrap);
        }

        @Override
        public void setMemoEval(@Nullable IExpr expr) {
            var memo = expr == null ? null : unwrap(expr);
            heap.setMemoEval(value, memo);
        }

        @Override
        public boolean hasHeadTag(Tag tag) {
            return fst().isTag(tag);
        }
    }
}
