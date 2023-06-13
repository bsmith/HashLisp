package uk.bs338.hashLisp.jproto;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public interface IHeap {
    @Nonnull LispValue hons(@Nonnull LispValue fst, @Nonnull LispValue snd) throws Exception;
    @Nonnull LispValue fst(LispValue val) throws Exception;
    @Nonnull LispValue snd(LispValue val) throws Exception;
    
    void dumpHeap(PrintStream stream);
}
