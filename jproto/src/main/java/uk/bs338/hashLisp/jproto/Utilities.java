package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/* Utilities to interop between Java and IValue */

public final class Utilities {
    private Utilities() {
        throw new AssertionError("No Utilities instances for you!");
    }
    
    @NotNull
    public static <V extends IValue> V applySmallIntOperation(@NotNull IValueFactory<V> ivf, @NotNull IntUnaryOperator func, @NotNull V val) {
        if (val.getType() != ValueType.SMALL_INT)
            return ivf.nil();
        var rvInt = func.applyAsInt(val.toSmallInt());
        return ivf.makeSmallInt(rvInt);
    }

    @NotNull
    public static <V extends IValue> V applySmallIntOperation(@NotNull IValueFactory<V> ivf, @NotNull IntBinaryOperator func, @NotNull V left, @NotNull V right) {
        if (left.getType() != ValueType.SMALL_INT || right.getType() != ValueType.SMALL_INT) {
            return ivf.nil();
        }
        var rvInt = func.applyAsInt(left.toSmallInt(), right.toSmallInt());
        return ivf.makeSmallInt(rvInt);
    }
    
    @NotNull
    public static <V extends IValue> V sumList(@NotNull IHeap<V> heap, @NotNull V list) {
        switch (list.getType()) {
            case NIL, SYMBOL_TAG -> {
                return heap.makeSmallInt(0);
            }
            case SMALL_INT -> {
                return list;
            }
            case CONS_REF -> {
                V head = sumList(heap, heap.fst(list));
                V rest = sumList(heap, heap.snd(list));
                return applySmallIntOperation(heap, Integer::sum, head, rest);
            }
            default -> {
                assert false; /* Unreachable if switch is exhaustive */
                return heap.makeSmallInt(0);
            }
        }
    }

    @NotNull
    public static <V extends IValue> V intList(@NotNull IHeap<V> heap, int @NotNull [] nums) {
        V list = heap.nil();
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = heap.cons(heap.makeSmallInt(num), list);
        }
        return list;
    }

    @NotNull
    public static <V extends IValue> V stringAsList(@NotNull IHeap<V> heap, @NotNull String str) {
        return intList(heap, str.codePoints().toArray());
    }
    
    @NotNull
    public static <V extends IValue> String listAsString(@NotNull IHeap<V> heap, V list) {
        ArrayList<Integer> codepoints = new ArrayList<>();
        var cur = list;
        while (cur.getType() != ValueType.NIL) {
            /* XXX record patterns is a Java 19 feature */
//                if (heap.uncons(cur) instanceof ConsPair<V>(var fst, var snd)) {
            ConsPair<V> uncons = heap.uncons(cur);
            int ch = uncons.fst().toSmallInt();
            codepoints.add(ch);
            cur = uncons.snd();
        }
        return new String(codepoints.stream().mapToInt(ch -> ch).toArray(), 0, codepoints.size());
    }
    
    @NotNull
    @SafeVarargs
    public static <V extends IValue> V makeList(@NotNull IHeap<V> heap, V @NotNull ... elements) {
        var list = heap.nil();
        for (int index = elements.length - 1; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }

    @NotNull
    @SafeVarargs
    public static <V extends IValue> V makeListWithDot(@NotNull IHeap<V> heap, V @NotNull ... elements) {
        var list = elements[elements.length - 1];
        for (int index = elements.length - 2; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }

    @NotNull
    public static <V extends IValue> V makeList(@NotNull IHeap<V> heap, @NotNull List<V> elements) {
        var list = heap.nil();
        for (int index = elements.size() - 1; index >= 0; index--) {
            list = heap.cons(elements.get(index), list);
        }
        return list;
    }

    @NotNull
    public static <V extends IValue> V makeListWithDot(@NotNull IHeap<V> heap, @NotNull List<V> elements) {
        var list = elements.get(elements.size() - 1);
        for (int index = elements.size() - 2; index >= 0; index--) {
            list = heap.cons(elements.get(index), list);
        }
        return list;
    }
    
    public static <V extends IValue> List<V> unmakeList(@NotNull IHeap<V> heap, @NotNull V list) {
        var dst = new ArrayList<V>();
        V cur = list;
        while (cur != null) {
            if (cur.getType() == ValueType.NIL)
                return dst;
            if (cur.getType() != ValueType.CONS_REF) {
                dst.add(cur);
                return dst;
            }
            var uncons = heap.uncons(cur);
            dst.add(uncons.fst());
            cur = uncons.snd();
        }
        return dst;
    }
}
