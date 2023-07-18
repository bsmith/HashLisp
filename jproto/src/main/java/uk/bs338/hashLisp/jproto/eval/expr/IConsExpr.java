package uk.bs338.hashLisp.jproto.eval.expr;

import org.jetbrains.annotations.NotNull;

public interface IConsExpr extends IExpr {
    @NotNull IExpr fst();
    @NotNull IExpr snd();
}
