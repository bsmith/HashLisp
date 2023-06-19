package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator {
    private final HonsHeap heap;
    private final Map<HonsValue, IPrimitive<HonsValue>> primitives;
    private boolean debug;

    public LazyEvaluator(HonsHeap heap) throws Exception {
        this.heap = heap;
        primitives = new HashMap<>();
        debug = false;

        primitives.put(stringAsList(heap, "fst"), this::fst);
        primitives.put(stringAsList(heap, "snd"), this::snd);
        primitives.put(stringAsList(heap, "add"), this::add);
        primitives.put(stringAsList(heap, "lambda"), this::lambda);
        primitives.put(stringAsList(heap, "eval"), this::eval);
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }
    
    public HonsValue fst(HonsValue args) throws Exception {
        var arg = heap.fst(args);
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.fst(arg);
    }

    public HonsValue snd(HonsValue args) throws Exception {
        var arg = heap.fst(args);
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.snd(arg);
    }
    
    public HonsValue add(HonsValue args) throws Exception {
        int sum = 0;
        var cur = args;
        while (cur.isConsRef()) {
            var fst = eval(heap.fst(cur));
            if (fst.isShortInt())
                sum += fst.toShortInt();
            cur = heap.snd(cur);
        }
        if (cur.isShortInt())
            sum += cur.toShortInt();
        return heap.makeShortInt(sum);
    }

    public HonsValue lambda(HonsValue args) {
        return HonsValue.nil;
    }

    public boolean isLambda(HonsValue args) {
        return false;
    }

    public HonsValue applyLambda(HonsValue head, HonsValue args) {
        return HonsValue.nil;
    }

    public HonsValue apply(HonsValue args) throws Exception {
        /* cons */
        var pair = heap.uncons(args);
        var head = eval(pair.fst);
        var rest = pair.snd;
        if (heap.isSymbol(head)) {
            var prim = primitives.get(heap.snd(head));
            if (prim != null) {
                return prim.apply(rest);
            }
        }
        else if (isLambda(head)) {
            return applyLambda(head, pair.snd);
        }
        return heap.cons(head, rest);
    }

    static String evalIndent = "";
    public HonsValue eval(HonsValue val) throws Exception {
        HonsValue result = null;
        var savedIndent = evalIndent;
        if (debug) {
            System.out.printf("%seval: %s%n", evalIndent, heap.valueToString(val));
            evalIndent += "  ";
        }
        
        if (val.isNil() || val.isShortInt() || heap.isSymbol(val)) {
            result = val;
        } else if (val.isConsRef()) {
            var memoEval = heap.getMemoEval(val);
            if (memoEval.isPresent()) {
                result = memoEval.get();
            }
            else {
                result = apply(val);
                heap.setMemoEval(val, result);
            }
        }
        
        if (debug) {
            evalIndent = savedIndent;
            System.out.printf("%s==> %s%n", evalIndent, heap.valueToString(result));
        }
        return result;
    }
    
    public static void demo(HonsHeap heap) throws Exception {
        System.out.println("Evaluator demo");
        
        var evaluator = new LazyEvaluator(heap);
        
        var add = heap.makeSymbol("add");
        var program = makeList(heap,
            add,
            heap.makeShortInt(5),
            makeList(heap,
                add,
                heap.makeShortInt(2),
                heap.makeShortInt(3)
            )
        );
        System.out.println(heap.valueToString(program));

        System.out.println(heap.valueToString(evaluator.eval(program)));
    }
}
