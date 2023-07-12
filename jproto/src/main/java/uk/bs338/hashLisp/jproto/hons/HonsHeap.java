package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.ISymbolMixin;
import uk.bs338.hashLisp.jproto.ConsPair;

import java.io.PrintStream;
import java.util.Optional;

public class HonsHeap implements
    IHeap<HonsValue>,
    ISymbolMixin<HonsValue>
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

    public void dumpHeap(@NotNull PrintStream stream) {
        dumpHeap(stream, false);
    }
    
    public void dumpHeap(@NotNull PrintStream stream, boolean onlyWithMemoValues) {
        stream.printf("HonsHeap.dumpHeap(size=%d,load=%d)%n", table.length, tableLoad);
        
        for (var cell : table) {
            if (cell != null && (!onlyWithMemoValues || cell.getMemoEval() != null))
                stream.printf("%s: %s%n  %s%n", cell.getObjectHash(), cell, valueToString(cell.toValue()));
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
