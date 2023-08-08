package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.*;
import java.util.stream.Collectors;

class Assignments {
    private final @NotNull HonsMachine machine;
    private @Nullable IExpr assignmentsAsValue;
    private final @NotNull Map<HonsValue, HonsValue> assignments;

    public Assignments(@NotNull HonsMachine machine, @NotNull Map<HonsValue, HonsValue> assignments) {
        this.machine = machine;
        this.assignmentsAsValue = null;
        this.assignments = assignments;
    }
    
    public static @NotNull Assignments ofExprs(@NotNull HonsMachine machine, @NotNull Map<ISymbolExpr, IExpr> assignments) {
        Set<Map.Entry<HonsValue, HonsValue>> entrySet = assignments.entrySet().stream()
            .map(entry -> Map.entry(entry.getKey().getValue(), entry.getValue().getValue()))
            .collect(Collectors.toSet());
        var unwrappedAssignments = new AbstractMap<HonsValue, HonsValue>() {
            @NotNull
            @Override
            public Set<Entry<HonsValue, HonsValue>> entrySet() {
                return entrySet;
            }
        };
        return new Assignments(machine, unwrappedAssignments);
    }

    public @NotNull IExpr getAssignmentsAsValue() {
        if (assignmentsAsValue != null)
            return assignmentsAsValue;
        IExpr assignmentsList = IExpr.nil(machine);
        for (var assignment : assignments.entrySet()) {
            IExpr pair = IExpr.wrap(machine, machine.cons(assignment.getKey(), assignment.getValue()));
            assignmentsList = IExpr.cons(pair, assignmentsList);
        }
        return assignmentsAsValue = assignmentsList;
    }
    
    public @NotNull Map<HonsValue, HonsValue> getAssignmentsAsMap() {
        return Map.copyOf(assignments);
    }

    public @NotNull String toString() {
        return "Assignments{" + getAssignmentsAsValue().valueToString() + "}";
    }
    
    public @Nullable HonsValue get(@NotNull HonsValue name) {
        return assignments.get(name);
    }

    public @NotNull Assignments withoutNames(Collection<HonsValue> names) {
        if (names.isEmpty())
            return this;
        var reducedAssignments = new HashMap<>(assignments);
        reducedAssignments.keySet().removeAll(names);
        return new Assignments(machine, reducedAssignments);
    }
    
    public @NotNull Assignments addAssignments(Map<HonsValue, HonsValue> newAssignments) {
        if (newAssignments.isEmpty())
            return this;
        var combined = new HashMap<>(assignments);
        combined.putAll(newAssignments);
        return new Assignments(machine, combined);
    }
    
    public @NotNull Assignments withoutNameExprs(Collection<ISymbolExpr> names) {
        var reducedAssignments = new HashMap<>(assignments);
        for (var name : names)
            reducedAssignments.keySet().remove(name.getValue());
        return new Assignments(machine, reducedAssignments);
    }
}
