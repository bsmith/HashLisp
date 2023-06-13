package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

@ParametersAreNonnullByDefault
public final class Utilities {
    private Utilities() {
        throw new AssertionError("No Utilities instances for you!");
    }

    public static LispValue intList(IHeap heap, int[] nums) throws Exception {
        LispValue list = LispValue.nil;
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = heap.hons(LispValue.fromShortInt(num), list);
        }
        return list;
    }

    public static LispValue stringAsList(IHeap heap, String str) throws Exception {
        return intList(heap, str.codePoints().toArray());
    }
    
    public static String listAsString(IHeap heap, LispValue list) {
        try {
            ArrayList<Integer> codepoints = new ArrayList<>();
            LispValue cur = list;
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
}
