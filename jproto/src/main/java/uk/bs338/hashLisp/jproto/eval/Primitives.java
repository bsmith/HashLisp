package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.ValueType;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Collection;
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
        var arg = evaluator.evaluate(machine.fst(args));
        if (arg.getType() != ValueType.CONS_REF)
            return HonsValue.nil;
        else
            return machine.fst(arg);
    }

    public @NotNull HonsValue snd(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var arg = evaluator.evaluate(machine.fst(args));
        if (arg.getType() != ValueType.CONS_REF)
            return HonsValue.nil;
        else
            return machine.snd(arg);
    }

    public @NotNull HonsValue cons(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var fst = evaluator.evaluate(machine.fst(args));
        var snd = evaluator.evaluate(machine.fst(machine.snd(args)));
        return machine.cons(fst, snd);
    }

    public @NotNull HonsValue add(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException {
        int sum = 0;
        var cur = args;
        while (cur.getType() == ValueType.CONS_REF) {
            var fst = evaluator.evaluate(machine.fst(cur));
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
            var fst = evaluator.evaluate(machine.fst(cur));
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
        cond = evaluator.evaluate(cond);
        if (cond.getType() != ValueType.SMALL_INT) {
            return makeList(machine, machine.makeSymbol("error"), machine.makeSymbol("zerop-not-smallint"));
        }
        else if (cond.toSmallInt() == 0) {
            return evaluator.evaluate(t_val);
        }
        else {
            return evaluator.evaluate(f_val);
        }
    }
    
    public @NotNull HonsValue eqp(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var left = machine.fst(args);
        var right = machine.fst(machine.snd(args));
        var t_val = machine.fst(machine.snd(machine.snd(args)));
        var f_val = machine.fst(machine.snd(machine.snd(machine.snd(args))));
        left = evaluator.evaluate(left);
        right = evaluator.evaluate(right);
        if (left.equals(right)) {
            return evaluator.evaluate(t_val);
        }
        else {
            return evaluator.evaluate(f_val);
        }
    }
    
    public @NotNull HonsValue eval(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        return evaluator.evaluate(machine.fst(args));
    }
    
    private class Lambda implements IPrimitive {
        public record ParsedLambda(@NotNull HonsValue argSpec, @NotNull HonsValue body, int uniq) {
            public HonsValue toArgs(HonsMachine machine) {
                return machine.cons(this.argSpec(), machine.cons(this.body(), HonsValue.nil));
            }
        }
        
        public ParsedLambda parseLambdaArgs(@NotNull HonsValue args) {
            return new ParsedLambda(
                machine.fst(args),
                machine.fst(machine.snd(args)),
                args.toObjectHash()
            );
        }

        public ParsedLambda doAlphaConversion(@NotNull LazyEvaluator evaluator, @NotNull ParsedLambda parsedLambda) throws EvalException {
            ArgSpec parsedSpec = evaluator.getContext().argSpecCache.get(parsedLambda.argSpec());
            
            var transformation = parsedSpec.alphaConversion(parsedLambda.uniq());
            if (!transformation.getAssignmentsAsMap().isEmpty()) {
                var transformationVisitor = new SubstituteVisitor(evaluator, transformation);
                var argSpec = transformationVisitor.substitute(IExpr.wrap(machine, parsedLambda.argSpec())).getValue();
                var body = transformationVisitor.substitute(IExpr.wrap(machine, parsedLambda.body())).getValue();
                parsedLambda = new ParsedLambda(argSpec, body, parsedLambda.uniq());
            }
            return parsedLambda;
        }
        
        @Override
        public @NotNull HonsValue apply(@NotNull LazyEvaluator evaluator, @NotNull HonsValue args) {
            var argSpec = machine.fst(args);
            var body = machine.fst(machine.snd(args));
            return machine.cons(evaluator.getContext().lambdaTag.getValue(), machine.cons(argSpec, machine.cons(body, HonsValue.nil)));
        }
//        public @NotNull HonsValue apply(@NotNull LazyEvaluator evaluator, @NotNull HonsValue args) throws EvalException {
//            var parsedLambda = parseLambdaArgs(args);
//            parsedLambda = doAlphaConversion(evaluator, parsedLambda);
//            return machine.cons(evaluator.getContext().lambdaTag.getValue(), parsedLambda.toArgs(machine));
//        }

        @Override
        public @NotNull Optional<HonsValue> substitute(@NotNull LazyEvaluator evaluator, @NotNull Assignments assignments, @NotNull HonsValue value, @NotNull HonsValue args) throws EvalException {
            /* we want to remove from our assignments map any var mentioned in argSpec */
            /* if our assignments map becomes empty, skip recursion */
            /* otherwise, apply the reduced assignments map to the body */
            ParsedLambda parsedLambda = parseLambdaArgs(args);
            
            /* Alpha conversion. */
            HonsValue alphaConversionValue = machine.cons(evaluator.getContext().lambdaExprTag.getValue(), parsedLambda.toArgs(machine));
            Optional<HonsValue> alphaConversionMemo = machine.getMemoEval(alphaConversionValue);
            
            if (alphaConversionMemo.isEmpty()) {
                parsedLambda = doAlphaConversion(evaluator, parsedLambda);
                HonsValue convertedLambda = machine.cons(evaluator.getContext().lambdaTag.getValue(), parsedLambda.toArgs(machine));
                machine.setMemoEval(alphaConversionValue, convertedLambda);
            } else {
                parsedLambda = parseLambdaArgs(machine.snd(alphaConversionMemo.get()));
            }

            HonsValue argSpec = parsedLambda.argSpec();
            HonsValue body = parsedLambda.body();

            ArgSpec parsedSpec = evaluator.getContext().parseArgSpec(argSpec);
            Collection<HonsValue> argNames = parsedSpec.getBoundVariables();

            Assignments newAssignments = assignments.withoutNames(argNames);
            HonsValue newBody = newAssignments.isEmpty() ? body :
                SubstituteVisitor.substitute(evaluator, newAssignments, IExpr.wrap(machine, body)).getValue();
            return Optional.of(makeList(machine, argSpec, newBody));
        }

        @Override
        public @NotNull Set<HonsValue> freeVariables(@NotNull HonsValue args) {
            try {
                var argSpec = ArgSpec.parse(machine, machine.fst(args));
                var body = machine.fst(machine.snd(args));
                
                throw new Error("unimplemented");
            }
            catch (EvalException e) {
                /* XXX too silent */
                return Set.of();
            }
        }
    }
    
    public @NotNull HonsValue error(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException {
        throw new EvalException("error primitive");
    }
}
