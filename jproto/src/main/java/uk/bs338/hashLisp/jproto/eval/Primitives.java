package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;

import static uk.bs338.hashLisp.jproto.Utilities.makeList;

public class Primitives implements IPrimitives<HonsValue, HonsValue> {
    private final @NotNull HonsHeap heap;
    private final @NotNull Map<HonsValue, IPrimitive<HonsValue>> primitives;

    public Primitives(@NotNull HonsHeap heap) {
        this.heap = heap;
        this.primitives = new HashMap<>();

        put("fst", this::fst);
        put("snd", this::snd);
        put("cons", this::cons);
        put("add", this::add);
        put("mul", this::mul);
        put("zerop", this::zerop);
        put("quote", this::quote);
        put("eval", this::eval);
    }
    
    public void put(@NotNull String name, @NotNull IPrimitive<HonsValue> prim) {
        primitives.put(heap.makeSymbol(name), prim);
    }

    @Override
    public @NotNull IPrimitive<HonsValue> get(@NotNull HonsValue name) {
        return primitives.get(name);
    }
    
    public @NotNull HonsValue fst(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var arg = evaluator.eval_one(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.fst(arg);
    }

    public @NotNull HonsValue snd(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var arg = evaluator.eval_one(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.snd(arg);
    }

    public @NotNull HonsValue cons(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var fst = evaluator.eval_one(heap.fst(args));
        var snd = evaluator.eval_one(heap.fst(heap.snd(args)));
        return heap.cons(fst, snd);
    }

    public @NotNull HonsValue add(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException {
        int sum = 0;
        var cur = args;
        while (cur.isConsRef()) {
            var fst = evaluator.eval_one(heap.fst(cur));
            if (fst.isSmallInt())
                sum += fst.toSmallInt();
            else {
                throw new EvalException("arg is not a smallint: args=%s=%s cur=%s=%s fst=%s=%s wtf=%s".formatted(args, heap.valueToString(args), cur, heap.valueToString(cur), fst, heap.valueToString(fst), heap.getCell(fst)));
            }
            cur = heap.snd(cur);
        }
        if (!cur.isNil())
            throw new EvalException("args not terminated by nil");
        return heap.makeSmallInt(sum);
    }

    public @NotNull HonsValue mul(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) throws EvalException {
        int product = 1;
        var cur = args;
        while (cur.isConsRef()) {
            var fst = evaluator.eval_one(heap.fst(cur));
            if (fst.isSmallInt())
                product *= fst.toSmallInt();
            else
                throw new EvalException("arg is not a smallint");
            cur = heap.snd(cur);
        }
        if (!cur.isNil())
            throw new EvalException("args not terminated by nil");
        return heap.makeSmallInt(product);
    }

    public @NotNull HonsValue zerop(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        var cond = heap.fst(args);
        var t_val = heap.fst(heap.snd(args));
        var f_val = heap.fst(heap.snd(heap.snd(args)));
        cond = evaluator.eval_one(cond);
        if (!cond.isSmallInt()) {
            return makeList(heap, heap.makeSymbol("error"), heap.makeSymbol("zerop-not-smallint"));
        }
        else if (cond.toSmallInt() == 0) {
            return evaluator.eval_one(t_val);
        }
        else {
            return evaluator.eval_one(f_val);
        }
    }

    public @NotNull HonsValue quote(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        return heap.fst(args);
    }
    
    public @NotNull HonsValue eval(@NotNull IEvaluator<HonsValue> evaluator, @NotNull HonsValue args) {
        return evaluator.eval_one(heap.fst(args));
    }
}
