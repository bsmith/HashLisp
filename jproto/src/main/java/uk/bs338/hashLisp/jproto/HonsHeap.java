package uk.bs338.hashLisp.jproto;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nonnull;

public class HonsHeap {
    @Nonnull
    private final HashMap<Integer, HonsCell> heap;
    
    /* special Cells where the objectHash does not match fst and snd */
    public final static HonsCell nil = new HonsCell(0, "nil");
    public final static HonsCell tagSymbol = new HonsCell(1, "symbol");

    public HonsHeap() {
        heap = new HashMap<>();
        putCell(nil);
        putCell(tagSymbol);
    }
    
    private void putCell(@Nonnull HonsCell cell) {
        heap.put(cell.getObjectHash(), cell);
    }
    
    private HonsCell getCell(@Nonnull LispValue obj) {
        return heap.get(obj.toObjectHash());
    }
    
    private HonsCell getCell(@Nonnull HonsCell cell) {
        return heap.get(cell.getObjectHash());
    }

    @Nonnull
    public LispValue hons(@Nonnull LispValue fst, @Nonnull LispValue snd) throws Exception {
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

        var restCell = getCell(rest);
        if (restCell == null)
            return valueToString(rest, str + " . ");
        
        return listToString(restCell.getFst(), restCell.getSnd(), str + " ");
    }

    public String valueToString(LispValue val) {
        return valueToString(val, "");
    }

    public String valueToString(LispValue val, String accum) {
        if (val.isObjectHash()) {
            var cell = getCell(val);
            if (cell == null)
                return accum + val.toString();
            var special = cell.getSpecial();
            if (special != null)
                return String.format("#%d:%s", cell.getObjectHash(), special);
            if (cell.getFst().equals(tagSymbol.toValue())) {
                String symName = listAsString(cell.getSnd());
                if (symName != null)
                    return accum + symName;
            }
            return accum + "(" + listToString(cell.getFst(), cell.getSnd()) + ")";
        } else {
            return accum + val.toString();
        }
    }
    
    public String listAsString(LispValue list) {
        try {
            ArrayList<Integer> codepoints = new ArrayList<>();
            LispValue cur = list;
            while (!cur.isNil()) {
                int ch = fst(cur).toShortInt();
                codepoints.add(ch);
                cur = snd(cur);
            }
            return new String(codepoints.stream().mapToInt(ch -> ch).toArray(), 0, codepoints.size());
        } catch (Exception e) {
            return null;
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
        var cell = getCell(val);
        if (cell == null)
            throw new Exception("Failed to find cell in heap: " + val);
        return cell.getFst();
    }

    @Nonnull
    public LispValue snd(LispValue val) throws Exception {
        if (!val.isObjectHash())
            return LispValue.nil;
        var cell = getCell(val);
        if (cell == null)
            throw new Exception("Failed to find cell in heap: " + val);
        return cell.getSnd();
    }
    
    public LispValue intList(int nums[]) throws Exception {
        LispValue list = LispValue.nil;
        for (int index = nums.length - 1; index >= 0; index--) {
            int num = nums[index];
            list = hons(LispValue.fromShortInt(num), list);
        }
        return list;
    }
    
    public LispValue makeSymbol(String name) throws Exception {
//        var list = name.codePoints().mapToObj(ch -> LispValue.fromShortInt(ch)).toArray();
        return hons(tagSymbol.toValue(), intList(name.codePoints().toArray()));
    }
}
