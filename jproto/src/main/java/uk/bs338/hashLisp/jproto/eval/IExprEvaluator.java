package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.IExpr;

import java.util.List;

public interface IExprEvaluator {
    @NotNull IExpr evalExpr(@NotNull IExpr origExpr) throws EvalException;

    @Contract("_->param1")
    @NotNull List<IExpr> evalMultiInplace(@NotNull List<IExpr> exprs);
}
