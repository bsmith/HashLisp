package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;

public interface IIterateHeapVisitor {
    void visit(int idx, @NotNull HonsCell cell);
    
    /* allow this to be a functional interface */
    default void finished() { };
}
