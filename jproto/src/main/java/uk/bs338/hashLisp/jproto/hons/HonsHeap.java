package uk.bs338.hashLisp.jproto.hons;

import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.Pair;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class HonsHeap implements IHeap {
    private final HashMap<Integer, HonsCell> heap;
    
    public HonsHeap() {
        heap = new HashMap<>();
        for (HonsValue special : HonsValue.getAllSpecials()) {
            putCell(new HonsCell(special));
        }
    }
    
    private void putCell(@Nonnull HonsCell cell) {
        heap.put(cell.getObjectHash(), cell);
    }
    
    private HonsCell getCell(@Nonnull HonsValue obj) {
        return heap.get(obj.toObjectHash());
    }
    
    private HonsCell getCell(@Nonnull HonsCell cell) {
        return heap.get(cell.getObjectHash());
    }

    @Nonnull
    public HonsValue cons(@Nonnull HonsValue fst, @Nonnull HonsValue snd) {
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

    public String listToString(HonsValue head, HonsValue rest) {
        return listToString(head, rest, "");
    }

    public String listToString(HonsValue head, HonsValue rest, String accum) {
        var str = valueToString(head, accum);
        
        if (rest.isNil())
            return str;

        if (!rest.isObjectHash())
            // return accum + String.format("%s . %s", str, valueToString(rest));
            return valueToString(rest, str + " . ");

        var restCell = getCell(rest);
        if (restCell == null)
            return valueToString(rest, str + " . ");
        
        return listToString(restCell.getFst(), restCell.getSnd(), str + " ");
    }

    public String valueToString(HonsValue val) {
        return valueToString(val, "");
    }

    public String valueToString(HonsValue val, String accum) {
        if (val.isObjectHash()) {
            var cell = getCell(val);
            if (cell == null)
                return accum + val.toString();
            var special = cell.getSpecial();
            if (special != null)
                return String.format("#%d:%s", cell.getObjectHash(), special);
            if (cell.getFst().equals(HonsValue.tagSymbol)) {
                String symName = listAsString(this, cell.getSnd());
                if (symName != null)
                    return accum + symName;
            }
            return accum + "(" + listToString(cell.getFst(), cell.getSnd()) + ")";
        } else {
            return accum + val.toString();
        }
    }

    public void dumpHeap(PrintStream stream) {
        stream.printf("HonsHeap.dumpHeap(size=%d)%n", heap.size());

        var sortedHeap = heap.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        
        for (var entry : sortedHeap) {
            HonsCell cell = entry.getValue();
            stream.printf("%s: %s%n  %s%n", entry.getKey(), cell, valueToString(cell.toValue()));
        }
    }

    @Nonnull
    public Pair<HonsValue> uncons(HonsValue val) throws Exception {
        if (!val.isObjectHash())
            throw new IllegalArgumentException("Cannot uncons not-cons: " + val);
        var cell = getCell(val);
        if (cell == null)
            throw new Exception("Failed to find cell in heap: " + val);
        return cell.getPair();
    }
}
