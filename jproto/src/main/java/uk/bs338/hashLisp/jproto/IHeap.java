package uk.bs338.hashLisp.jproto;

import javax.annotation.Nonnull;

public interface IHeap<Value extends IValue> extends IValueFactory<Value> {
    @Nonnull
    Value cons(@Nonnull Value fst, @Nonnull Value snd);
    
    @Nonnull
    Pair<Value> uncons(@Nonnull Value cons) throws Exception;

    @Nonnull
    default Value fst(Value val) throws Exception {
        return uncons(val).fst;
    }

    @Nonnull
    default Value snd(Value val) throws Exception {
        return uncons(val).snd;
    }

    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
