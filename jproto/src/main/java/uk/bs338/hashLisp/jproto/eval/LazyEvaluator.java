package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.eval.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.eval.expr.IConsExpr;
import uk.bs338.hashLisp.jproto.eval.expr.IExpr;
import uk.bs338.hashLisp.jproto.eval.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.*;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator implements IEvaluator<HonsValue> {
    private final @NotNull HonsHeap heap;
    private final @NotNull ExprFactory exprFactory;
    private final @NotNull Primitives primitives;
    private final @NotNull ArgSpecCache argSpecCache;
    private final @NotNull ISymbolExpr blackholeSentinel;
    private boolean debug;

    public LazyEvaluator(@NotNull HonsHeap heap) {
        this.heap = heap;
        exprFactory = new ExprFactory(heap);
        primitives = new Primitives(heap);
        argSpecCache = new ArgSpecCache(exprFactory);
        blackholeSentinel = exprFactory.makeSymbol(Tag.BLACKHOLE);
        debug = false;
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }
    
    private IExpr wrap(HonsValue value) {
        return exprFactory.wrap(value);
    }

    /* If applyPrimitive needs to evaluate anything, it should call eval recursively */
    public @NotNull IExpr applyPrimitive(@NotNull ISymbolExpr function, @NotNull IExpr args) throws EvalException {
        var prim = primitives.get(function.getValue());
        if (prim.isEmpty()) {
            /* if the symbol starts with a *, then treat it a data head
             * otherwise, treat as a strict constructor.
             *   This means evaluate the args, and then prepend the *
             */
            if (function.isDataHead())
                return wrap(heap.cons(function.getValue(), args.getValue()));
            
            /* recursively evaluate */
            var constrArgs = unmakeList(heap, args.getValue());
            constrArgs = eval_multi(constrArgs);
            
            var starredSymbol = function.makeDataHead();
            return wrap(heap.cons(starredSymbol.getValue(), makeList(heap, constrArgs.toArray(new HonsValue[0]))));
        }
        try {
            /* may recursively evaluate */
            return wrap(prim.get().apply(this, args.getValue()));
        }
        catch (EvalException e) {
            e.setPrimitive(function.symbolNameAsString());
            e.setCurrentlyEvaluating(function.valueToString());
            throw e;
        }
    }

    public IExpr substitute(@NotNull Assignments assignments, @NotNull IExpr body) {
        return SubstituteVisitor.substitute(exprFactory, primitives, this, assignments, body);
    }

    /* result needs further evaluation */
    public @NotNull IExpr applyLambdaOnce(@NotNull IConsExpr lambda, @NotNull IExpr args) throws EvalException {
        IExpr argSpec = lambda.snd().asConsExpr().fst();
        IExpr body = lambda.snd().asConsExpr().snd().asConsExpr().fst();
        
        var assignments = argSpecCache.match(argSpec.getValue(), args.getValue());

        return substitute(assignments, body);
    }

    /* result needs further evaluation */
    public IExpr apply_hnf(@NotNull IExpr function, @NotNull IExpr args) throws EvalException {
        if (debug)
            System.out.printf("%sapply %s to %s%n", evalIndent, function.valueToString(), args.valueToString());
        if (!function.isNormalForm())
            throw new EvalException("apply_hnf called but function not in normal form");
        if (function.isSymbol()) {
            return applyPrimitive(function.asSymbolExpr(), args);
        }
        else if (function.hasHeadTag(Tag.LAMBDA)) {
            return applyLambdaOnce(function.asConsExpr(), args);
        }
        else {
            var e = new EvalException("Cannot apply something that is not a symbol or lambda");
            e.setCurrentlyEvaluating(function.valueToString());
            throw e;
        }
    }
    
    String evalIndent = "";

    private class EvalIndenter implements AutoCloseable {
        private final String savedIndent;
        
        public EvalIndenter() {
            this("  ");
        }

        public EvalIndenter(String indent) {
            this.savedIndent = evalIndent;
            evalIndent += indent;
        }

        @Override
        public void close() {
            evalIndent = savedIndent;
        }
    }

    private @NotNull Optional<IExpr> getMemoEvalCheckingForBlackhole(@NotNull IConsExpr expr) {
        var memoEval = expr.getMemoEval();
        if (memoEval.isPresent()) {
            if (memoEval.get().equals(blackholeSentinel))
                throw new IllegalStateException("Encountered blackhole when evaluating");
        }
        return memoEval;
    }
    
    public Optional<IExpr> evaluateIfNeeded(EvaluationQueue evaluationQueue, IExpr function) {
        /* Ensure the head is evaluated to normal form first */
        if (!function.isNormalForm()) {
            /* we need to evaluate the head first! */
            assert function.isCons(); /* !isNormalForm() => isCons() */

            var memoEval = getMemoEvalCheckingForBlackhole(function.asConsExpr());
            if (memoEval.isEmpty()) {
                if (debug)
                    System.out.printf("%s  not in nf: pushing %s%n", evalIndent, function.valueToString());
                evaluationQueue.pushNeededEvaluation(function.asConsExpr());
                return Optional.empty();
            } else {
                return memoEval;
            }
        }
        return Optional.of(function);
    }
    
    public @NotNull IExpr eval_cons(@NotNull IConsExpr origExpr) throws EvalException {
        {
            /* Do this early */
            var memoEval = getMemoEvalCheckingForBlackhole(origExpr);
            if (memoEval.isPresent())
                return memoEval.get();
        }

        try (final EvaluationQueue evaluationQueue = new EvaluationQueue(blackholeSentinel)) {
            evaluationQueue.pushNeededEvaluation(origExpr);
            
            while (evaluationQueue.hasEntries()) {
                var frame = evaluationQueue.getCurrentFrame();
                IConsExpr expr = frame.getOrigExpr();
                IExpr applied = frame.getApplyResult();
                Optional<IExpr> result;

                if (debug) {
                    System.out.printf("%seval/%d: %s%n", evalIndent, evaluationQueue.size(), expr.valueToString());
                }

                if (applied == null) {
                    Optional<IExpr> function = evaluateIfNeeded(evaluationQueue, expr.fst());
                    if (function.isEmpty())
                        continue;

                    /* actually perform the application */
                    try (var ignored = new EvalIndenter()) {
                        applied = apply_hnf(function.get(), expr.snd());
                        if (applied.isCons()) /* XXX */
                            frame.setApplyResult(applied.asConsExpr());
                    }
                }
                
                result = evaluateIfNeeded(evaluationQueue, applied);
                if (result.isEmpty())
                    continue;

                if (!result.get().isNormalForm())
                    throw new AssertionError("result not normal form");

                evaluationQueue.finishEvaluation(frame, result.get());

                if (debug) {
                    System.out.printf("%s==> %s%n", evalIndent, result.get().valueToString());
                }
            }
        }
        
        var memoEval = getMemoEvalCheckingForBlackhole(origExpr);
        if (memoEval.isEmpty())
            throw new AssertionError("Didn't find memoised evaluation result");
        return memoEval.get();
    }
    
    public @NotNull IExpr eval_expr(@NotNull IExpr expr) throws EvalException {
        /* Simple values are already in normal form */
        if (expr.isNormalForm())
            return expr;
        
        assert expr.isCons();
        var consExpr = expr.asConsExpr();
        var result = eval_cons(consExpr);
        if (!result.isNormalForm())
            throw new AssertionError("expression not evaluated to normal form");
        return result;
    }
    
    public @NotNull HonsValue eval_one(@NotNull HonsValue val) {
        var expr = wrap(val);
        try {
            return eval_expr(expr).getValue();
        }
        catch (EvalException e) { /* XXX */
            e.printStackTrace();
            return heap.cons(heap.makeSymbol("error"), HonsValue.nil);
        }
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
