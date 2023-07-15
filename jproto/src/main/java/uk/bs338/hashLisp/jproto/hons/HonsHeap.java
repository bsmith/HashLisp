package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.ISymbolMixin;
import uk.bs338.hashLisp.jproto.ConsPair;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HonsHeap implements
    IHeap<HonsValue>,
    ISymbolMixin<HonsValue>
{
    private final @NotNull HashMap<Integer, HonsCell> heap;
    
    public HonsHeap() {
        heap = new HashMap<>();
        for (HonsValue special : HonsValue.getAllSpecials()) {
            putCell(new HonsCell(special));
        }
    }
    
    public HonsCell getCell(@NotNull HonsValue obj) {
        return heap.get(obj.toObjectHash());
    }

    private void putCell(@NotNull HonsCell cell) {
        heap.put(cell.getObjectHash(), cell);
    }
    
    private HonsCell getCell(@NotNull HonsCell cell) {
        return heap.get(cell.getObjectHash());
    }

    @Override
    public @NotNull HonsValue nil() {
        return HonsValue.nil;
    }

    @Override
    public @NotNull HonsValue makeSmallInt(int num) {
        return HonsValue.fromSmallInt(num);
    }

    @Override
    public @NotNull HonsValue symbolTag() {
        return HonsValue.symbolTag;
    }

    @NotNull
    public HonsValue cons(@NotNull HonsValue fst, @NotNull HonsValue snd) {
        HonsCell cell = new HonsCell(fst, snd);
        do {
            HonsCell heapCell = getCell(cell);
            /* equals compares the hash, fst, and snd, so we can return this cell as our value */
            if (heapCell != null && heapCell.equals(cell)) {
                return cell.toValue();
            }
            /* need to put the cell into the heap */
            if (heapCell == null) {
                putCell(cell);
                return cell.toValue();
            }
            /* otherwise we have a hash collision! */
            cell.bumpObjectHash();
        } while (true);
    }

    public void dumpHeap(@NotNull PrintStream stream) {
        dumpHeap(stream, false);
    }
    
    public void dumpHeap(@NotNull PrintStream stream, boolean onlyWithMemoValues) {
        stream.printf("HonsHeap.dumpHeap(size=%d)%n", heap.size());

        var sortedHeap = heap.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        
        for (var entry : sortedHeap) {
            HonsCell cell = entry.getValue();
            if (!onlyWithMemoValues || cell.getMemoEval() != null)
                stream.printf("%s: %s%n  %s%n", entry.getKey(), cell, valueToString(cell.toValue()));
        }
    }
    
    public void validateHeap() {
        validateHeap(false);
    }
    
    public void validateHeap(boolean verbose) {
        if (verbose)
            System.err.println("Starting heap validation");
        
        /* Validate that special entries are still correct */
        for (HonsValue special : HonsValue.getAllSpecials()) {
            var cellByObjectHash = getCell(special);
            var cellByCell = getCell(new HonsCell(special));
            if (cellByObjectHash != cellByCell)
                throw new HeapValidationError("Heap validation failed while validating special: " + special);
        }

        /* The heap is valid if two conditions pass:
         *     1. Each cell is retrievable by calling getCell with its objectHash (via a HonsValue)
         *     2. Each cell is retrievable by constructing a new Cell with the same fst & snd
         */
        var brokenEntries = new ArrayList<Map.Entry<Integer, HonsCell>>();

        for (var entry : heap.entrySet()) {
            var cell = entry.getValue();
            
            var val = cell.toValue();
            assert val.toObjectHash() == cell.getObjectHash();
            var retrievedByObjectHash = getCell(val);
            if (cell != retrievedByObjectHash)
                brokenEntries.add(entry);
            
            /* Special cells are checked above, and do not have fst/snd values stored */
            if (!cell.toValue().isSpecial()) {
                var newCell = new HonsCell(cell.getFst(), cell.getSnd());
                var retrievedByCell = getCell(newCell);
                if (cell != retrievedByCell)
                    brokenEntries.add(entry);
            }
        }
        
        if (brokenEntries.size() > 0) {
            System.err.printf("*** HEAP FAILED VALIDATION ***%n");
            System.err.printf("  Found %d broken cells%n%n", brokenEntries.size());
            
            brokenEntries.sort(Map.Entry.comparingByKey());
            for (var entry : brokenEntries) {
                System.err.printf("%s: %s%n  %s%n", entry.getKey(), entry.getValue(), valueToString(entry.getValue().toValue()));
            }
            
            throw new HeapValidationError();
        } else {
            if (verbose)
                System.err.println("Heap validation completed successfully");
        }
    }

    @NotNull
    public ConsPair<HonsValue> uncons(@NotNull HonsValue val) {
        if (!val.isObjectHash())
            throw new IllegalArgumentException("Cannot uncons not-cons: " + val);
        var cell = getCell(val);
        if (cell == null)
            throw new IllegalStateException("Failed to find cell in heap: " + val);
        return cell.getPair();
    }
    
    public @NotNull Optional<HonsValue> getMemoEval(@NotNull HonsValue val) {
        if (!val.isConsRef())
            return Optional.empty();
        return Optional.ofNullable(getCell(val)).map(HonsCell::getMemoEval);
    }
    
    public void setMemoEval(@NotNull HonsValue val, @NotNull HonsValue evalResult) {
        if (!val.isConsRef())
            throw new IllegalArgumentException("can't setMemoEval if its not a ConsRef");
        var cell = getCell(val);
        if (cell == null)
            throw new IllegalStateException("can't find cell for ConsRef: " + val);
        cell.setMemoEval(evalResult);
    }
}
