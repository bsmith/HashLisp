package uk.bs338.hashLisp.jproto;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public interface IHeap {
    public @Nonnull LispValue hons(@Nonnull LispValue fst, @Nonnull LispValue snd) throws Exception;
    public @Nonnull LispValue fst(LispValue val) throws Exception;
    public @Nonnull LispValue snd(LispValue val) throws Exception;
    
    void dumpHeap(PrintStream stream);
}
