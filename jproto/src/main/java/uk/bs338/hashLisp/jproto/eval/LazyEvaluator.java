package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.eval.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.eval.expr.IConsExpr;
import uk.bs338.hashLisp.jproto.eval.expr.IExpr;
import uk.bs338.hashLisp.jproto.eval.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.function.Supplier;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator implements IEvaluator<HonsValue> {
    private final @NotNull HonsHeap heap;
    private final @NotNull Primitives primitives;
    private final @NotNull ArgSpecCache argSpecCache;
    private final @NotNull ExprFactory exprFactory;
    private boolean debug;

    public LazyEvaluator(@NotNull HonsHeap heap) {
        this.heap = heap;
        primitives = new Primitives(heap);
        argSpecCache = new ArgSpecCache(heap);
        exprFactory = new ExprFactory(heap);
        debug = false;
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }

    public @NotNull IExpr applyPrimitive(@NotNull ISymbolExpr function, @NotNull IExpr args) throws EvalException {
        var prim = primitives.get(function.getValue());
        if (prim.isEmpty()) {
            /* if the symbol starts with a *, then treat it a data head
             * otherwise, treat as a strict constructor.
             *   This means evaluate the args, and then prepend the *
             */
            if (function.isDataHead())
                return exprFactory.wrap(heap.cons(function.getValue(), args.getValue()));
            var constrArgs = unmakeList(heap, args.getValue());
            constrArgs = eval_multi(constrArgs);
            var starredSymbol = heap.makeSymbol(heap.cons(heap.makeSmallInt('*'), function.symbolName().getValue()));
            return exprFactory.wrap(heap.cons(starredSymbol, makeList(heap, constrArgs.toArray(new HonsValue[0]))));
        }
        try {
            return exprFactory.wrap(prim.get().apply(this, args.getValue()));
        }
        catch (EvalException e) {
            e.setPrimitive(function.symbolNameAsString());
            e.setCurrentlyEvaluating(function.valueToString());
            throw e;
        }
    }

    public @NotNull IExpr applyLambda(@NotNull IConsExpr lambda, @NotNull IExpr args) throws EvalException {
        IExpr argSpec = lambda.snd().asConsExpr().fst();
        IExpr body = lambda.snd().asConsExpr().snd().asConsExpr().fst();
        
        var assignments = argSpecCache.match(argSpec.getValue(), args.getValue());
        
        var result = assignments.substitute(body.getValue());
        return eval_expr(exprFactory.wrap(result));
    }

    public IExpr apply(@NotNull IExpr function, @NotNull IExpr args) throws EvalException {
        if (debug)
            System.out.printf("%sapply %s to %s%n", evalIndent, function.valueToString(), args.valueToString());
        var head = eval_expr(function);
        if (head.isSymbol()) {
            return applyPrimitive(head.asSymbolExpr(), args);
        }
        else if (head.isLambda()) {
            return applyLambda(head.asConsExpr(), args);
        }
        else {
            var e = new EvalException("Cannot apply something that is not a symbol or lambda");
            e.setCurrentlyEvaluating(function.valueToString());
            throw e;
        }
    }

    String evalIndent = "";
    <T> T withEvalIndent(Supplier<T> func) {
        T result;
        var savedIndent = evalIndent;
        evalIndent += "  ";
        try {
            result = func.get();
        }
        finally {
            evalIndent = savedIndent;
        }
        return result;
    }
    
    public @NotNull IExpr eval_cons(@NotNull IConsExpr expr) {
        
        /* Do this early */
        var memoEval = expr.getMemoEval();
        if (memoEval.isPresent()) {
            if (memoEval.get().isBlackholeSentinel())
                throw new IllegalStateException("Encountered blackhole when evaluating");
            return memoEval.get();
        }
        
        expr.setMemoEval(exprFactory.getBlackholeSentinel());
        
        IExpr result;
        try {
            if (debug) {
                System.out.printf("%seval: %s%n", evalIndent, expr.valueToString());
            }

            result = withEvalIndent(() -> {
                try {
                    return apply(expr.fst(), expr.snd());
                } catch (EvalException e) {
                    throw new RuntimeException("Exception during apply in eval", e); /* XXX */
                }
            });

            if (debug) {
                System.out.printf("%s==> %s%n", evalIndent, result.valueToString());
            }
        }
        finally {
            var prevMemo = expr.getMemoEval();
            if (prevMemo.isEmpty() || !prevMemo.get().isBlackholeSentinel())
                //noinspection ThrowFromFinallyBlock
                throw new AssertionError("Didn't find blackhole sentinel when expected");
        }

        expr.setMemoEval(result);
        return result;
    }
    
    public @NotNull IExpr eval_expr(@NotNull IExpr expr) {
        /* Simple values are already in normal form */
        if (expr.isNormalForm())
            return expr;
        
        assert expr.isCons();
        var consExpr = expr.asConsExpr();
        return eval_cons(consExpr);
    }
    
    public @NotNull HonsValue eval_one(@NotNull HonsValue val) {
        var expr = exprFactory.wrap(val);
        return eval_expr(expr).getValue();
    }

    public static void demo(@NotNull HonsHeap heap) {
        System.out.println("Evaluator demo");
        
        var evaluator = new LazyEvaluator(heap);
        
        var add = heap.makeSymbol("add");
        var program = makeList(heap,
            add,
            heap.makeSmallInt(5),
            makeList(heap,
                add,
                heap.makeSmallInt(2),
                heap.makeSmallInt(3)
            )
        );
        System.out.println(heap.valueToString(program));

        System.out.println(heap.valueToString(evaluator.eval_one(program)));
    }
}
