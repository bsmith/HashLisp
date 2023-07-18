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
    private final List<String> brokenCellReasons;
    private final boolean verbose;

    public MemoEvalChecker(HonsHeap heap, IEvaluator<HonsValue> evaluator, boolean verbose) {
        this.heap = heap;
        this.evaluator = evaluator;
        this.brokenCells = new ArrayList<>();
        this.brokenCellReasons = new ArrayList<>();
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
        
        String reason = null;
        
        /* valid if you can eval the program and get the same thing! */
        var evaluated = evaluator.eval_one(cell.toValue());
        if (!memoEval.equals(evaluated))
            reason = "eval-diff";
        
        /* valid if it's in normal form s.t. eval as identity on it */
        var evaluatedAgain = evaluator.eval_one(evaluated);
        if (!memoEval.equals(evaluatedAgain))
            reason = "not-normal";
        
        /* even more strict! be a specific kind of value */
        if (memoEval.isNil() || memoEval.isSmallInt() || memoEval.isSpecial())
            /* no op */;
        else if (memoEval.isConsRef() && heap.isSymbol(heap.fst(memoEval)) && heap.symbolNameAsString(heap.fst(memoEval)).startsWith("*"))
            /* no op */;
        else
            reason = "data-type";
        
        if (reason != null) {
            brokenCells.add(cell);
            brokenCellReasons.add(reason);
        }
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

        for (int idx = 0; idx < brokenCells.size(); idx++) {
            var cell = brokenCells.get(idx);
            var reason = brokenCellReasons.get(idx);
            System.err.printf("%s: %s%n  %s%n", reason, cell, heap.valueToString(cell.toValue()));
            System.err.printf("  memoEval: %s%n", heap.valueToString(cell.getMemoEval()));
        }

//        heap.dumpHeap(System.err);

        throw new HeapValidationError();
    }
}
