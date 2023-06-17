package uk.bs338.hashLisp.jproto;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

@ParametersAreNonnullByDefault
public final class Utilities {
    private Utilities() {
        throw new AssertionError("No Utilities instances for you!");
    }

    public static HonsValue intList(IHeap heap, int[] nums) throws Exception {
        HonsValue list = HonsValue.nil;
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = heap.hons(HonsValue.fromShortInt(num), list);
        }
        return list;
    }

    public static HonsValue stringAsList(IHeap heap, String str) throws Exception {
        return intList(heap, str.codePoints().toArray());
    }
    
    public static String listAsString(IHeap heap, HonsValue list) {
        try {
            ArrayList<Integer> codepoints = new ArrayList<>();
            HonsValue cur = list;
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
    public static HonsValue makeList(IHeap heap, HonsValue... elements) throws Exception {
        var list = HonsValue.nil;
        for (int index = elements.length - 1; index >= 0; index--) {
            list = heap.hons(elements[index], list);
        }
        return list;
    }

    @SafeVarargs
    public static HonsValue makeListWithDot(IHeap heap, HonsValue... elements) throws Exception {
        var list = elements[elements.length - 1];
        for (int index = elements.length - 2; index >= 0; index--) {
            list = heap.hons(elements[index], list);
        }
        return list;
    }
}
