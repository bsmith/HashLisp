package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.ExprType;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.bs338.hashLisp.jproto.expr.ExprUtilities.makeList;

public class Primitives {
    private final @NotNull HonsMachine machine;
    private final @NotNull Map<HonsValue, IPrimitive> primitives;

    public Primitives(@NotNull HonsMachine machine) {
        this.machine = machine;
        this.primitives = new HashMap<>();

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
        primitives.put(machine.makeSymbol(name), prim);
    }

    public @NotNull Optional<IPrimitive> get(@NotNull HonsValue name) {
        return Optional.ofNullable(primitives.get(name));
    }
    
    public @NotNull IExpr fst(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var arg = evaluator.evalExpr(args.asConsExpr().fst());
        if (arg.getType() != ExprType.CONS)
            return IExpr.nil(machine);
        else
            return arg.asConsExpr().fst();
    }

    public @NotNull IExpr snd(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var arg = evaluator.evalExpr(args.asConsExpr().fst());
        if (arg.getType() != ExprType.CONS)
            return IExpr.nil(machine);
        else
            return arg.asConsExpr().snd();
    }

    public @NotNull IExpr cons(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var fst = evaluator.evalExpr(args.asConsExpr().fst());
        var snd = evaluator.evalExpr(args.asConsExpr().snd().asConsExpr().fst());
        return IExpr.cons(fst, snd);
    }

    public @NotNull IExpr add(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        int sum = 0;
        var cur = args;
        while (cur.getType() == ExprType.CONS) {
            var fst = evaluator.evalExpr(cur.asConsExpr().fst());
            if (fst.getType() == ExprType.SMALL_INT)
                sum += fst.getValue().toSmallInt();
            else {
                throw new EvalException("arg is not a smallint: args=%s=%s cur=%s=%s fst=%s=%s wtf=%s".formatted(args, args.valueToString(), cur, cur.valueToString(), fst, fst.valueToString(), machine.getHeap().getCell(fst.getValue())));
            }
            cur = cur.asConsExpr().snd();
        }
        if (cur.getType() != ExprType.NIL)
            throw new EvalException("args not terminated by nil");
        return IExpr.ofSmallInt(machine, sum);
    }

    public @NotNull IExpr mul(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        int product = 1;
        var cur = args;
        while (cur.getType() == ExprType.CONS) {
            var fst = evaluator.evalExpr(cur.asConsExpr().fst());
            if (fst.getType() == ExprType.SMALL_INT)
                product *= fst.getValue().toSmallInt();
            else
                throw new EvalException("arg is not a smallint");
            cur = cur.asConsExpr().snd();
        }
        if (cur.getType() != ExprType.NIL)
            throw new EvalException("args not terminated by nil");
        return IExpr.ofSmallInt(machine, product);
    }

    public @NotNull IExpr zerop(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var cond = args.asConsExpr().fst();
        var t_val = args.asConsExpr().snd().asConsExpr().fst();
        var f_val = args.asConsExpr().snd().asConsExpr().snd().asConsExpr().fst();
        cond = evaluator.evalExpr(cond);
        if (cond.getType() != ExprType.SMALL_INT) {
            return makeList(IExpr.nil(machine), List.of(IExpr.makeSymbol(machine, "error"), IExpr.makeSymbol(machine, "zerop-not-smallint")));
        }
        else if (cond.getValue().toSmallInt() == 0) {
            return evaluator.evalExpr(t_val);
        }
        else {
            return evaluator.evalExpr(f_val);
        }
    }
    
    public @NotNull IExpr eqp(@NotNull IExprEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        var left = args.asConsExpr().fst();
        var right = args.asConsExpr().snd().asConsExpr().fst();
        var t_val = args.asConsExpr().snd().asConsExpr().snd().asConsExpr().fst();
        var f_val = args.asConsExpr().snd().asConsExpr().snd().asConsExpr().snd().asConsExpr().fst();
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
        public @NotNull IExpr apply(@NotNull LazyEvaluator evaluator, @NotNull IExpr args) throws EvalException {
            ArgSpec argSpec = new ArgSpec(machine, args.asConsExpr().fst());
            IExpr body = args.asConsExpr().snd().asConsExpr().fst();
            return IExpr.cons(evaluator.getContext().lambdaTag, IExpr.cons(argSpec.getOrigArgSpec(), IExpr.cons(body, IExpr.nil(machine))));
        }

        @Override
        public @NotNull Optional<IExpr> substitute(@NotNull LazyEvaluator evaluator, @NotNull Assignments assignments, @NotNull IExpr value, @NotNull IExpr args) {
            /* we want to remove from our assignments map any var mentioned in argSpec */
            /* if our assignments map becomes empty, skip recursion */
            /* otherwise, apply the reduced assignments map to the body */
            IExpr argSpec = args.asConsExpr().fst();
            IExpr body = args.asConsExpr().snd().asConsExpr().fst();
            Set<ISymbolExpr> argNames = Set.of();
            Assignments transformation;
            
            /* Alpha conversion.
             * XXX This is currently slow as it doesn't combine any processing if this lambda ends up duplicated
             */
            try {
                var parsedSpec = new ArgSpec(machine, argSpec);
                argNames = parsedSpec.getBoundVariables();

                transformation = parsedSpec.alphaConversion(args.getValue().toObjectHash());
                if (!transformation.getAssignmentsAsMap().isEmpty()) {
                    var transformationVisitor = new SubstituteVisitor(evaluator, transformation);
                    argSpec = transformationVisitor.substitute(argSpec);
                    /* body is transformed below using addAssignments */
                }
            } catch (EvalException e) {
                /* XXX report error better */
                transformation = new Assignments(machine, Map.of());
            }

            Assignments newAssignments = assignments.withoutNameExprs(argNames).addAssignments(transformation.getAssignmentsAsMap());
            IExpr newBody = newAssignments.getAssignmentsAsMap().size() > 0 ? SubstituteVisitor.substitute(evaluator, newAssignments, body) : body;
            return Optional.of(makeList(IExpr.nil(machine), List.of(argSpec, newBody)));
        }
    }
    
    public @NotNull IExpr error(@NotNull LazyEvaluator evaluator, @NotNull IExpr args) throws EvalException {
        throw new EvalException("error primitive");
    }
}
