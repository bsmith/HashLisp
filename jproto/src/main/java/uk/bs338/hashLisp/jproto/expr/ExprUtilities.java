package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ExprUtilities {
    private ExprUtilities() {
        throw new AssertionError("No ExprUtilities instances for you!");
    }
    
    public static @NotNull IExpr intList(@NotNull IExprFactory factory, int @NotNull [] nums) {
        IExpr list = IExpr.nil;
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = factory.cons(IExpr.ofSmallInt(num), list);
        }
        return list;
    }

    public static @NotNull IExpr makeList(@NotNull IExprFactory factory, @NotNull List<IExpr> elements) {
        IExpr list = IExpr.nil;
        for (int index = elements.size() - 1; index >= 0; index--) {
            list = factory.cons(elements.get(index), list);
        }
        return list;
    }
    
    public static @NotNull IExpr makeListWithDot(@NotNull IExprFactory factory, @NotNull List<IExpr> elements) {
        var list = elements.get(elements.size() - 1);
        for (int index = elements.size() - 2; index >= 0; index--) {
            list = factory.cons(elements.get(index), list);
        }
        return list;
    }

    public static @NotNull List<IExpr> unmakeList(@NotNull IExpr list) {
        var dst = new ArrayList<IExpr>();
        IExpr cur = list;
        while (!cur.isNil()) {
            if (!cur.isCons()) {
                dst.add(cur);
                break;
            } else {
                var cons = cur.asCons();
                dst.add(cons.fst());
                cur = cons.snd();
            }
        }
        return dst;
    }
}
