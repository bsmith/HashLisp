package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static uk.bs338.hashLisp.jproto.Utilities.makeList;
import static uk.bs338.hashLisp.jproto.Utilities.unmakeList;

class Assignments {
    private final HonsHeap heap;
    private @Nullable HonsValue assignmentsAsValue;
    private final Map<HonsValue, HonsValue> assignments;

    public Assignments(HonsHeap heap, Map<HonsValue, HonsValue> assignments) {
        this.heap = heap;
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

    public @NotNull String toString() {
        return "Assignments{" + heap.valueToString(getAssignmentsAsValue()) + "}";
    }

    private class SubstituteVisitor implements IExprVisitor<HonsValue, HonsValue> {
        @Override
        public @NotNull HonsValue visitConstant(@NotNull HonsValue visited) {
            return visited;
        }

        @Override
        public @NotNull HonsValue visitSymbol(@NotNull HonsValue visited) {
            var assignedValue = assignments.get(visited);
            return assignedValue == null ? visited : assignedValue;
        }

        @Override
        public @NotNull HonsValue visitApply(@NotNull HonsValue visited, @NotNull HonsValue head, @NotNull HonsValue args) {
            return heap.cons(
                substitute(head),
                substitute(args)
            );
        }

        @Override
        public @NotNull HonsValue visitLambda(@NotNull HonsValue visited, @NotNull HonsValue argSpec, @NotNull HonsValue body) {
            /* we want to remove from our assignments map any var mentioned in argSpec */
            /* if our assignments map becomes empty, just return visited */
            /* otherwise, apply the reduced assignments map to the body */
            var reducedAssignments = new HashMap<>(assignments);
            var argsList = new ArrayList<HonsValue>();
            unmakeList(heap, argSpec, argsList);
            for (var arg : argsList) {
                reducedAssignments.remove(arg);
            }
            if (argsList.isEmpty())
                return visited;
            var newAssignments = new Assignments(heap, reducedAssignments);
            var newBody = newAssignments.substitute(body);
            return makeList(heap, heap.makeSymbol("lambda"), argSpec, newBody);
        }
    }

    public HonsValue substitute(@NotNull HonsValue body) {
        var visitor = new SubstituteVisitor();
        return ExprToHeapVisitorAdapter.visitExpr(heap, body, visitor);
    }
}
