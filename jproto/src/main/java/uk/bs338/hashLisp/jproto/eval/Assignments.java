package uk.bs338.hashLisp.jproto.eval;

import com.beust.ah.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.expr.IExprFactory;
import uk.bs338.hashLisp.jproto.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.*;
import java.util.stream.Collectors;

class Assignments {
    private final IExprFactory exprFactory;
    private @Nullable IExpr assignmentsAsValue;
    private final Map<HonsValue, HonsValue> assignments;

    public Assignments(@NotNull IExprFactory exprFactory, @NotNull Map<HonsValue, HonsValue> assignments) {
        this.exprFactory = exprFactory;
        this.assignmentsAsValue = null;
        this.assignments = assignments;
    }
    
    public static @NotNull Assignments ofExprs(@NotNull IExprFactory exprFactory, @NotNull Map<ISymbolExpr, IExpr> assignments) {
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
        return new Assignments(exprFactory, unwrappedAssignments);
    }

    public @NotNull IExpr getAssignmentsAsValue() {
        if (assignmentsAsValue != null)
            return assignmentsAsValue;
        IExpr assignmentsList = exprFactory.nil();
        for (var assignment : assignments.entrySet()) {
            assignmentsList = exprFactory.cons(exprFactory.cons(exprFactory.wrap(assignment.getKey()), exprFactory.wrap(assignment.getValue())), assignmentsList);
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
        var reducedAssignments = new HashMap<>(assignments);
        reducedAssignments.keySet().removeAll(names);
        return new Assignments(exprFactory, reducedAssignments);
    }
    
    public @NotNull Assignments withoutNameExprs(Collection<ISymbolExpr> names) {
        var reducedAssignments = new HashMap<>(assignments);
        for (var name : names)
            reducedAssignments.keySet().remove(name.getValue());
        return new Assignments(exprFactory, reducedAssignments);
    }
}
