package uk.bs338.hashLisp.jproto;

import java.util.HashMap;

import javax.annotation.Nonnull;

public class HonsHeap {
    @Nonnull
    private final HashMap<Integer, HonsCell> heap;

    public HonsHeap() {
        heap = new HashMap<>();
        heap.put(0, HonsCell.nil);
    }

    @Nonnull
    public LispValue hons(@Nonnull LispValue fst, @Nonnull LispValue snd) throws Exception {
        HonsCell cell = new HonsCell(fst, snd);
        do {
            HonsCell heapCell = heap.get(cell.getObjectHash());
            /* equals compares the hash, fst, and snd, so we can return this cell as our value */
            if (heapCell != null && heapCell.equals(cell)) {
                return cell.toValue();
            }
            /* need to put the cell into the heap */
            if (heapCell == null) {
                heap.put(cell.getObjectHash(), cell);
                return cell.toValue();
            }
            /* otherwise we have a hash collision! */
            cell.bumpObjectHash();
        } while (true);
        /* XXX loop back around to line 19 */
        // throw new Exception("hash collision");
    }

    public String listToString(LispValue head, LispValue rest) {
        return listToString(head, rest, "");
    }

    public String listToString(LispValue head, LispValue rest, String accum) {
        var str = valueToString(head, accum);
        
        if (rest.isNil())
            return str;

        if (!rest.isObjectHash())
            // return accum + String.format("%s . %s", str, valueToString(rest));
            return valueToString(rest, str + " . ");

        var restCell = heap.get(rest.toObjectHash().getAsInt());
        if (restCell == null)
            return valueToString(rest, str + " . ");
        
        return listToString(restCell.getFst(), restCell.getSnd(), str + " ");
    }

    public String valueToString(LispValue val) {
        return valueToString(val, "");
    }

    public String valueToString(LispValue val, String accum) {
        if (val.isObjectHash()) {
            int objectHash = val.toObjectHash().getAsInt();
            var cell = heap.get(objectHash);
            if (cell == null)
                return accum + val.toString();
            return accum + "(" + listToString(cell.getFst(), cell.getSnd()) + ")";
        } else {
            return accum + val.toString();
        }
    }

    public void dumpHeap() {
        System.out.printf("HonsHeap.dumpHeap(size=%d)%n", heap.size());

        var sortedKeys = heap.keySet().stream().sorted().toArray();
        for (var key : sortedKeys) {
            HonsCell cell = heap.get(key);
            System.out.printf("%s%n  %s%n", cell, valueToString(cell.toValue()));
        }
    }

    @Nonnull
    public LispValue fst(LispValue val) throws Exception {
        if (!val.isObjectHash())
            return LispValue.nil;
        var cell = heap.get(val.toObjectHash().getAsInt());
        if (cell == null)
            throw new Exception("Failed to find cell in heap: " + val);
        return cell.getFst();
    }

    @Nonnull
    public LispValue snd(LispValue val) throws Exception {
        if (!val.isObjectHash())
            return LispValue.nil;
        var cell = heap.get(val.toObjectHash().getAsInt());
        if (cell == null)
            throw new Exception("Failed to find cell in heap: " + val);
        return cell.getSnd();
    }
}
