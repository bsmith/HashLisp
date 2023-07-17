package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

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
        lambdaTag = heap.makeSymbol("*lambda");
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

    public @NotNull HonsValue applyPrimitive(@NotNull HonsValue function, @NotNull HonsValue args) throws EvalException {
        var prim = primitives.get(function);
        if (prim.isEmpty()) {
            /* if the symbol starts with a *, then treat it a data head
             * otherwise, treat as a strict constructor.
             *   This means evaluate the args, and then prepend the *
             */
            if (heap.fst(heap.symbolName(function)).toSmallInt() == '*')
                return heap.cons(function, args);
            List<HonsValue> constrArgs = new ArrayList<>();
            unmakeList(heap, args, constrArgs);
            eval_multi_inplace(constrArgs);
            var starredSymbol = heap.makeSymbol(heap.cons(heap.makeSmallInt('*'), heap.symbolName(function)));
            return heap.cons(starredSymbol, makeList(heap, constrArgs));
        }
        try {
            return prim.get().apply(this, args);
        }
        catch (EvalException e) {
            e.setPrimitive(heap.symbolNameAsString(function));
            e.setCurrentlyEvaluating(heap.valueToString(args));
            throw e;
        }
    }

    public @NotNull HonsValue applyLambda(@NotNull HonsValue lambda, @NotNull HonsValue args) throws EvalException {
        HonsValue argSpec = heap.fst(heap.snd(lambda));
        HonsValue body = heap.fst(heap.snd(heap.snd(lambda)));
        
        var assignments = argSpecCache.match(argSpec, args);
        
        var result = assignments.substitute(body);
//        return eval_one(result);
        return result;
    }

    public HonsValue apply(@NotNull HonsValue function, @NotNull HonsValue args) throws EvalException {
        if (debug)
            System.out.printf("%sapply %s to %s%n", evalIndent, heap.valueToString(function), heap.valueToString(args));
        
        if (heap.isSymbol(function)) {
            return applyPrimitive(function, args);
        }
        else if (isLambda(function)) {
            return applyLambda(function, args);
        }
        else {
            var e = new EvalException("Cannot apply something that is not a symbol or lambda");
            e.setCurrentlyEvaluating(heap.valueToString(function));
            throw e;
        }
    }
    
    private @NotNull Optional<HonsValue> evalOnlyIfSimple(@NotNull HonsValue val) {
        if (val.isSpecial() || val.isSmallInt())
            return Optional.of(val);

        /* XXX Not 100% sure about testing for lambda here because we do *data differently */
        if (heap.isSymbol(val)/* || isLambda(val)*/)
            return Optional.of(val);

        /* otherwise, val is a ConsRef representing an application */
        /* but first!  check for a memoised result */
        /* this return empty if it needs more evaluation */
        return heap.getMemoEval(val);
    }
    
    private @NotNull HonsValue eval_application(@NotNull HonsValue val) throws EvalException {
        Deque<HonsValue> argsStack = new ArrayDeque<>();
        HonsValue focus = val;
        
        Optional<HonsValue> function = Optional.empty();
        do {
            var uncons = heap.uncons(focus);
            function = evalOnlyIfSimple(uncons.fst());
            argsStack.addLast(uncons.snd());
            focus = function.orElse(uncons.fst());
            if (debug) {
                if (function.isPresent()) {
                    System.out.printf("%seval_application: head is in normal form%n", evalIndent);
                } else {
                    System.out.printf("%seval_application: head is not in normal form%n", evalIndent);
                    System.out.printf("%s  new focus: %s%n", evalIndent, heap.valueToString(focus));
                }
            }
        } while (function.isEmpty());
        
        while (argsStack.size() > 0) {
            var args = argsStack.removeLast();
            focus = apply(focus, args);
        }
        
        return focus;
    }

    static String evalIndent = "";
    public @NotNull HonsValue eval_one(@NotNull HonsValue val) {
        /* quickly handle simple cases including memoisation */
        {
            var simple = evalOnlyIfSimple(val);
            if (simple.isPresent())
                return simple.get();
        }

        var currentlyEvaluating = val;
        while (true) {
            var simple = evalOnlyIfSimple(currentlyEvaluating);
            if (simple.isPresent()) {
                currentlyEvaluating = simple.get();
                break;
            }
            
            var savedIndent = evalIndent;

            if (debug) {
                System.out.printf("%seval: %s%n", evalIndent, heap.valueToString(currentlyEvaluating));
                evalIndent += "  ";
            }

            try {
                try {
                    var result = eval_application(currentlyEvaluating);
                    currentlyEvaluating = result;
                } catch (EvalException e) {
                    throw new Error("Exception during apply in eval: " + heap.valueToString(currentlyEvaluating), e); /* XXX */
                }
            } finally {
                if (debug)
                    evalIndent = savedIndent;
            }
        }
        
        var result = currentlyEvaluating;
        heap.setMemoEval(val, result);

        if (debug)
            System.out.printf("%s==> %s%n", evalIndent, heap.valueToString(result));
        
        return result;
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
