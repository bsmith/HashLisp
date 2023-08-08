package uk.bs338.hashLisp.jproto.expr;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ExprUtilities {
    private ExprUtilities() {
        throw new AssertionError("No ExprUtilities instances for you!");
    }

    public static @NotNull IExpr makeList(@NotNull IExpr nil, @NotNull List<IExpr> elements) {
        IExpr list = nil;
        for (int index = elements.size() - 1; index >= 0; index--) {
            list = IExpr.cons(elements.get(index), list);
        }
        return list;
    }

    public static @NotNull List<IExpr> unmakeList(@NotNull IExpr list) {
        var dst = new ArrayList<IExpr>();
        IExpr cur = list;
        while (cur.getType() != ExprType.NIL) {
            if (cur.getType() != ExprType.CONS) {
                dst.add(cur);
                break;
            } else {
                var cons = cur.asConsExpr();
                dst.add(cons.fst());
                cur = cons.snd();
            }
        }
        return dst;
    }
}
