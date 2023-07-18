package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;
import java.util.Optional;

/* XXX: make this just a specialisation of WrappedHeap/WrappedValue! */
public class ExprFactory {
    protected final @NotNull HonsHeap heap;
    protected final @NotNull HonsValue lambdaTag;
    protected final @NotNull HonsValue blackholeSentinel;
    
    public ExprFactory(@NotNull HonsHeap heap) {
        this.heap = heap;
        lambdaTag = heap.makeSymbol("*lambda");
        blackholeSentinel = heap.makeSymbol("***BLACKHOLE");
    }

    public @NotNull HonsHeap getHeap() {
        return heap;
    }
    
    /* classify the Expr for the purposes of the evaluator
     *   simple: nil, smallInt, symbol
     *   application: any other cons-ref
     */
    public @NotNull IExpr wrap(@NotNull HonsValue value) {
        if (value.isConsRef()) {
            if (heap.isSymbol(value))
                return new SymbolExpr(value);
            return new ConsExpr(value);
        }
        return new SimpleExpr(value);
    }

    @Contract("null -> null; !null -> !null")
    public HonsValue unwrap(IExpr wrapped) {
        if (wrapped == null)
            return null;
        if (!(wrapped instanceof ExprBase))
            throw new IllegalArgumentException("Unwrapping IExpr which is not ExprFactory.ExprBase");
        if (heap != ((ExprBase)wrapped).getHeap())
            throw new IllegalArgumentException("Mismatched heap between IExpr and ExprFactory");
        return wrapped.getValue();
    }
    
    public @NotNull IExpr getBlackholeSentinel() {
        return wrap(blackholeSentinel);
    }
    
    public @NotNull IConsExpr cons(@NotNull IExpr left, @NotNull IExpr right) {
        return wrap(heap.cons(unwrap(left), unwrap(right))).asConsExpr();
    }
    
    public @NotNull ISimpleExpr nil() {
        return wrap(HonsValue.nil).asSimpleExpr();
    }
    
    public @NotNull ISymbolExpr makeSymbol(@NotNull IConsExpr name) {
        return wrap(heap.makeSymbol(unwrap(name))).asSymbolExpr();
    }
    
    public @NotNull ISymbolExpr makeSymbol(@NotNull String name) {
        return wrap(heap.makeSymbol(name)).asSymbolExpr();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExprFactory that = (ExprFactory) o;
        return Objects.equals(heap, that.heap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heap);
    }

    private abstract class ExprBase implements IExpr {
        protected final @NotNull HonsValue value;

        private ExprBase(@NotNull HonsValue value) {
            this.value = value;
        }
        
        public @NotNull HonsHeap getHeap() {
            return heap;
        }
        
        public @NotNull HonsValue getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExprBase exprBase = (ExprBase) o;
            return Objects.equals(value, exprBase.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public @NotNull String valueToString() {
            return heap.valueToString(value);
        }
    }
    
    public class SimpleExpr extends ExprBase implements ISimpleExpr {
        private SimpleExpr(@NotNull HonsValue value) {
            super(value);
        }
        
        @Override public boolean isSimple() {
            return true;
        }

        @Override public void visit(IExprVisitor2 visitor) {
            visitor.visitSimple(this);
        }
    }
    
    public class SymbolExpr extends SimpleExpr implements ISymbolExpr {
        private SymbolExpr(@NotNull HonsValue value) {
            super(value);
            assert heap.isSymbol(value);
        }
        
        @Override public boolean isSymbol() {
            return true;
        }

        @Override public void visit(IExprVisitor2 visitor) {
            visitor.visitSymbol(this);
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
        public boolean isLambdaTag() { return value.equals(lambdaTag); }
        
        @Override
        public boolean isBlackholeSentinel() { return value.equals(blackholeSentinel); }
    }
    
    /* XXX how is this different from ConsPair?! */
    public class ConsExpr extends ExprBase implements IConsExpr {
        private final @NotNull IExpr fst;
        private final @NotNull IExpr snd;

        private ConsExpr(@NotNull HonsValue value) {
            super(value);
            assert value.isConsRef();
            /* We can't do this because ExprBase doesn't implement IValue and ConsPair is strict */
//            var uncons = heap.uncons(value).<ExprBase>fmap(ExprFactory.this::of);
            var uncons = heap.uncons(value);
            fst = wrap(uncons.fst());
            snd = wrap(uncons.snd());
        }

        @Override public boolean isCons() {
            return true;
        }

        @Override public void visit(IExprVisitor2 visitor) {
            visitor.visitCons(this);
        }

        @Override
        public @NotNull IExpr fst() {
            return fst;
        }

        @Override
        public @NotNull IExpr snd() {
            return snd;
        }

        @Override
        public @NotNull Optional<IExpr> getMemoEval() {
            return heap.getMemoEval(value).map(ExprFactory.this::wrap);
        }

        @Override
        public void setMemoEval(IExpr expr) {
            var memo = unwrap(expr);
            heap.setMemoEval(value, memo);
        }

        @Override
        public boolean isLambda() {
            return fst().isLambdaTag();
        }
    }
}
