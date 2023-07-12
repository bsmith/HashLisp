package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

public interface IHeapVisitor<V extends IValue> {
    void visitNil(@NotNull V visited);
    void visitSmallInt(@NotNull V visited, int num);
    void visitSymbol(@NotNull V visited, @NotNull V val);
    void visitCons(@NotNull V visited, @NotNull V fst, @NotNull V snd);
}
