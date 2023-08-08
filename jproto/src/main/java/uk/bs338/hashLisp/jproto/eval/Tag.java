package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;

/* Tags are used to tag objects known to the evaluator.  Tags should be different from primitives */
public enum Tag {
    BLACKHOLE("***BLACKHOLE"),
    LAMBDA("*lambda"),
    DATA("*data");
    
    private final @NotNull String symbolStr;
    
    Tag(@NotNull String symbolStr) {
        this.symbolStr = symbolStr;
    }
    
    public @NotNull String getSymbolStr() {
        return symbolStr;
    }
}
