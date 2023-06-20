package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        primitives.put(stringAsList(heap, "cons"), this::cons);
        primitives.put(stringAsList(heap, "add"), this::add);
        primitives.put(stringAsList(heap, "lambda"), this::lambda);
        primitives.put(stringAsList(heap, "lambdaAssignments"), this::lambdaAssignments);
        primitives.put(stringAsList(heap, "eval"), this::eval);
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }
    
    public HonsValue fst(HonsValue args) throws Exception {
        var arg = eval(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.fst(arg);
    }

    public HonsValue snd(HonsValue args) throws Exception {
        var arg = eval(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.snd(arg);
    }
    
    public HonsValue cons(HonsValue args) throws Exception {
        var fst = eval(heap.fst(args));
        var snd = eval(heap.fst(heap.snd(args)));
        return heap.cons(fst, snd);
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

    public HonsValue lambda(HonsValue args) throws Exception {
        System.out.printf("lambda: %s%n", heap.valueToString(args));
        var argSpec = heap.fst(args);
        var body = heap.fst(heap.snd(args));
        return heap.cons(heap.makeSymbol("lambda"), heap.cons(argSpec, heap.cons(body, heap.nil())));
    }

    public boolean isLambda(HonsValue value) throws Exception {
        if (!value.isConsRef())
            return false;
        var head = heap.fst(value);
        return heap.isSymbol(head) && heap.symbolNameAsString(head).equals("lambda");
    }
    
    public HonsValue getLambdaArgSpec(HonsValue lambda) throws Exception {
//        ArrayList<HonsValue> argSpec = new ArrayList<>();
//        unmakeList(heap, heap.fst(heap.snd(lambda)), argSpec);
//        return argSpec;
        return heap.fst(heap.snd(lambda));
    }

    public HonsValue getLambdaBody(HonsValue lambda) throws Exception {
        return heap.fst(heap.snd(heap.snd(lambda)));
    }

    public HonsValue applyLambda(HonsValue lambda, HonsValue args) throws Exception {
        HonsValue argSpec = getLambdaArgSpec(lambda);
        HonsValue body = getLambdaBody(lambda);
//        System.out.printf("argSpec=%s%nbody=%s%n", argSpec.stream().map(heap::valueToString).toList(), heap.valueToString(body));
        System.out.printf("args=%s%nargSpec=%s%nbody=%s%n", heap.valueToString(args), heap.valueToString(argSpec), heap.valueToString(body));
        
        if (heap.isSymbol(argSpec)) {
            return makeList(heap, heap.makeSymbol("error"), heap.makeSymbol("slurpy argSpec not implemented"));
        }
        else if (argSpec.isConsRef()) {
            var assignments = new HashMap<HonsValue, HonsValue>();
            var curSpec = argSpec;
            var curArg = args;
            while (!curSpec.isNil()) {
                assignments.put(heap.fst(curSpec), heap.fst(curArg));
                curSpec = heap.snd(curSpec);
                curArg = heap.snd(curArg);
            }
            var assignmentsList = HonsValue.nil;
            for (var assignment : assignments.entrySet()) {
                assignmentsList = heap.cons(heap.cons(assignment.getKey(), assignment.getValue()), assignmentsList);
            }
            System.out.printf("assignmentsList=%s%n", assignmentsList);
            return lambdaAssignments(assignmentsList, body); //makeList(heap, heap.makeSymbol("lambdaAssignments"), assignmentsList, body);
        }

        return makeList(heap, heap.makeSymbol("error"), heap.makeSymbol("failed to apply lambda"));
    }
    
    public HonsValue lambdaAssignments(HonsValue args) throws Exception {
        return lambdaAssignments(heap.fst(args), heap.fst(heap.snd(args)));
    }
    
    public HonsValue lambdaAssignments(HonsValue assignmentsList, HonsValue body) throws Exception {
        if (heap.isSymbol(body)) {
            return makeList(heap, heap.makeSymbol("error"), heap.makeSymbol("unimpl: lambdaAssignments 1"));
        }
        else if (body.isConsRef()) {
            Function<HonsValue, HonsValue> wrapInLambdaAssignments = val -> {
                try {
                    return makeList(heap, heap.makeSymbol("lambdaAssignments"), assignmentsList, val);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
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

        if (val.isNil() || val.isShortInt() || heap.isSymbol(val)) {
            return val;
        }
        
        if (debug) {
            System.out.printf("%seval: %s%n", evalIndent, heap.valueToString(val));
            evalIndent += "  ";
        }
        
        if (val.isConsRef()) {
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
