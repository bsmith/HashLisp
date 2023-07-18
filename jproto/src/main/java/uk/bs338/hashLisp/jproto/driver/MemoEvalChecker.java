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
    
    private boolean isNormalForm(HonsValue val) {
        if (val.isNil() || val.isSmallInt() || val.isSpecial())
            return true;
        else if (heap.isSymbol(val))
            return true;
        
        if (val.isConsRef()) {
            if (heap.isSymbol(heap.fst(val)) && heap.symbolNameAsString(heap.fst(val)).startsWith("*"))
                return true;
        }
        
        return false;
    }
    
    private boolean isHeadNormalForm(HonsValue val) {
        if (!val.isConsRef())
            return true;
        return isNormalForm(heap.fst(val));
    }
    
    @Override
    public void visit(int idx, @NotNull HonsCell cell) {
        var memoEval = cell.getMemoEval();
        if (memoEval == null)
            return;
        
        String reason = null;
        
        try {
            /* valid if you can eval the program and get the same thing! */
            var evaluated = evaluator.eval_one(cell.toValue());
            if (!memoEval.equals(evaluated))
                reason = "eval-diff";
            
            /* XXX: Not sure this is correct, need to consult the specification:
             *      consider (cons '(1 . 2) 3)
             *      this evaluates once to ((1 . 2) . 3)
             *      then the eval fails as (1 . 2) is not applicable/normal-form
             * Current compromise: cons is effectively apply
             */
            /* even more strict! be in head normal form */
            if (!isHeadNormalForm(memoEval))
                reason = "not-hnf";
        }
        catch (Exception e) {
            reason = "exception";
        }
        
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
