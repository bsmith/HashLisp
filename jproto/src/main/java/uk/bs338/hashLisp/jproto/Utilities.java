package uk.bs338.hashLisp.jproto;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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
}
