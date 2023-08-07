package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

public interface IValue {
    @NotNull ValueType getType();
    
    /* throw NoSuchElementException if not a small int */
    int toSmallInt();
}
