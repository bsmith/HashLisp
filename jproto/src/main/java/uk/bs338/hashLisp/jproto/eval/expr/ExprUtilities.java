package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ExprUtilities {
    private ExprUtilities() {
        throw new AssertionError("No ExprUtilities instances for you!");
    }

    public static @NotNull IExpr makeList(@NotNull ExprFactory factory, @NotNull List<IExpr> elements) {
        IExpr list = factory.nil();
        for (int index = elements.size() - 1; index >= 0; index--) {
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
