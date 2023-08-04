package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.eval.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.eval.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public class EvalContext {
    public final @NotNull HonsHeap heap;
    public final @NotNull ExprFactory exprFactory;
    public final @NotNull ArgSpecCache argSpecCache;
    
    public final @NotNull HonsValue blackholeTag;
    public final @NotNull HonsValue lambdaTag;
    
    public EvalContext(@NotNull HonsHeap heap) {
        this.heap = heap;
        exprFactory = new ExprFactory(heap);
        argSpecCache = new ArgSpecCache(heap);
        blackholeTag = heap.makeSymbol(Tag.BLACKHOLE.getSymbolStr());
        lambdaTag = heap.makeSymbol(Tag.LAMBDA.getSymbolStr());
    }
}
