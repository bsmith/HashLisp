package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.expr.IConsExpr;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.*;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator implements IEvaluator<HonsValue> {
    private final @NotNull HonsMachine machine;
    private final @NotNull EvalContext context;
    private final @NotNull Primitives primitives;
    private final @NotNull ArgSpecCache argSpecCache;
    private final @NotNull ISymbolExpr blackholeSentinel;
    private boolean debug;

    public LazyEvaluator(HonsMachine machine) {
        this.machine = machine;
        this.context = new EvalContext(machine);
        argSpecCache = context.argSpecCache;
        primitives = new Primitives(context.machine);
        blackholeSentinel = IExpr.wrap(machine, context.blackholeTag).asSymbolExpr();
        debug = false;
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }

    public @NotNull EvalContext getContext() {
        return context;
    }

    public @NotNull Primitives getPrimitives() {
        return primitives;
    }

    private IExpr wrap(HonsValue value) {
        return IExpr.wrap(machine, value);
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
                return wrap(machine.cons(function.getValue(), args.getValue()));
            
            /* recursively evaluate */
            var constrArgs = unmakeList(machine, args.getValue());
            eval_multi_inplace(constrArgs);
            
            var starredSymbol = function.makeDataHead();
            return wrap(machine.cons(starredSymbol.getValue(), makeList(machine, constrArgs)));
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
        return SubstituteVisitor.substitute(this, assignments, body);
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
    
    public Optional<IExpr> evaluateIfNeeded(EvaluationQueue evaluationQueue, IExpr expr) {
        /* Ensure the head is evaluated to normal form first */
        if (!expr.isNormalForm()) {
            /* we need to evaluate the head first! */
            assert expr.isCons(); /* !isNormalForm() => isCons() */

            var memoEval = getMemoEvalCheckingForBlackhole(expr.asConsExpr());
            if (memoEval.isEmpty()) {
                if (debug)
                    System.out.printf("%s  not in nf: pushing %s%n", evalIndent, expr.valueToString());
                evaluationQueue.pushNeededEvaluation(expr.asConsExpr());
                return Optional.empty();
            } else {
                return memoEval;
            }
        }
        return Optional.of(expr);
    }
    
    public void evaluateQueue(EvaluationQueue evaluationQueue) throws EvalException {
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
    
    public @NotNull IExpr eval_cons(@NotNull IConsExpr origExpr) throws EvalException {
        try (final EvaluationQueue evaluationQueue = new EvaluationQueue(blackholeSentinel)) {
            var eval = evaluateIfNeeded(evaluationQueue, origExpr);
            if (eval.isPresent())
                return eval.get();
            
            evaluateQueue(evaluationQueue);
            assert evaluationQueue.isEmpty();
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
            return machine.cons(machine.makeSymbol("error"), HonsValue.nil);
        }
    }
    
    @Override
    @Contract("_->param1")
    public @NotNull List<HonsValue> eval_multi_inplace(@NotNull List<HonsValue> vals) {
        try (final EvaluationQueue evaluationQueue = new EvaluationQueue(blackholeSentinel)) {
            /* push all the values onto the evaluation queue */
            for (var val : vals) {
                evaluateIfNeeded(evaluationQueue, wrap(val));
            }

            evaluateQueue(evaluationQueue);
            assert evaluationQueue.isEmpty();
        } catch (EvalException e) {
            e.printStackTrace();
        }

        /* Now update them all in-place */
        vals.replaceAll(val -> {
            var expr = wrap(val);
            if (expr.isNormalForm())
                return val;
            var memoEval = getMemoEvalCheckingForBlackhole(expr.asConsExpr());
            if (memoEval.isEmpty())
                return machine.cons(machine.makeSymbol("error"), HonsValue.nil);
            return memoEval.get().getValue();
        });
        
        return vals;
    }

    public static void demo(@NotNull HonsMachine machine) {
        System.out.println("Evaluator demo");
        
        var evaluator = new LazyEvaluator(machine);
        
        var add = machine.makeSymbol("add");
        var program = makeList(machine,
            add,
            machine.makeSmallInt(5),
            makeList(machine,
                add,
                machine.makeSmallInt(2),
                machine.makeSmallInt(3)
            )
        );
        System.out.println(machine.valueToString(program));

        System.out.println(machine.valueToString(evaluator.eval_one(program)));
    }
}
