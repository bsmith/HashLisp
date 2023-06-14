package uk.bs338.hashLisp.jproto;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public interface IHeap {
    @Nonnull
    HonsValue cons(@Nonnull HonsValue fst, @Nonnull HonsValue snd) throws Exception;
    
    @Nonnull
    Pair<HonsValue> uncons(@Nonnull HonsValue cons) throws Exception;

    @Nonnull
    default HonsValue fst(HonsValue val) throws Exception {
        return uncons(val).fst;
    }

    @Nonnull
    default HonsValue snd(HonsValue val) throws Exception {
        return uncons(val).snd;
    }

    // --Commented out by Inspection (13/06/2023, 19:33):void dumpHeap(PrintStream stream);
}
