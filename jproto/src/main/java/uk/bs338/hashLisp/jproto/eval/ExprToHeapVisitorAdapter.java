package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.IHeapVisitor;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

/* XXX how does this deal with recursion? ie when is result valid? */
public class ExprToHeapVisitorAdapter<R> implements IHeapVisitor<HonsValue> {
    private final HonsHeap heap;
    private final IExprVisitor<HonsValue, R> exprVisitor;
    public @Nullable R result;

    public ExprToHeapVisitorAdapter(HonsHeap heap, IExprVisitor<HonsValue, R> exprVisitor) {
        this.heap = heap;
        this.exprVisitor = exprVisitor;
        result = null;
    }
    
    public static <R> R visitExpr(@NotNull HonsHeap heap, HonsValue value, IExprVisitor<HonsValue, R> exprVisitor) {
        var heapVisitor = new ExprToHeapVisitorAdapter<>(heap, exprVisitor);
        heap.visitValue(value, heapVisitor);
        return heapVisitor.result;
    }

    @Override
    public void visitNil(@NotNull HonsValue visited) {
        result = exprVisitor.visitConstant(visited);
    }

    @Override
    public void visitSmallInt(@NotNull HonsValue visited, int num) {
        result = exprVisitor.visitConstant(visited);
    }

    @Override
    public void visitSymbol(@NotNull HonsValue visited, @NotNull HonsValue val) {
        result = exprVisitor.visitSymbol(visited);
    }

    @Override
    public void visitCons(@NotNull HonsValue visited, @NotNull HonsValue fst, @NotNull HonsValue snd) {
        if (heap.isSymbol(fst)) {
            String symbolName = heap.symbolNameAsString(fst);
            if (symbolName.equals("lambda")) {
                result = exprVisitor.visitLambda(visited, heap.fst(snd), heap.fst(heap.snd(snd)));
                return;
            }
        }
        /* should be an application */
        result = exprVisitor.visitApply(visited, fst, snd);
    }
}
