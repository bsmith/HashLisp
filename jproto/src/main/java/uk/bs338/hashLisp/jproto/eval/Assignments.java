package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.eval.expr.*;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static uk.bs338.hashLisp.jproto.Utilities.makeList;
import static uk.bs338.hashLisp.jproto.Utilities.unmakeList;

class Assignments {
    private final ExprFactory exprFactory;
    private final HonsHeap heap;
    private @Nullable HonsValue assignmentsAsValue;
    private final Map<HonsValue, HonsValue> assignments;

    public Assignments(ExprFactory exprFactory, Map<HonsValue, HonsValue> assignments) {
        this.exprFactory = exprFactory;
        this.heap = exprFactory.getHeap();
        this.assignmentsAsValue = null;
        this.assignments = assignments;
    }

    public @NotNull HonsValue getAssignmentsAsValue() {
        if (assignmentsAsValue != null)
            return assignmentsAsValue;
        var assignmentsList = HonsValue.nil;
        for (var assignment : assignments.entrySet()) {
            assignmentsList = heap.cons(heap.cons(assignment.getKey(), assignment.getValue()), assignmentsList);
        }
        return assignmentsAsValue = assignmentsList;
    }
    
    public @NotNull Map<HonsValue, HonsValue> getAssignmentsAsMap() {
        return Map.copyOf(assignments);
    }

    public @NotNull String toString() {
        return "Assignments{" + heap.valueToString(getAssignmentsAsValue()) + "}";
    }

    private class SubstituteVisitor implements IExprVisitor2 {
        HonsValue result;

        @Override
        public void visitSimple(ISimpleExpr simpleExpr) {
            result = simpleExpr.getValue();
        }

        @Override
        public void visitSymbol(ISymbolExpr symbolExpr) {
            var assignedValue = assignments.get(symbolExpr.getValue());
            result = assignedValue == null ? symbolExpr.getValue() : assignedValue;
        }

        @Override
        public void visitCons(IConsExpr consExpr) {
            if (consExpr.hasHeadTag(Tag.LAMBDA_SYN)) {
                result = visitLambda(consExpr, consExpr.snd().asConsExpr().fst().getValue(), consExpr.snd().asConsExpr().snd().asConsExpr().fst().getValue());
            } else {
                result = visitApply(consExpr);
            }
        }

        public @NotNull HonsValue visitApply(@NotNull IConsExpr consExpr) {
            return heap.cons(
                substitute(consExpr.fst().getValue()),
                substitute(consExpr.snd().getValue())
            );
        }

        public @NotNull HonsValue visitLambda(@NotNull IConsExpr consExpr, @NotNull HonsValue argSpec, @NotNull HonsValue body) {
            /* we want to remove from our assignments map any var mentioned in argSpec */
            /* if our assignments map becomes empty, just return visited */
            /* otherwise, apply the reduced assignments map to the body */
            var argsList = unmakeList(heap, argSpec);
            if (argsList.isEmpty())
                return consExpr.getValue();
            var newAssignments = withoutNames(argsList);
            var newBody = newAssignments.substitute(body);
            return makeList(heap, heap.makeSymbol("lambda"), argSpec, newBody);
        }
    }

    public HonsValue substitute(@NotNull HonsValue body) {
        return exprFactory.wrap(body).visit(new SubstituteVisitor()).result;
//        return ExprToHeapVisitorAdapter.visitExpr(heap, body, new SubstituteVisitor());
    }
    
    public @Nullable HonsValue get(@NotNull HonsValue name) {
        return assignments.get(name);
    }
    
    public @NotNull Assignments withoutNames(Collection<HonsValue> names) {
        var reducedAssignments = new HashMap<>(assignments);
        reducedAssignments.keySet().removeAll(names);
        return new Assignments(exprFactory, reducedAssignments);
    }
}
