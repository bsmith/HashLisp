package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.hons.*;

import java.util.ArrayList;
import java.util.List;

public class MemoEvalChecker implements IIterateHeapVisitor {
    /* does this extend Context, or HasContext, or has a Context? */
    private final HonsHeap heap;
    private final IEvaluator<HonsValue> evaluator;
    private final List<HonsCell> brokenCells;
    private final boolean verbose;

    public MemoEvalChecker(HonsHeap heap, IEvaluator<HonsValue> evaluator, boolean verbose) {
        this.heap = heap;
        this.evaluator = evaluator;
        this.brokenCells = new ArrayList<>();
        this.verbose = verbose;
    }
    
    public static void checkHeap(HonsHeap heap, IEvaluator<HonsValue> evaluator, boolean verbose) {
        heap.iterateHeap(new MemoEvalChecker(heap, evaluator, verbose));
    }
    
    public static void checkHeap(HonsHeap heap, IEvaluator<HonsValue> evaluator) {
        heap.iterateHeap(new MemoEvalChecker(heap, evaluator, false));
    }
    
    @Override
    public void visit(int idx, @NotNull HonsCell cell) {
        var memoEval = cell.getMemoEval();
        if (memoEval == null)
            return;
        var evaluated = evaluator.eval_one(cell.toValue());
        if (!memoEval.equals(evaluated))
            brokenCells.add(cell);
    }

    @Override
    public void finished() {
        if (brokenCells.size() <= 0) {
            if (verbose)
                System.err.println("Memo eval checker completed successfully");
            return;
        }

        System.err.printf("*** MEMO EVAL CHECKER FOUND BROKEN CELS ***%n");
        System.err.printf("  Found %d broken cells%n%n", brokenCells.size());

        for (var cell : brokenCells) {
            System.err.printf("0x???: %s%n  %s%n", cell, heap.valueToString(cell.toValue()));
        }

//        heap.dumpHeap(System.err);

        throw new HeapValidationError();
    }
}
