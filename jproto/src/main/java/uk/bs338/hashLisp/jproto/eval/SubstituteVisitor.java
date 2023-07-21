package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.eval.expr.*;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Optional;

class SubstituteVisitor implements IExprVisitor, ISubstitutor<HonsValue> {
    private final @NotNull ExprFactory exprFactory;
    private final @NotNull Primitives primitives;
    private final @NotNull Assignments assignments;
    IExpr result;

    public SubstituteVisitor(@NotNull ExprFactory exprFactory, @NotNull Primitives primitives, @NotNull Assignments assignments) {
        this.exprFactory = exprFactory;
        this.primitives = primitives;
        this.assignments = assignments;
    }

    /* for recursive substitution */
    @NotNull
    public IExpr substitute(@NotNull IExpr body) {
        return body.visit(new SubstituteVisitor(exprFactory, primitives, assignments)).result;
    }

    @Override
    public @NotNull HonsValue substitute(@NotNull HonsValue body) {
        return substitute(exprFactory.wrap(body)).getValue();
    }

    @NotNull
    public IExpr substitute(@NotNull Assignments assignments, @NotNull IExpr body) {
        return body.visit(new SubstituteVisitor(exprFactory, primitives, assignments)).result;
    }

    @Override
    public @NotNull HonsValue substitute(@NotNull Assignments assignments, @NotNull HonsValue body) {
        return substitute(assignments, exprFactory.wrap(body)).getValue();
    }

    /* convenience function */
    public static @NotNull IExpr substitute(@NotNull ExprFactory exprFactory, @NotNull Primitives primitives, @NotNull IEvaluator<HonsValue> evaluator, @NotNull Assignments assignments, @NotNull IExpr body) {
        return body.visit(new SubstituteVisitor(exprFactory, primitives, assignments)).result;
    }

    @Override
    public @NotNull Assignments getAssignments() {
        return assignments;
    }

    @Override
    public void visitSimple(ISimpleExpr simpleExpr) {
        result = simpleExpr;
    }

    @Override
    public void visitSymbol(ISymbolExpr symbolExpr) {
        var assignedValue = assignments.get(symbolExpr.getValue());
        result = assignedValue == null ? symbolExpr : exprFactory.wrap(assignedValue);
    }

    @Override
    public void visitCons(IConsExpr consExpr) {
        Optional<IConsExpr> rv = Optional.empty();
            
        if (consExpr.fst().isSymbol()) {
            if (consExpr.fst().asSymbol().isDataHead())
                /* do not substitute under data heads */
                rv = Optional.of(consExpr);
            else
                rv = primitives.get(consExpr.fst().getValue())
                    .flatMap(prim -> prim.substitute(this, consExpr.snd().getValue()))
                    .map(val -> exprFactory.cons(consExpr.fst(), exprFactory.wrap(val)));
        }
        
        result = rv.orElseGet(() -> visitApply(consExpr));
    }

    public @NotNull IConsExpr visitApply(@NotNull IConsExpr consExpr) {
        return exprFactory.cons(
            substitute(consExpr.fst()),
            substitute(consExpr.snd())
        );
    }
}
