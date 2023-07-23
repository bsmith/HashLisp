package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.bs338.hashLisp.jproto.expr.ExprUtilities.makeList;
import static uk.bs338.hashLisp.jproto.Utilities.unmakeList;

public class Primitives {
    private final @NotNull ExprFactory exprFactory;
    private final @NotNull IArgSpecFactory argSpecFactory;
    private final @NotNull Map<HonsValue, IPrimitive> primitives;
    private final @NotNull IExpr lambdaTag;

    public Primitives(@NotNull ExprFactory exprFactory, @NotNull IArgSpecFactory argSpecFactory) {
        this.exprFactory = exprFactory;
        this.argSpecFactory = argSpecFactory;
        this.primitives = new HashMap<>();
        lambdaTag = exprFactory.makeSymbol(Tag.LAMBDA.getSymbolStr());

        put("fst", this::fst);
        put("snd", this::snd);
        put("cons", this::cons);
        put("add", this::add);
        put("mul", this::mul);
        put("zerop", this::zerop);
        put("eq?", this::eqp);
        put("lambda", new Lambda());
        put("error", this::error);
    }
    
    public void put(@NotNull String name, @NotNull IPrimitive prim) {
        primitives.put(exprFactory.makeSymbol(name).getValue(), prim);
    }

    public @NotNull Optional<IPrimitive> get(@NotNull HonsValue name) {
        return Optional.ofNullable(primitives.get(name));
    }
    
    public @NotNull IExpr fst(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var arg = evaluator.evalExpr(args.asCons().fst());
        if (!arg.isCons())
            return exprFactory.nil();
        else
            return arg.asCons().fst();
    }

    public @NotNull IExpr snd(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var arg = evaluator.evalExpr(args.asCons().fst());
        if (!arg.isCons())
            return exprFactory.nil();
        else
            return arg.asCons().snd();
    }

    public @NotNull IExpr cons(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var fst = evaluator.evalExpr(args.asCons().fst());
        var snd = evaluator.evalExpr(args.asCons().snd().asCons().fst());
        return exprFactory.cons(fst, snd);
    }

    public @NotNull IExpr add(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        int sum = 0;
        var cur = args;
        while (cur.isCons()) {
            var fst = evaluator.evalExpr(cur.asCons().fst());
            if (fst.isSmallInt())
                sum += fst.getValue().toSmallInt();
            else {
                throw new EvalException("arg is not a smallint: args=%s=%s cur=%s=%s fst=%s=%s wtf=%s".formatted(args, args.valueToString(), cur, cur.valueToString(), fst, fst.valueToString(), exprFactory.getHeap().getCell(fst.getValue())));
            }
            cur = cur.asCons().snd();
        }
        if (!cur.isNil())
            throw new EvalException("args not terminated by nil");
        return exprFactory.makeSmallInt(sum);
    }

    public @NotNull IExpr mul(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        int product = 1;
        var cur = args;
        while (cur.isCons()) {
            var fst = evaluator.evalExpr(cur.asCons().fst());
            if (fst.isSmallInt())
                product *= fst.getValue().toSmallInt();
            else
                throw new EvalException("arg is not a smallint");
            cur = cur.asCons().snd();
        }
        if (!cur.isNil())
            throw new EvalException("args not terminated by nil");
        return exprFactory.makeSmallInt(product);
    }

    public @NotNull IExpr zerop(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var cond = args.asCons().fst();
        var t_val = args.asCons().snd().asCons().fst();
        var f_val = args.asCons().snd().asCons().snd().asCons().fst();
        cond = evaluator.evalExpr(cond);
        if (!cond.isSmallInt()) {
            return makeList(exprFactory, List.of(exprFactory.makeSymbol("error"), exprFactory.makeSymbol("zerop-not-smallint")));
        }
        else if (cond.getValue().toSmallInt() == 0) {
            return evaluator.evalExpr(t_val);
        }
        else {
            return evaluator.evalExpr(f_val);
        }
    }
    
    public @NotNull IExpr eqp(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var left = args.asCons().fst();
        var right = args.asCons().snd().asCons().fst();
        var t_val = args.asCons().snd().asCons().snd().asCons().fst();
        var f_val = args.asCons().snd().asCons().snd().asCons().snd().asCons().fst();
        left = evaluator.evalExpr(left);
        right = evaluator.evalExpr(right);
        if (left.equals(right)) {
            return evaluator.evalExpr(t_val);
        }
        else {
            return evaluator.evalExpr(f_val);
        }
    }
    
    private class Lambda implements IPrimitive {
        @Override
        public @NotNull IExpr apply(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) {
            var argSpec = args.asCons().fst();
            var body = args.asCons().snd().asCons().fst();
            return exprFactory.cons(lambdaTag, exprFactory.cons(argSpec, exprFactory.cons(body, exprFactory.nil())));
        }

        @Override
        public @NotNull Optional<IExpr> substitute(@NotNull ISubstitutor substitutor, @NotNull IExpr args) throws EvalException {
            /* we want to remove from our assignments map any var mentioned in argSpec */
            /* if our assignments map becomes empty, skip recursion */
            /* otherwise, apply the reduced assignments map to the body */
            var argSpec = argSpecFactory.get(args.asCons().fst());
            var body = args.asCons().snd().asCons().fst();

            var argsList = argSpec.getBoundNames();
            var newAssignments = substitutor.getAssignments().withoutNameExprs(argsList);
            var newBody = newAssignments.getAssignmentsAsMap().size() > 0 ? substitutor.substitute(newAssignments, body) : body;
            return Optional.of(makeList(exprFactory, List.of(args.asCons().fst(), newBody)));
        }
    }
    
    public @NotNull IExpr error(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException{
        throw new EvalException("error primitive");
    }
}
