package uk.bs338.hashLisp.jproto;

import uk.bs338.hashLisp.jproto.hons.HonsValue;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public interface IHeap {
    @Nonnull
    HonsValue hons(@Nonnull HonsValue fst, @Nonnull HonsValue snd);
    @Nonnull
    HonsValue fst(HonsValue val) throws Exception;
    @Nonnull
    HonsValue snd(HonsValue val) throws Exception;
    
    void dumpHeap(PrintStream stream);
}
