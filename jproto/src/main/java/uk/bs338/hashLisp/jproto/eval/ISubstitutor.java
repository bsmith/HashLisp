package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;

public interface ISubstitutor<T> {
    /* for recursive substitution */
    @NotNull T substitute(@NotNull T body);

    @NotNull T substitute(@NotNull Assignments assignments, @NotNull T body);
    
    @NotNull Assignments getAssignments();
}
