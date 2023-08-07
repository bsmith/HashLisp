package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.ValueType;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.bs338.hashLisp.jproto.Utilities.makeList;

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
        put("quote", this::quote);
        put("eval", this::eval);
        put("lambda", new Lambda());
        put("error", this::error);
    }
    
    public void put(@NotNull String name, @NotNull IPrimitive prim) {
        primitives.put(machine.makeSymbol(name), prim);
    }

    public @NotNull Optional<IPrimitive> get(@NotNull HonsValue name) {
        return Optional.ofNullable(primitives.get(name));
    }
    
    public @NotNull HonsValue fst(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var arg = evaluator.eval_one(machine.fst(args));
        if (arg.getType() != ValueType.CONS_REF)
            return HonsValue.nil;
        else
            return machine.fst(arg);
    }

    public @NotNull HonsValue snd(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var arg = evaluator.eval_one(machine.fst(args));
        if (arg.getType() != ValueType.CONS_REF)
            return HonsValue.nil;
        else
            return machine.snd(arg);
    }

    public @NotNull HonsValue cons(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var fst = evaluator.eval_one(machine.fst(args));
        var snd = evaluator.eval_one(machine.fst(machine.snd(args)));
        return machine.cons(fst, snd);
    }

    public @NotNull HonsValue add(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException {
        int sum = 0;
        var cur = args;
        while (cur.getType() == ValueType.CONS_REF) {
            var fst = evaluator.eval_one(machine.fst(cur));
            if (fst.getType() == ValueType.SMALL_INT)
                sum += fst.toSmallInt();
            else {
                throw new EvalException("arg is not a smallint: args=%s=%s cur=%s=%s fst=%s=%s wtf=%s".formatted(args, machine.valueToString(args), cur, machine.valueToString(cur), fst, machine.valueToString(fst), machine.getCell(fst)));
            }
            cur = machine.snd(cur);
        }
        if (cur.getType() != ValueType.NIL)
            throw new EvalException("args not terminated by nil");
        return machine.makeSmallInt(sum);
    }

    public @NotNull HonsValue mul(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException {
        int product = 1;
        var cur = args;
        while (cur.getType() == ValueType.CONS_REF) {
            var fst = evaluator.eval_one(machine.fst(cur));
            if (fst.getType() == ValueType.SMALL_INT)
                product *= fst.toSmallInt();
            else
                throw new EvalException("arg is not a smallint");
            cur = machine.snd(cur);
        }
        if (cur.getType() != ValueType.NIL)
            throw new EvalException("args not terminated by nil");
        return machine.makeSmallInt(product);
    }

    public @NotNull HonsValue zerop(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var cond = machine.fst(args);
        var t_val = machine.fst(machine.snd(args));
        var f_val = machine.fst(machine.snd(machine.snd(args)));
        cond = evaluator.eval_one(cond);
        if (cond.getType() != ValueType.SMALL_INT) {
            return makeList(machine, machine.makeSymbol("error"), machine.makeSymbol("zerop-not-smallint"));
        }
        else if (cond.toSmallInt() == 0) {
            return evaluator.eval_one(t_val);
        }
        else {
            return evaluator.eval_one(f_val);
        }
    }
    
    public @NotNull HonsValue eqp(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var left = machine.fst(args);
        var right = machine.fst(machine.snd(args));
        var t_val = machine.fst(machine.snd(machine.snd(args)));
        var f_val = machine.fst(machine.snd(machine.snd(machine.snd(args))));
        left = evaluator.eval_one(left);
        right = evaluator.eval_one(right);
        if (left.equals(right)) {
            return evaluator.eval_one(t_val);
        }
        else {
            return evaluator.eval_one(f_val);
        }
    }

    public @NotNull HonsValue quote(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        return machine.fst(args);
    }
    
    public @NotNull HonsValue eval(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        return evaluator.eval_one(machine.fst(args));
    }
    
    private class Lambda implements IPrimitive {
        @Override
        public @NotNull HonsValue apply(@NotNull LazyEvaluator evaluator, @NotNull HonsValue args) throws EvalException {
            var argSpec = new ArgSpec(machine, machine.fst(args));
            var body = machine.fst(machine.snd(args));
            return machine.cons(evaluator.getContext().lambdaTag, machine.cons(argSpec.getOrigArgSpec(), machine.cons(body, HonsValue.nil)));
        }

        @Override
        public @NotNull Optional<HonsValue> substitute(@NotNull LazyEvaluator evaluator, @NotNull Assignments assignments, @NotNull HonsValue value, @NotNull HonsValue args) {
            /* we want to remove from our assignments map any var mentioned in argSpec */
            /* if our assignments map becomes empty, skip recursion */
            /* otherwise, apply the reduced assignments map to the body */
            var argSpec = machine.fst(args);
            var body = machine.fst(machine.snd(args));
            Set<HonsValue> argNames = Set.of();
            Assignments transformation;
            
            /* Alpha conversion.
             * XXX This is currently slow as it doesn't combine any processing if this lambda ends up duplicated
             */
            try {
                var parsedSpec = new ArgSpec(machine, argSpec);
                argNames = parsedSpec.getBoundVariables();

                transformation = parsedSpec.alphaConversion(args.toObjectHash());
                if (!transformation.getAssignmentsAsMap().isEmpty()) {
                    var transformationVisitor = new SubstituteVisitor(evaluator, transformation);
                    argSpec = transformationVisitor.substitute(IExpr.wrap(machine, argSpec)).getValue();
                    /* body is transformed below using addAssignments */
                }
            } catch (EvalException e) {
                /* XXX report error better */
                transformation = new Assignments(machine, Map.of());
            }

            var newAssignments = assignments.withoutNames(argNames).addAssignments(transformation.getAssignmentsAsMap());
            var newBody = newAssignments.getAssignmentsAsMap().size() > 0 ? SubstituteVisitor.substitute(evaluator, newAssignments, IExpr.wrap(machine, body)).getValue() : body;
            return Optional.of(makeList(machine, argSpec, newBody));
        }
    }
    
    public @NotNull HonsValue error(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException {
        throw new EvalException("error primitive");
    }
}
