package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

public interface IHeapVisitor<V extends IValue> {
    void visitNil(@NotNull V visited);
    void visitSmallInt(@NotNull V visited, int num);
    void visitSymbol(@NotNull V visited, @NotNull V val);
    void visitCons(@NotNull V visited, @NotNull V fst, @NotNull V snd);
    
    default void visitValue(@NotNull IMachine<V> machine, @NotNull V val) {
        if (val.isNil())
            this.visitNil(val);
        else if (val.isSmallInt())
            this.visitSmallInt(val, val.toSmallInt());
        else if (machine.isSymbol(val))
            this.visitSymbol(val, machine.symbolName(val));
        else if (val.isConsRef()) {
            var uncons = machine.uncons(val);
            this.visitCons(val, uncons.fst(), uncons.snd());
        }
        else {
            throw new IllegalArgumentException("couldn't identify value: " + val);
        }
    }
}
