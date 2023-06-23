package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

/* Utilities to interop between Java and ILispValue */

@ParametersAreNonnullByDefault
public final class Utilities {
    private Utilities() {
        throw new AssertionError("No Utilities instances for you!");
    }

    public static <V extends IValue> V intList(IHeap<V> heap, int[] nums) {
        V list = heap.nil();
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = heap.cons(heap.makeShortInt(num), list);
        }
        return list;
    }

    public static <V extends IValue> V stringAsList(IHeap<V> heap, String str) {
        return intList(heap, str.codePoints().toArray());
    }
    
    public static <V extends IValue> String listAsString(IHeap<V> heap, V list) {
        try {
            ArrayList<Integer> codepoints = new ArrayList<>();
            var cur = list;
            while (!cur.isNil()) {
                ConsPair<V> pair = heap.uncons(cur);
                int ch = pair.fst().toSmallInt();
                codepoints.add(ch);
                cur = pair.snd();
            }
            return new String(codepoints.stream().mapToInt(ch -> ch).toArray(), 0, codepoints.size());
        } catch (Exception e) {
            return null;
        }
    }

    @SafeVarargs
    public static <V extends IValue> V makeList(IHeap<V> heap, V... elements) {
        var list = heap.nil();
        for (int index = elements.length - 1; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }

    @SafeVarargs
    public static <V extends IValue> V makeListWithDot(IHeap<V> heap, V... elements) {
        var list = elements[elements.length - 1];
        for (int index = elements.length - 2; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }
}
