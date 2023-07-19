package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.eval.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.eval.expr.IConsExpr;
import uk.bs338.hashLisp.jproto.eval.expr.IExpr;
import uk.bs338.hashLisp.jproto.eval.expr.ISymbolExpr;

import java.util.ArrayDeque;
import java.util.Deque;

public class EvaluationQueue {
    public static class EvaluationFrame {
        public final @NotNull IConsExpr origExpr;
        public @Nullable IConsExpr appliedExpr;
        
        private EvaluationFrame(@NotNull IConsExpr origExpr) {
            this.origExpr = origExpr;
            this.appliedExpr = null;
        }
    }
    
    private final @NotNull Deque<EvaluationFrame> queue;
    private final @NotNull ISymbolExpr blackholeSentinel;

    public EvaluationQueue(ExprFactory exprFactory) {
        this.queue = new ArrayDeque<>();
        blackholeSentinel = exprFactory.makeSymbol(Tag.BLACKHOLE);
    }
    
    public boolean hasEntries() {
        return !queue.isEmpty();
    }
    
    public int size() {
        return queue.size();
    }
    
    public void pushNeededEvaluation(IConsExpr expr) {
        queue.addLast(new EvaluationFrame(expr));

        /* Set the sentinel: it is cleared in finishEvaluation, or abortQueue */
        assert expr.getMemoEval().isEmpty();
        expr.setMemoEval(blackholeSentinel);
    }
    
    public EvaluationFrame getCurrentFrame() {
        return queue.getLast();
    }
    
    private void checkForBlackholeSentinel(IConsExpr expr) {
        var prevMemo = expr.getMemoEval();
        if (prevMemo.isEmpty() || !prevMemo.get().equals(blackholeSentinel))
            throw new AssertionError("Didn't find blackhole sentinel when expected");
    }
    
    public void finishEvaluation(IConsExpr origExpr, IExpr result) {
        /* remove from queue */
        var frame = queue.removeLast();
        if (frame.origExpr != origExpr)
            throw new AssertionError("finishEvaluation doesn't match current frame");

        /* Check that the sentinel is still in-place */
        checkForBlackholeSentinel(origExpr);
        origExpr.setMemoEval(result);
    }
    
    public void clearQueue() {
        for (var frame : queue) {
            checkForBlackholeSentinel(frame.origExpr);
            frame.origExpr.setMemoEval(null);
        }
        
        queue.clear();
    }
}
