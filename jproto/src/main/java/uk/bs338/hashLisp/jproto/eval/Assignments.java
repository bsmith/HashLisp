package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class Assignments {
    private final @NotNull HonsMachine machine;
    private @Nullable HonsValue assignmentsAsValue;
    private final @NotNull Map<HonsValue, HonsValue> assignments;

    public Assignments(@NotNull HonsMachine machine, @NotNull Map<HonsValue, HonsValue> assignments) {
        this.machine = machine;
        this.assignmentsAsValue = null;
        this.assignments = assignments;
    }

    public @NotNull HonsValue getAssignmentsAsValue() {
        if (assignmentsAsValue != null)
            return assignmentsAsValue;
        var assignmentsList = HonsValue.nil;
        for (var assignment : assignments.entrySet()) {
            assignmentsList = machine.cons(machine.cons(assignment.getKey(), assignment.getValue()), assignmentsList);
        }
        return assignmentsAsValue = assignmentsList;
    }
    
    public @NotNull Map<HonsValue, HonsValue> getAssignmentsAsMap() {
        return Map.copyOf(assignments);
    }

    public @NotNull String toString() {
        return "Assignments{" + machine.valueToString(getAssignmentsAsValue()) + "}";
    }
    
    public @Nullable HonsValue get(@NotNull HonsValue name) {
        return assignments.get(name);
    }
    
    public @NotNull Assignments withoutNames(@NotNull Collection<HonsValue> names) {
        if (names.isEmpty())
            return this;
        var reducedAssignments = new HashMap<>(assignments);
        reducedAssignments.keySet().removeAll(names);
        return new Assignments(machine, reducedAssignments);
    }
    
    public @NotNull Assignments addAssignments(@NotNull Map<HonsValue, HonsValue> newAssignments) {
        if (newAssignments.isEmpty())
            return this;
        var combined = new HashMap<>(assignments);
        combined.putAll(newAssignments);
        return new Assignments(machine, combined);
    }
}
