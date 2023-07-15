package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.List;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class LazyEvaluator implements IEvaluator<HonsValue> {
    private final @NotNull HonsHeap heap;
    private final @NotNull Primitives primitives;
    private final @NotNull ArgSpecCache argSpecCache;
    private final @NotNull HonsValue lambdaTag;
    private boolean debug;

    public LazyEvaluator(@NotNull HonsHeap heap) {
        this.heap = heap;
        primitives = new Primitives(heap);
        argSpecCache = new ArgSpecCache(heap);
        lambdaTag = heap.makeSymbol("lambda");
        debug = false;
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }

    public boolean isLambda(@NotNull HonsValue value) {
        if (!value.isConsRef())
            return false;
        return heap.fst(value).equals(lambdaTag);
    }

    public @NotNull HonsValue applyLambda(@NotNull HonsValue lambda, @NotNull HonsValue args) throws EvalException {
        HonsValue argSpec = heap.fst(heap.snd(lambda));
        HonsValue body = heap.fst(heap.snd(heap.snd(lambda)));
        
        var assignments = argSpecCache.match(argSpec, args);
        
        var result = assignments.substitute(body);
        return eval_one(result);
    }

    public HonsValue apply(@NotNull HonsValue args) throws EvalException {
        /* cons */
        var uncons = heap.uncons(args);
        var head = eval_one(uncons.fst());
        var rest = uncons.snd();
        if (heap.isSymbol(head)) {
            var prim = primitives.get(head);
            if (prim.isEmpty()) {
                /* if the symbol starts with a *, then treat it a data head
                 * otherwise, treat as a strict constructor.
                 *   This means evaluate the args, and then prepend the *
                 */
                if (heap.fst(heap.symbolName(head)).toSmallInt() == '*')
                    return heap.cons(head, rest);
                List<HonsValue> constrArgs = new ArrayList<>();
                unmakeList(heap, rest, constrArgs);
                constrArgs = eval_multi(constrArgs);
                var starredSymbol = heap.makeSymbol(heap.cons(heap.makeSmallInt('*'), heap.symbolName(head)));
                return heap.cons(starredSymbol, makeList(heap, constrArgs.toArray(new HonsValue[0])));
            }
            try {
                return prim.get().apply(this, rest);
            }
            catch (EvalException e) {
                e.setPrimitive(heap.symbolNameAsString(head));
                e.setCurrentlyEvaluating(heap.valueToString(args));
                throw e;
            }
        }
        else if (isLambda(head)) {
            return applyLambda(head, uncons.snd());
        }
        return heap.cons(head, rest);
    }

    static String evalIndent = "";
    public @NotNull HonsValue eval_one(@NotNull HonsValue val) {
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
                var savedIndent = evalIndent; // XXX add try/finally for this!  maybe an auxiallary function that takes a lambda
                var result = (HonsValue)null;

                if (debug) {
                    System.out.printf("%seval: %s%n", evalIndent, heap.valueToString(val));
                    evalIndent += "  ";
                }
                
                var memoEval = heap.getMemoEval(val);
                if (memoEval.isPresent()) {
                    result = memoEval.get();
                } else {
                    try {
                        result = apply(val);
                        heap.setMemoEval(val, result);
                    }
                    catch (EvalException e) {
                        throw new Error("Exception during apply in eval", e); /* XXX */
                    }
                }

                if (debug) {
                    evalIndent = savedIndent;
                    System.out.printf("%s==> %s%n", evalIndent, heap.valueToString(result));
                }
                
                return result;
            }
        };
        
        return ExprToHeapVisitorAdapter.visitExpr(heap, val, visitor);
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

        System.out.println(heap.valueToString(evaluator.eval_one(program)));
    }
}
