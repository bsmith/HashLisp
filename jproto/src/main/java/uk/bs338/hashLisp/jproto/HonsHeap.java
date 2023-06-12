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

    public String valueToString(LispValue val) {
        if (val.isObjectHash()) {
            int objectHash = val.toObjectHash().getAsInt();
            var cell = heap.get(objectHash);
            if (cell == null)
                return val.toString();
            var fst = valueToString(cell.getFst());
            if (cell.getSnd().isNil())
                return String.format("(%s)", fst);
            var snd = valueToString(cell.getSnd());
            return String.format("(%s . %s)", fst, snd);
        } else {
            return val.toString();
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
}
