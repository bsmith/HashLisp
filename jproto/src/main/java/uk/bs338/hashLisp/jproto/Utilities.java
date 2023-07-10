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
    
    /* XXX Are these two operations the best?  Most javaish? */
    /* XXX using fromInteger does some checks for overflow, but not all? */
    @NotNull
    public static <V extends IValue> V applySmallIntOperation(IValueFactory<V> ivf, IntUnaryOperator func, V val) {
        var rvInt = func.applyAsInt(val.toSmallInt());
        return val.isSmallInt() ? ivf.makeSmallInt(rvInt) : ivf.nil();
    }

    @NotNull
    public static <V extends IValue> V applySmallIntOperation(IValueFactory<V> ivf, IntBinaryOperator func, V left, V right) {
        if (!left.isSmallInt() || !right.isSmallInt()) {
            return ivf.nil();
        }
        var rvInt = func.applyAsInt(left.toSmallInt(), right.toSmallInt());
        return ivf.makeSmallInt(rvInt);
    }
    
    @NotNull
    public static <V extends IValue> V sumList(IHeap<V> heap, V list) {
        if (list.isNil())
            return heap.makeSmallInt(0);
        else if (list.isSmallInt())
            return list;
        else {
            V head = sumList(heap, heap.fst(list));
            V rest = sumList(heap, heap.snd(list));
            return applySmallIntOperation(heap, Integer::sum, head, rest);
        }
    }

    @NotNull
    public static <V extends IValue> V intList(IHeap<V> heap, int[] nums) {
        V list = heap.nil();
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = heap.cons(heap.makeSmallInt(num), list);
        }
        return list;
    }

    @NotNull
    public static <V extends IValue> V stringAsList(IHeap<V> heap, String str) {
        return intList(heap, str.codePoints().toArray());
    }
    
    @NotNull
    public static <V extends IValue> String listAsString(IHeap<V> heap, V list) {
        ArrayList<Integer> codepoints = new ArrayList<>();
        var cur = list;
        while (!cur.isNil()) {
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
    public static <V extends IValue> V makeList(IHeap<V> heap, V... elements) {
        var list = heap.nil();
        for (int index = elements.length - 1; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }

    @NotNull
    @SafeVarargs
    public static <V extends IValue> V makeListWithDot(IHeap<V> heap, V... elements) {
        var list = elements[elements.length - 1];
        for (int index = elements.length - 2; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }
    
    public static <V extends IValue> void unmakeList(IHeap<V> heap, V list, List<V> dst) {
        V cur = list;
        while (cur != null) {
            if (cur.isNil())
                return;
            if (!cur.isConsRef()) {
                dst.add(cur);
                return;
            }
            var uncons = heap.uncons(cur);
            dst.add(uncons.fst());
            cur = uncons.snd();
        }
    }
}
