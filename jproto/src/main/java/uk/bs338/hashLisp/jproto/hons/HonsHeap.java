package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.ValueType;

import java.util.ArrayList;
import java.util.Optional;

public class HonsHeap implements
    IHeap<HonsValue>
{
    private HonsCell[] table;
    private int tableLoad;
    
    public HonsHeap() {
        this(2048);
    }
    
    public HonsHeap(int initialSize) {
        table = new HonsCell[initialSize];
        tableLoad = 0;
        for (HonsValue special : HonsValue.getAllSpecials()) {
            putCell(new HonsCell(special));
        }
    }
    
    public @Nullable HonsCell getCell(@NotNull HonsValue obj) {
        return getCell(obj.toObjectHash());
    }
    
    private void expandTable() {
        if (table.length > 1024*1024)
            throw new Error("Refusing to increase the heap over 1Mi cells");
        
        HonsCell[] oldTable = table;
        table = new HonsCell[oldTable.length * 2];
        tableLoad = 0;
        
        for (final HonsCell cell : oldTable) {
            if (cell != null)
                putCell(cell);
        }
    }
    
    /* Mix up the high-order bits into the low-order bits for a bit more entropy */
    private int mixHash(int objectHash) {
        return objectHash | (objectHash >> 10);
    }

    /* putCell and getCell implement our hash table */
    private @NotNull HonsCell putCell(@NotNull HonsCell cell) {
        if (tableLoad > table.length / 2)
            expandTable();
        int objectHash = cell.getObjectHash();
        int hash = mixHash(objectHash);
        for (int offset = 0; offset < table.length; offset++) {
            int tableIdx = (hash + offset) % table.length;
            HonsCell tableCell = table[tableIdx];
            if (tableCell == null) {
                table[tableIdx] = cell;
                tableLoad++;
                return cell;
            }
            if (tableCell.getObjectHash() == objectHash)
                return tableCell;
        }
        throw new Error("Table full");
    }
    
    private @Nullable HonsCell getCell(int objectHash) {
        int hash = mixHash(objectHash);
        for (int offset = 0; offset < table.length; offset++) {
            HonsCell cell = table[(hash + offset) % table.length];
            if (cell == null)
                return null;
            if (cell.getObjectHash() == objectHash)
                return cell;
        }
        return null;
    }

    @NotNull
    public HonsValue cons(@NotNull HonsValue fst, @NotNull HonsValue snd) {
        HonsCell cell = new HonsCell(fst, snd);
        do {
            HonsCell heapCell = getCell(cell.getObjectHash());
            /* equals compares the hash, fst, and snd, so we can return this cell as our value */
            if (heapCell != null && heapCell.equals(cell)) {
                return cell.toValue();
            }
            /* need to put the cell into the heap */
            if (heapCell == null) {
                return putCell(cell).toValue();
            }
            /* otherwise we have a hash collision! */
            cell.bumpObjectHash();
        } while (true);
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
            if (!(cellByObjectHash != null &&
                    cellByObjectHash.getMemoEval() == null &&
                    cellByObjectHash.getObjectHash() == special.toObjectHash() &&
                    cellByObjectHash.getFst() == HonsValue.nil &&
                    cellByObjectHash.getSnd() == HonsValue.nil))
                throw new HeapValidationError("Heap validation failed while validating special: " + special + "; got cell: " + cellByObjectHash);
        }

        /* The heap is valid if two conditions pass:
         *     1. Each cell is retrievable by calling getCell with its objectHash (via a HonsValue)
         *     2. Each cell is retrievable by constructing a new Cell with the same fst & snd
         */
        var brokenCells = new ArrayList<Integer>();

        for (int idx = 0; idx < table.length; idx++) {
            var cell = table[idx];
            if (cell == null)
                continue;
            var val = cell.toValue();
            assert val.toObjectHash() == cell.getObjectHash();
            var retrievedByObjectHash = getCell(val);
            if (cell != retrievedByObjectHash) {
                System.err.printf("  failed by ObjectHash at 0x%x: %s != %s%n", idx, cell, retrievedByObjectHash);
                brokenCells.add(idx);
            }
            
            /* Special cells are checked above, and do not have fst/snd values stored */
            if (!cell.toValue().isSpecial()) {
                var retrievedByCell = getCell(cons(cell.getFst(), cell.getSnd()));
                if (cell != retrievedByCell) {
                    System.err.printf("  failed by Cell at 0x%x: %s != %s%n", idx, cell, retrievedByCell);
                    brokenCells.add(idx);
                }
            }
        }
        
        if (brokenCells.size() > 0) {
            System.err.printf("*** HEAP FAILED VALIDATION ***%n");
            System.err.printf("  Found %d broken cells%n%n", brokenCells.size());
            
            /* create a temporary machine */
            HonsMachine machine = new HonsMachine(this);
            for (var idx : brokenCells) {
                var cell = table[idx];
                System.err.printf("0x%x: %s%n  %s%n", idx, cell, PrettyPrinter.valueToString(machine, cell.toValue()));
            }
            
            machine.dumpMachine(System.err);
            
            throw new HeapValidationError();
        } else {
            if (verbose)
                System.err.println("Heap validation completed successfully");
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    @Contract("_ -> param1")
    public <V extends IIterateHeapVisitor> @NotNull V iterateHeap(@NotNull V visitor) {
        for (int idx = 0; idx < table.length; idx++) {
            var cell = table[idx];
            if (cell != null)
                visitor.visit(idx, cell);
        }
        visitor.finished();
        return visitor;
    }

    @NotNull
    public ConsPair<HonsValue> uncons(@NotNull HonsValue val) {
        if (val.getType() != ValueType.CONS_REF)
            throw new IllegalArgumentException("Cannot uncons not-cons: " + val);
        var cell = getCell(val);
        if (cell == null)
            throw new IllegalStateException("Failed to find cell in heap: " + val);
        return cell.getPair();
    }
    
    public @NotNull Optional<HonsValue> getMemoEval(@NotNull HonsValue val) {
        if (val.getType() != ValueType.CONS_REF)
            return Optional.empty();
        return Optional.ofNullable(getCell(val)).map(HonsCell::getMemoEval);
    }
    
    public void setMemoEval(@NotNull HonsValue val, @Nullable HonsValue evalResult) {
        if (val.getType() != ValueType.CONS_REF)
            throw new IllegalArgumentException("can't setMemoEval if its not a ConsRef");
        var cell = getCell(val);
        if (cell == null)
            throw new IllegalStateException("can't find cell for ConsRef: " + val);
        cell.setMemoEval(evalResult);
    }

    public int getSize() {
        return table.length;
    }

    public int getTableLoad() {
        return tableLoad;
    }
}
