package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.eval.expr.IConsExpr;
import uk.bs338.hashLisp.jproto.eval.expr.IExpr;
import uk.bs338.hashLisp.jproto.eval.expr.ISymbolExpr;

import java.util.ArrayDeque;
import java.util.Deque;

public class EvaluationQueue implements AutoCloseable {
    public static class EvaluationFrame {
        private final @NotNull IConsExpr origExpr;
        private @Nullable IExpr applyResult;
        
        private EvaluationFrame(@NotNull IConsExpr origExpr) {
            this.origExpr = origExpr;
            this.applyResult = null;
        }
        
        public @NotNull IConsExpr getOrigExpr() {
            return origExpr;
        }

        public @Nullable IExpr getApplyResult() {
            return applyResult;
        }
        
        public void setApplyResult(@NotNull IExpr expr) {
            this.applyResult = expr;
        }
    }
    
    private final @NotNull Deque<EvaluationFrame> queue;
    private final @NotNull ISymbolExpr blackholeSentinel;

    public EvaluationQueue(@NotNull ISymbolExpr blackholeSentinel) {
        this.queue = new ArrayDeque<>();
        this.blackholeSentinel = blackholeSentinel;
    }
    
    public boolean hasEntries() {
        return !queue.isEmpty();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
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
    
    public void finishEvaluation(EvaluationFrame frame, IExpr result) {
        /* remove from queue */
        var removedFrame = queue.removeLast();
        if (removedFrame != frame)
            throw new AssertionError("finishEvaluation doesn't match current frame");

        /* Check that the sentinel is still in-place */
        checkForBlackholeSentinel(frame.origExpr);
        frame.origExpr.setMemoEval(result);
    }
    
    public void close() {
        clearQueue();
    }
    
    public void clearQueue() {
        for (var frame : queue) {
            checkForBlackholeSentinel(frame.origExpr);
            frame.origExpr.setMemoEval(null);
        }
        
        queue.clear();
    }
}
