package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/* Utilities to interop between Java and IValue */

@ParametersAreNonnullByDefault
public final class Utilities {
    private Utilities() {
        throw new AssertionError("No Utilities instances for you!");
    }

    public static <V extends IValue> V intList(IHeap<V> heap, int[] nums) throws Exception {
        V list = heap.nil();
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = heap.cons(heap.makeShortInt(num), list);
        }
        return list;
    }

    public static <V extends IValue> V stringAsList(IHeap<V> heap, String str) throws Exception {
        return intList(heap, str.codePoints().toArray());
    }
    
    public static <V extends IValue> String listAsString(IHeap<V> heap, V list) {
        try {
            ArrayList<Integer> codepoints = new ArrayList<>();
            var cur = list;
            while (!cur.isNil()) {
                Pair<V> pair = heap.uncons(cur);
                int ch = pair.fst.toShortInt();
                codepoints.add(ch);
                cur = pair.snd;
            }
            return new String(codepoints.stream().mapToInt(ch -> ch).toArray(), 0, codepoints.size());
        } catch (Exception e) {
            return null;
        }
    }

    @SafeVarargs
    public static <V extends IValue> V makeList(IHeap<V> heap, V... elements) throws Exception {
        var list = heap.nil();
        for (int index = elements.length - 1; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }

    @SafeVarargs
    public static <V extends IValue> V makeListWithDot(IHeap<V> heap, V... elements) throws Exception {
        var list = elements[elements.length - 1];
        for (int index = elements.length - 2; index >= 0; index--) {
            list = heap.cons(elements[index], list);
        }
        return list;
    }
    
    public static <V extends IValue> void unmakeList(IHeap<V> heap, V list, List<V> dst) throws Exception {
        V cur = list;
        while (cur != null) {
            if (cur.isNil())
                return;
            if (!cur.isConsRef()) {
                dst.add(cur);
                return;
            }
            var pair = heap.uncons(cur);
            dst.add(pair.fst);
            cur = pair.snd;
        }
    }
}
