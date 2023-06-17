package uk.bs338.hashLisp.jproto;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

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
                int ch = heap.fst(cur).toShortInt();
                codepoints.add(ch);
                cur = heap.snd(cur);
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
}
