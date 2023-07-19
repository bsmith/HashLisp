package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.eval.expr.*;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    
    public @Nullable HonsValue get(@NotNull HonsValue name) {
        return assignments.get(name);
    }
    
    public @NotNull Assignments withoutNames(Collection<HonsValue> names) {
        var reducedAssignments = new HashMap<>(assignments);
        reducedAssignments.keySet().removeAll(names);
        return new Assignments(exprFactory, reducedAssignments);
    }
}
