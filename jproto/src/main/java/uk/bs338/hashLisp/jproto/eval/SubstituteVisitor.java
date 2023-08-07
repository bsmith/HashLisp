package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.*;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Optional;

/* since this recurses into itself, I suppose we can just reuse result, instead of creating lots of new visitors */
class SubstituteVisitor implements IExprVisitor, ISubstitutor<HonsValue> {
    public static class TakePut<T> {
        private T value;
        public TakePut() {
            this.value = null;
        }
        public void put(T value) {
            assert this.value == null;
            assert value != null;
            this.value = value;
        }
        public T take() {
            assert this.value != null;
            var rv = this.value;
            this.value = null;
            return rv;
        }
    }
    
    private final @NotNull LazyEvaluator evaluator;
    private final @NotNull HonsMachine machine;
    private final @NotNull Primitives primitives;
    private final @NotNull Assignments assignments;
    private final @NotNull TakePut<IExpr> result;

    public SubstituteVisitor(@NotNull LazyEvaluator evaluator, @NotNull Assignments assignments) {
        this.evaluator = evaluator;
        this.machine = evaluator.getContext().machine;
        this.primitives = evaluator.getPrimitives();
        this.assignments = assignments;
        this.result = new TakePut<>();
    }

    /* for recursive substitution */
    @NotNull
    public IExpr substitute(@NotNull IExpr body) {
        if (assignments.getAssignmentsAsMap().isEmpty())
            return body;
        body.visit(this);
        return result.take();
    }

    @Override
    public @NotNull HonsValue substitute(@NotNull HonsValue body) {
        return substitute(IExpr.wrap(machine, body)).getValue();
    }

    @NotNull
    public IExpr substitute(@NotNull Assignments assignments, @NotNull IExpr body) {
        return substitute(evaluator, assignments, body);
    }

    @Override
    public @NotNull HonsValue substitute(@NotNull Assignments assignments, @NotNull HonsValue body) {
        return substitute(assignments, IExpr.wrap(machine, body)).getValue();
    }

    /* convenience function */
    public static @NotNull IExpr substitute(@NotNull LazyEvaluator evaluator, @NotNull Assignments assignments, @NotNull IExpr body) {
        if (assignments.getAssignmentsAsMap().isEmpty())
            return body;
        return new SubstituteVisitor(evaluator, assignments).substitute(body);
    }

    @Override
    public @NotNull Assignments getAssignments() {
        return assignments;
    }

    @Override
    public void visitSimple(IExpr simpleExpr) {
        result.put(simpleExpr);
    }

    @Override
    public void visitSymbol(ISymbolExpr symbolExpr) {
        var assignedValue = assignments.get(symbolExpr.getValue());
        result.put(assignedValue == null ? symbolExpr : IExpr.wrap(machine, assignedValue));
    }

    @Override
    public void visitCons(IConsExpr consExpr) {
        Optional<IConsExpr> rv = Optional.empty();
            
        if (consExpr.fst().getType() == ExprType.SYMBOL) {
            rv = primitives.get(consExpr.fst().getValue())
                .flatMap(prim -> prim.substitute(evaluator, assignments, consExpr.getValue(), consExpr.snd().getValue()))
                .map(val -> IExpr.cons(consExpr.fst(), IExpr.wrap(machine, val)));
        }
        
        result.put(rv.orElseGet(() -> visitApply(consExpr)));
    }

    public @NotNull IConsExpr visitApply(@NotNull IConsExpr consExpr) {
        return IExpr.cons(
            substitute(consExpr.fst()),
            substitute(consExpr.snd())
        );
    }
}
