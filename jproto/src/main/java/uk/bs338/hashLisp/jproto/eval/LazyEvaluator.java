package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.eval.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.function.Supplier;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator implements IEvaluator<HonsValue> {
    private final @NotNull HonsHeap heap;
    private final @NotNull Primitives primitives;
    private final @NotNull ArgSpecCache argSpecCache;
    private final @NotNull ExprFactory exprFactory;
    private final @NotNull HonsValue lambdaTag;
    private final @NotNull HonsValue blackholeSentinel;
    private boolean debug;

    public LazyEvaluator(@NotNull HonsHeap heap) {
        this.heap = heap;
        primitives = new Primitives(heap);
        argSpecCache = new ArgSpecCache(heap);
        exprFactory = new ExprFactory(heap);
        lambdaTag = heap.makeSymbol("*lambda");
        blackholeSentinel = heap.makeSymbol("***BLACKHOLE");
        debug = false;
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }

    public boolean isLambda(@NotNull HonsValue value) {
        if (!value.isConsRef())
            return false;
        return heap.fst(value).equals(lambdaTag);
    }

    public @NotNull HonsValue applyPrimitive(@NotNull HonsValue function, @NotNull HonsValue args) throws EvalException {
        var prim = primitives.get(function);
        if (prim.isEmpty()) {
            /* if the symbol starts with a *, then treat it a data head
             * otherwise, treat as a strict constructor.
             *   This means evaluate the args, and then prepend the *
             */
            if (heap.fst(heap.symbolName(function)).toSmallInt() == '*')
                return heap.cons(function, args);
            var constrArgs = unmakeList(heap, args);
            constrArgs = eval_multi(constrArgs);
            var starredSymbol = heap.makeSymbol(heap.cons(heap.makeSmallInt('*'), heap.symbolName(function)));
            return heap.cons(starredSymbol, makeList(heap, constrArgs.toArray(new HonsValue[0])));
        }
        try {
            return prim.get().apply(this, args);
        }
        catch (EvalException e) {
            e.setPrimitive(heap.symbolNameAsString(function));
            e.setCurrentlyEvaluating(heap.valueToString(function));
            throw e;
        }
    }

    public @NotNull HonsValue applyLambda(@NotNull HonsValue lambda, @NotNull HonsValue args) throws EvalException {
        HonsValue argSpec = heap.fst(heap.snd(lambda));
        HonsValue body = heap.fst(heap.snd(heap.snd(lambda)));
        
        var assignments = argSpecCache.match(argSpec, args);
        
        var result = assignments.substitute(body);
        return eval_one(result);
    }

    public HonsValue apply(@NotNull HonsValue function, @NotNull HonsValue args) throws EvalException {
        if (debug)
            System.out.printf("%sapply %s to %s%n", evalIndent, heap.valueToString(function), heap.valueToString(args));
        var head = eval_one(function);
        if (heap.isSymbol(head)) {
            return applyPrimitive(head, args);
        }
        else if (isLambda(head)) {
            return applyLambda(head, args);
        }
        else {
            var e = new EvalException("Cannot apply something that is not a symbol or lambda");
            e.setCurrentlyEvaluating(heap.valueToString(function));
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
    
    public @NotNull HonsValue eval_one(@NotNull HonsValue val) {
        var expr = exprFactory.wrap(val);
        
        /* Simple values are already in normal form */
        if (expr.isNormalForm())
            return val;
        assert val.isConsRef();
        var consExpr = expr.asConsExpr();
        
        /* Do this early */
        var memoEval = consExpr.getMemoEval();
        if (memoEval.isPresent()) {
            if (memoEval.get().getValue().equals(blackholeSentinel))
                throw new IllegalStateException("Encountered blackhole when evaluating");
            return memoEval.get().getValue();
        }
        
        consExpr.setMemoEval(exprFactory.wrap(blackholeSentinel));
        
        HonsValue result;
        try {
            if (debug) {
                System.out.printf("%seval: %s%n", evalIndent, heap.valueToString(val));
            }

            result = withEvalIndent(() -> {
                try {
                    return apply(consExpr.fst().getValue(), consExpr.snd().getValue());
                } catch (EvalException e) {
                    throw new RuntimeException("Exception during apply in eval", e); /* XXX */
                }
            });

            if (debug) {
                System.out.printf("%s==> %s%n", evalIndent, heap.valueToString(result));
            }        
        }
        finally {
            var prevMemo = heap.getMemoEval(val);
            if (prevMemo.isPresent() && !prevMemo.get().equals(blackholeSentinel))
                //noinspection ThrowFromFinallyBlock
                throw new AssertionError("Didn't find blackhole sentinel when expected");
        }

        heap.setMemoEval(val, result);
        return result;
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
