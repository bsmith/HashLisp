package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.IHeapVisitor;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator {
    private final HonsHeap heap;
    private final Map<HonsValue, IPrimitive<HonsValue>> primitives;
    private boolean debug;

    public LazyEvaluator(HonsHeap heap) {
        this.heap = heap;
        primitives = new HashMap<>();
        debug = false;
        
        primitives.put(stringAsList(heap, "fst"), this::fst);
        primitives.put(stringAsList(heap, "snd"), this::snd);
        primitives.put(stringAsList(heap, "cons"), this::cons);
        primitives.put(stringAsList(heap, "add"), this::add);
        primitives.put(stringAsList(heap, "lambda"), this::lambda);
        primitives.put(stringAsList(heap, "eval"), this::eval);
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }
    
    public HonsValue fst(HonsValue args) {
        var arg = eval(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.fst(arg);
    }

    public HonsValue snd(HonsValue args) {
        var arg = eval(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.snd(arg);
    }
    
    public HonsValue cons(HonsValue args) {
        var fst = eval(heap.fst(args));
        var snd = eval(heap.fst(heap.snd(args)));
        return heap.cons(fst, snd);
    }
    
    public HonsValue add(HonsValue args) {
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

    /* XXX this does validation stuff? */
    public HonsValue lambda(HonsValue args) {
        System.out.printf("lambda: %s%n", heap.valueToString(args));
        var argSpec = heap.fst(args);
        var body = heap.fst(heap.snd(args));
        return heap.cons(heap.makeSymbol("lambda"), heap.cons(argSpec, heap.cons(body, heap.nil())));
    }

    public boolean isLambda(HonsValue value) {
        if (!value.isConsRef())
            return false;
        var head = heap.fst(value);
        return heap.isSymbol(head) && heap.symbolNameAsString(head).equals("lambda");
    }
    
//    HonsValue assignmentsCacheValue = null;
//    Map<HonsValue, HonsValue> assignmentsCacheMap = null;
    
    private class Assignments implements IHeapVisitor<HonsValue> {
        private HonsValue assignmentsAsValue;
        private final Map<HonsValue, HonsValue> assignments;
        private HonsValue visitResult;
        
        public Assignments(Map<HonsValue, HonsValue> assignments) {
            this.assignmentsAsValue = null;
            this.assignments = assignments;
            this.visitResult = null;
        }
        
        public HonsValue getAssignmentsAsValue() {
            if (assignmentsAsValue != null)
                return assignmentsAsValue;
            var assignmentsList = HonsValue.nil;
            for (var assignment : assignments.entrySet()) {
                assignmentsList = heap.cons(heap.cons(assignment.getKey(), assignment.getValue()), assignmentsList);
            }
            return assignmentsAsValue = assignmentsList;
        }
        
        public HonsValue substitute(HonsValue body) {
            /* XXX Java can be bad sometimes */
            assert this.visitResult == null;
            heap.visitValue(body, this);
            assert this.visitResult != null;
            var result = this.visitResult;
            this.visitResult = null;
            return result;
        }

        @Override
        public void visitNil(HonsValue visited) {
            this.visitResult = visited;
        }

        @Override
        public void visitShortInt(HonsValue visited, int num) {
            this.visitResult = visited;
        }

        @Override
        public void visitSymbol(HonsValue visited, HonsValue val) {
            var assignedValue = assignments.get(visited);
            this.visitResult = assignedValue == null ? visited : assignedValue;
        }

        @Override
        public void visitCons(HonsValue visited, HonsValue fst, HonsValue snd) {
            this.visitResult = heap.cons(
                makeList(heap, heap.makeSymbol("let"), getAssignmentsAsValue(), fst),
                makeList(heap, heap.makeSymbol("let"), getAssignmentsAsValue(), snd)
                );
        }
    }
    
    public Assignments matchArgSpec(HonsValue argSpec, HonsValue args) {
        if (heap.isSymbol(argSpec)) {
//            return makeList(heap, heap.makeSymbol("error"), heap.makeSymbol("slurpy argSpec not implemented"));
            throw new RuntimeException("Not implemented");
        }
        else if (argSpec.isConsRef()) {
            var assignmentsMap = new HashMap<HonsValue, HonsValue>();
            var curSpec = argSpec;
            var curArg = args;
            while (!curSpec.isNil()) {
                assignmentsMap.put(heap.fst(curSpec), heap.fst(curArg));
                curSpec = heap.snd(curSpec);
                curArg = heap.snd(curArg);
            }
            var assignments = new Assignments(assignmentsMap);
            System.out.printf("assignmentsList=%s%n", assignments.getAssignmentsAsValue());
            return assignments;
        }
        else
            throw new RuntimeException("Not implemented");
    }

    public HonsValue applyLambda(HonsValue lambda, HonsValue args) {
        HonsValue argSpec = heap.fst(heap.snd(lambda));
        HonsValue body = heap.fst(heap.snd(heap.snd(lambda)));
        System.out.printf("args=%s%nargSpec=%s%nbody=%s%n", heap.valueToString(args), heap.valueToString(argSpec), heap.valueToString(body));
        
        var assignments = matchArgSpec(argSpec, args);
        
        var result = assignments.substitute(body);
        System.out.printf("result=%s%n", heap.valueToString(result));
        return eval(result);

//        return makeList(heap, heap.makeSymbol("error"), heap.makeSymbol("failed to apply lambda"));
    }

    public HonsValue apply(HonsValue args) {
        /* cons */
        var uncons = heap.uncons(args);
        var head = eval(uncons.fst());
        var rest = uncons.snd();
        if (heap.isSymbol(head)) {
            var prim = primitives.get(heap.snd(head));
            if (prim != null) {
                return prim.apply(rest);
            }
        }
        else if (isLambda(head)) {
            return applyLambda(head, uncons.snd());
        }
        return heap.cons(head, rest);
    }

    static String evalIndent = "";
    public HonsValue eval(HonsValue val) {
        var visitor = new IHeapVisitor<HonsValue>() {
            public HonsValue result = null;

            @Override
            public void visitNil(HonsValue visited) {
                result = visited;
            }

            @Override
            public void visitShortInt(HonsValue visited, int num) {
                result = visited;
            }
            
            @Override
            public void visitSymbol(HonsValue visited, HonsValue val) {
                result = visited;
            }

            @Override
            public void visitCons(HonsValue visited, HonsValue fst, HonsValue snd) {
                var memoEval = heap.getMemoEval(val);
                if (memoEval.isPresent()) {
                    result = memoEval.get();
                } else {
                    try {
                        result = apply(val);
                        heap.setMemoEval(val, result);
                    } catch (Exception e) {
                        /* XXX */
                    }
                }
            }
        };

        var savedIndent = evalIndent;

        if (debug) {
            System.out.printf("%seval: %s%n", evalIndent, heap.valueToString(val));
            evalIndent += "  ";
        }
        
        heap.visitValue(val, visitor);
        
        if (debug) {
            evalIndent = savedIndent;
            System.out.printf("%s==> %s%n", evalIndent, heap.valueToString(visitor.result));
        }
        return visitor.result;
    }
    
    public static void demo(HonsHeap heap) {
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
