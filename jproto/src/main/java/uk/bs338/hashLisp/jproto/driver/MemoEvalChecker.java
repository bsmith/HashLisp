package uk.bs338.hashLisp.jproto.driver;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.ValueType;
import uk.bs338.hashLisp.jproto.hons.*;

import java.util.ArrayList;
import java.util.List;

public class MemoEvalChecker implements IIterateHeapVisitor {
    /* does this extend Context, or HasContext, or has a Context? */
    private final HonsMachine machine;
    private final IEvaluator<HonsValue> evaluator;
    private final List<HonsCell> brokenCells;
    private final List<String> brokenCellReasons;
    private final boolean verbose;

    public MemoEvalChecker(HonsMachine machine, IEvaluator<HonsValue> evaluator, boolean verbose) {
        this.machine = machine;
        this.evaluator = evaluator;
        this.brokenCells = new ArrayList<>();
        this.brokenCellReasons = new ArrayList<>();
        this.verbose = verbose;
    }
    
    public static void checkHeap(HonsMachine machine, IEvaluator<HonsValue> evaluator, boolean verbose) {
        machine.iterateHeap(new MemoEvalChecker(machine, evaluator, verbose));
    }
    
    public static void checkHeap(HonsMachine machine, IEvaluator<HonsValue> evaluator) {
        checkHeap(machine, evaluator, false);
    }
    
    private boolean isNormalForm(HonsValue val) {
        switch (val.getType()) {
            case NIL, SYMBOL_TAG, SMALL_INT -> {
                return true;
            }
        }
        assert val.getType() == ValueType.CONS_REF;
        
        if (machine.isSymbol(val))
            return true;

        HonsValue fst = machine.fst(val);
        return machine.isSymbol(fst) && machine.symbolNameAsString(fst).startsWith("*");
    }
    
    @Override
    public void visit(int idx, @NotNull HonsCell cell) {
        var memoEval = cell.getMemoEval();
        if (memoEval == null)
            return;
        
        String reason = null;
        
        try {
            /* valid if you can eval the program and get the same thing! */
            var evaluated = evaluator.evaluate(cell.toValue());
            if (!memoEval.equals(evaluated))
                reason = "eval-diff";
            
            /* even more strict! be in head normal form */
            if (!isNormalForm(memoEval))
                reason = "not-nf";
        }
        catch (Exception e) {
            e.printStackTrace();
            reason = "exception";
        }
        
        if (reason != null) {
            brokenCells.add(cell);
            brokenCellReasons.add(idx + ": " + reason);
        }
    }

    @Override
    public void finished() {
        //noinspection NonStrictComparisonCanBeEquality
        if (brokenCells.size() <= 0) {
            if (verbose)
                System.err.println("Memo eval checker completed successfully");
            return;
        }

        System.err.printf("*** MEMO EVAL CHECKER FOUND BROKEN CELLS ***%n");
        System.err.printf("  Found %d broken cells%n%n", brokenCells.size());

        for (int idx = 0; idx < brokenCells.size(); idx++) {
            var cell = brokenCells.get(idx);
            var reason = brokenCellReasons.get(idx);
            System.err.printf("%s: %s%n  %s%n", reason, cell, machine.valueToString(cell.toValue()));
            System.err.printf("  memoEval: %s%n", machine.valueToString(cell.getMemoEval()));
        }

//        heap.dumpHeap(System.err);

        throw new HeapValidationError();
    }
}
