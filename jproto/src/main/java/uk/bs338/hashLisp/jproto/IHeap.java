package uk.bs338.hashLisp.jproto;

import javax.annotation.Nonnull;

public interface IHeap<Value extends IValue> extends IValueFactory<Value> {
    @Nonnull
    Value cons(@Nonnull Value fst, @Nonnull Value snd);
    
    @Nonnull
    ConsPair<Value> uncons(@Nonnull Value cons) throws Exception;

    @Nonnull
    default Value fst(Value val) throws Exception {
        return uncons(val).fst();
    }

    @Nonnull
    default Value snd(Value val) throws Exception {
        return uncons(val).snd();
    }

    Value makeSymbol(Value name) throws Exception;

    boolean isSymbol(Value symbol);

    Value symbolName(Value symbol) throws Exception;

    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
