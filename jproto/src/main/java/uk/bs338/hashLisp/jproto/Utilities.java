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
    
    public static <V extends IValue> int sumList(@NotNull IHeap<V> heap, @NotNull V list) {
        return switch (list.getType()) {
            case NIL, SYMBOL_TAG -> 0;
            case SMALL_INT -> list.toSmallInt();
            case CONS_REF -> {
                int head = sumList(heap, heap.fst(list));
                int rest = sumList(heap, heap.snd(list));
                yield head + rest;
            }
        };
    }

    public static <V extends IValue> @NotNull V intList(@NotNull IMachine<V> m, int @NotNull [] nums) {
        V list = m.nil();
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = m.cons(m.makeSmallInt(num), list);
        }
        return list;
    }

    public static <V extends IValue> @NotNull V stringAsList(@NotNull IMachine<V> m, @NotNull String str) {
        return intList(m, str.codePoints().toArray());
    }
    
    @NotNull
    public static <V extends IValue> String listAsString(@NotNull IHeap<V> heap, V list) {
        ArrayList<Integer> codepoints = new ArrayList<>();
        var cur = list;
        while (cur.getType() == ValueType.CONS_REF) {
            /* XXX record patterns is a Java 19 feature */
//                if (machine.uncons(cur) instanceof ConsPair<V>(var fst, var snd)) {
            ConsPair<? extends V> uncons = heap.uncons(cur);
            int ch = uncons.fst().toSmallInt();
            codepoints.add(ch);
            cur = uncons.snd();
        }
        return new String(codepoints.stream().mapToInt(ch -> ch).toArray(), 0, codepoints.size());
    }
    
    @NotNull
    @SafeVarargs
    public static <V extends IValue> V makeList(@NotNull IMachine<V> m, V @NotNull ... elements) {
        var list = m.nil();
        for (int index = elements.length - 1; index >= 0; index--) {
            list = m.cons(elements[index], list);
        }
        return list;
    }

    @NotNull
    @SafeVarargs
    public static <V extends IValue> V makeListWithDot(@NotNull IMachine<V> m, V @NotNull ... elements) {
        var list = elements[elements.length - 1];
        for (int index = elements.length - 2; index >= 0; index--) {
            list = m.cons(elements[index], list);
        }
        return list;
    }

    @NotNull
    public static <V extends IValue> V makeList(@NotNull IMachine<V> m, @NotNull List<V> elements) {
        var list = m.nil();
        for (int index = elements.size() - 1; index >= 0; index--) {
            list = m.cons(elements.get(index), list);
        }
        return list;
    }

    @NotNull
    public static <V extends IValue> V makeListWithDot(@NotNull IMachine<V> m, @NotNull List<V> elements) {
        var list = elements.get(elements.size() - 1);
        for (int index = elements.size() - 2; index >= 0; index--) {
            list = m.cons(elements.get(index), list);
        }
        return list;
    }
    
    public static <V extends IValue> @NotNull List<V> unmakeList(@NotNull IMachine<V> m, @NotNull V list) {
        var dst = new ArrayList<V>();
        V cur = list;
        while (cur != null) {
            if (cur.getType() == ValueType.NIL)
                return dst;
            if (cur.getType() != ValueType.CONS_REF) {
                dst.add(cur);
                return dst;
            }
            var uncons = m.uncons(cur);
            dst.add(uncons.fst());
            cur = uncons.snd();
        }
        return dst;
    }
}
