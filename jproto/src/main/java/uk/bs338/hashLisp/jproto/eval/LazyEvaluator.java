package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.expr.*;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.*;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator implements IEvaluator<HonsValue> {
    private final @NotNull HonsMachine machine;
    private final @NotNull EvalContext context;
    private final @NotNull Primitives primitives;
    private final @NotNull ArgSpecCache argSpecCache;
    private boolean debug;

    public LazyEvaluator(@NotNull HonsMachine machine) {
        this.machine = machine;
        this.context = new EvalContext(machine);
        argSpecCache = context.argSpecCache;
        primitives = new Primitives(context.machine);
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

    private @NotNull IExpr wrap(@NotNull HonsValue value) {
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
            var constrArgs = ExprUtilities.unmakeList(args);
            evalMultiInplace(constrArgs);
            
            var starredSymbol = function.makeDataHead();
            return IExpr.cons(starredSymbol, ExprUtilities.makeList(IExpr.nil(machine), constrArgs));
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

    public @NotNull IExpr substitute(@NotNull Assignments assignments, @NotNull IExpr body) {
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
    public @NotNull IExpr apply_hnf(@NotNull IExpr function, @NotNull IExpr args) throws EvalException {
        if (debug)
            System.out.printf("%sapply %s to %s%n", evalIndent, function.valueToString(), args.valueToString());
        if (!function.isNormalForm())
            throw new EvalException("apply_hnf called but function not in normal form");
        if (function.getType() == ExprType.SYMBOL) {
            return applyPrimitive(function.asSymbolExpr(), args);
        }
        else if (function.getType() == ExprType.CONS && function.asConsExpr().fst().equals(context.lambdaTag)) {
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
            if (memoEval.get().equals(context.blackholeTag))
                throw new IllegalStateException("Encountered blackhole when evaluating");
        }
        return memoEval;
    }
    
    public @NotNull Optional<IExpr> evaluateIfNeeded(@NotNull EvaluationQueue evaluationQueue, @NotNull IExpr expr) {
        /* Ensure the head is evaluated to normal form first */
        if (!expr.isNormalForm()) {
            /* we need to evaluate the head first! */
            assert expr.getType() == ExprType.CONS; /* !isNormalForm() => isCons() */

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
    
    public void evaluateQueue(@NotNull EvaluationQueue evaluationQueue) throws EvalException {
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
                    frame.setApplyResult(applied);
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
    
    public @NotNull IExpr evalExpr(@NotNull IExpr origExpr) throws EvalException {
        /* Simple values are already in normal form */
        if (origExpr.isNormalForm())
            return origExpr;
        
        try (final EvaluationQueue evaluationQueue = new EvaluationQueue(context.blackholeTag)) {
            var eval = evaluateIfNeeded(evaluationQueue, origExpr);
            if (eval.isPresent())
                return eval.get();
            
            evaluateQueue(evaluationQueue);
            assert evaluationQueue.isEmpty();
        }
        
        assert origExpr.getType() == ExprType.CONS;
        var memoEval = getMemoEvalCheckingForBlackhole(origExpr.asConsExpr());
        if (memoEval.isEmpty())
            throw new AssertionError("Didn't find memoised evaluation result");
        if (!memoEval.get().isNormalForm())
            throw new AssertionError("expression not evaluated to normal form");
        return memoEval.get();
    }
    
    @SuppressWarnings("UnusedReturnValue")
    @Contract("_->param1")
    public @NotNull List<IExpr> evalMultiInplace(@NotNull List<IExpr> exprs) {
        try (final EvaluationQueue evaluationQueue = new EvaluationQueue(context.blackholeTag)) {
            /* push all the values onto the evaluation queue */
            for (var expr : exprs) {
                evaluateIfNeeded(evaluationQueue, expr);
            }

            evaluateQueue(evaluationQueue);
            assert evaluationQueue.isEmpty();
        } catch (EvalException e) {
            e.printStackTrace();
        }

        /* Now update them all in-place */
        exprs.replaceAll(expr -> {
            if (expr.isNormalForm())
                return expr;
            var memoEval = getMemoEvalCheckingForBlackhole(expr.asConsExpr());
            return memoEval.orElseGet(() -> IExpr.cons(IExpr.makeSymbol(machine, "error"), IExpr.nil(machine)));
        });
        
        return exprs;
    }

    @Override
    public @NotNull HonsValue evaluate(@NotNull HonsValue val) {
        var expr = wrap(val);
        try {
            return evalExpr(expr).getValue();
        }
        catch (EvalException e) { /* XXX */
            e.printStackTrace();
                return machine.cons(machine.makeSymbol("error"), HonsValue.nil);
        }
    }
        
    @Override
    public @NotNull HonsValue evaluateWith(@NotNull Map<HonsValue, HonsValue> globals, @NotNull HonsValue val) {
        Assignments assignments = new Assignments(machine, globals);
        IExpr afterSubstitution = SubstituteVisitor.substitute(this, assignments, wrap(val));
        return evaluate(afterSubstitution.getValue());
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

        System.out.println(machine.valueToString(evaluator.evaluate(program)));
    }
}
