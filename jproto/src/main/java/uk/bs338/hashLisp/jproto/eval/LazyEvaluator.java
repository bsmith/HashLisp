package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator {
    private final HonsHeap heap;
    private final @NotNull Map<HonsValue, IPrimitive<HonsValue>> primitives;
    private boolean debug;

    public LazyEvaluator(@NotNull HonsHeap heap) {
        this.heap = heap;
        primitives = new HashMap<>();
        debug = false;
        
        primitives.put(heap.makeSymbol("fst"), this::fst);
        primitives.put(heap.makeSymbol( "snd"), this::snd);
        primitives.put(heap.makeSymbol( "cons"), this::cons);
        primitives.put(heap.makeSymbol( "add"), this::add);
        primitives.put(heap.makeSymbol( "lambda"), this::lambda);
        primitives.put(heap.makeSymbol( "eval"), this::eval);
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }
    
    public @NotNull HonsValue fst(HonsValue args) {
        var arg = eval(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.fst(arg);
    }

    public @NotNull HonsValue snd(HonsValue args) {
        var arg = eval(heap.fst(args));
        if (!arg.isConsRef())
            return HonsValue.nil;
        else
            return heap.snd(arg);
    }
    
    public @NotNull HonsValue cons(HonsValue args) {
        var fst = eval(heap.fst(args));
        var snd = eval(heap.fst(heap.snd(args)));
        return heap.cons(fst, snd);
    }
    
    public @NotNull HonsValue add(HonsValue args) {
        int sum = 0;
        var cur = args;
        while (cur.isConsRef()) {
            var fst = eval(heap.fst(cur));
            if (fst.isSmallInt())
                sum += fst.toSmallInt();
            cur = heap.snd(cur);
        }
        if (cur.isSmallInt())
            sum += cur.toSmallInt();
        return heap.makeSmallInt(sum);
    }

    /* XXX this does validation stuff? */
    /* XXX alpha convert early? */
    public @NotNull HonsValue lambda(HonsValue args) {
        System.out.printf("lambda: %s%n", heap.valueToString(args));
        var argSpec = heap.fst(args);
        var body = heap.fst(heap.snd(args));
        return heap.cons(heap.makeSymbol("lambda"), heap.cons(argSpec, heap.cons(body, heap.nil())));
    }

    public boolean isLambda(@NotNull HonsValue value) {
        if (!value.isConsRef())
            return false;
        var head = heap.fst(value);
        return heap.isSymbol(head) && heap.symbolNameAsString(head).equals("lambda");
    }
    
    private class Assignments {
        private @Nullable HonsValue assignmentsAsValue;
        private final Map<HonsValue, HonsValue> assignments;
        
        public Assignments(Map<HonsValue, HonsValue> assignments) {
            this.assignmentsAsValue = null;
            this.assignments = assignments;
        }
        
        public @NotNull HonsValue getAssignmentsAsValue() {
            if (assignmentsAsValue != null)
                return assignmentsAsValue;
            var assignmentsList = HonsValue.nil;
            for (var assignment : assignments.entrySet()) {
                assignmentsList = heap.cons(heap.cons(assignment.getKey(), assignment.getValue()), assignmentsList);
            }
            return assignmentsAsValue = assignmentsList;
        }
        
        public @NotNull String toString() {
            return "Assignments{" + heap.valueToString(getAssignmentsAsValue()) + "}";
        }
        
        private class SubstituteVisitor implements IExprVisitor<HonsValue, HonsValue> {
            @Override
            public @NotNull HonsValue visitConstant(@NotNull HonsValue visited) {
                return visited;
            }

            @Override
            public @NotNull HonsValue visitSymbol(@NotNull HonsValue visited) {
                var assignedValue = assignments.get(visited);
                return assignedValue == null ? visited : assignedValue;
            }

            @Override
            public @NotNull HonsValue visitApply(@NotNull HonsValue visited, @NotNull HonsValue head, @NotNull HonsValue args) {
                return heap.cons(
                    substitute(head),
                    substitute(args)
                );
            }

            @Override
            public @NotNull HonsValue visitLambda(@NotNull HonsValue visited, @NotNull HonsValue argSpec, @NotNull HonsValue body) {
                /* we want to remove from our assignments map any var mentioned in argSpec */
                /* if our assignments map becomes empty, just return visited */
                /* otherwise, apply the reduced assignments map to the body */
                var reducedAssignments = new HashMap<>(assignments);
                var argsList = new ArrayList<HonsValue>();
                unmakeList(heap, argSpec, argsList);
                for (var arg : argsList) {
                    reducedAssignments.remove(arg);
                }
                if (argsList.isEmpty())
                    return visited;
                var newAssignments = new Assignments(reducedAssignments);
                var newBody = newAssignments.substitute(body);
//                throw new RuntimeException("Unimplemented");
                return makeList(heap, heap.makeSymbol("lambda"), argSpec, newBody);
            }
        }
        
        public HonsValue substitute(@NotNull HonsValue body) {
            var visitor = new SubstituteVisitor();
            return visitExpr(body, visitor);
        }
    }
    
    public @NotNull Assignments matchArgSpec(@NotNull HonsValue argSpec, HonsValue args) {
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
            System.out.printf("assignmentsList=%s%n", assignments);
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

    public HonsValue apply(@NotNull HonsValue args) {
        /* cons */
        var uncons = heap.uncons(args);
        var head = eval(uncons.fst());
        var rest = uncons.snd();
        if (heap.isSymbol(head)) {
            var prim = primitives.get(head);
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
    public HonsValue eval(@NotNull HonsValue val) {
        var visitor = new IExprVisitor<HonsValue, HonsValue>() {
            @Override
            public @NotNull HonsValue visitConstant(@NotNull HonsValue visited) {
                return visited;
            }
            
            @Override
            public @NotNull HonsValue visitSymbol(@NotNull HonsValue visited) {
                return visited;
            }
            
            @Override
            public @NotNull HonsValue visitLambda(@NotNull HonsValue visited, @NotNull HonsValue argSpec, @NotNull HonsValue body) { return visited; }

            @Override
            public @NotNull HonsValue visitApply(@NotNull HonsValue visited, @NotNull HonsValue head, @NotNull HonsValue args) {
                var savedIndent = evalIndent;
                var result = (HonsValue)null;

                if (debug) {
                    System.out.printf("%seval: %s%n", evalIndent, heap.valueToString(val));
                    evalIndent += "  ";
                }
                
                var memoEval = heap.getMemoEval(val);
                if (memoEval.isPresent()) {
                    result = memoEval.get();
                } else {
                    result = apply(val);
                    heap.setMemoEval(val, result);
                }

                if (debug) {
                    evalIndent = savedIndent;
                    System.out.printf("%s==> %s%n", evalIndent, heap.valueToString(result));
                }
                
                return result;
            }
        };
        
        return visitExpr(val, visitor);
    }
    
    public <R> R visitExpr(@NotNull HonsValue value, IExprVisitor<HonsValue, R> exprVisitor) {
        var heapVisitor = new ExprToHeapVisitorAdapter<>(heap, exprVisitor);
        heap.visitValue(value, heapVisitor);
        return heapVisitor.result;
    }
    
    public static void demo(@NotNull HonsHeap heap) {
        System.out.println("Evaluator demo");
        
        var evaluator = new LazyEvaluator(heap);
        
        var add = heap.makeSymbol("add");
        var program = makeList(heap,
            add,
            heap.makeSmallInt(5),
            makeList(heap,
                add,
                heap.makeSmallInt(2),
                heap.makeSmallInt(3)
            )
        );
        System.out.println(heap.valueToString(program));

        System.out.println(heap.valueToString(evaluator.eval(program)));
    }
}
