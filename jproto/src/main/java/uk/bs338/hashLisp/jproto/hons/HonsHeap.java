package uk.bs338.hashLisp.jproto.hons;

import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IHeapVisitor;
import uk.bs338.hashLisp.jproto.ISymbolMixin;
import uk.bs338.hashLisp.jproto.ConsPair;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class HonsHeap implements
    IHeap<HonsValue>,
    ISymbolMixin<HonsValue>
{
    private final HashMap<Integer, HonsCell> heap;
    
    public HonsHeap() {
        heap = new HashMap<>();
        for (HonsValue special : HonsValue.getAllSpecials()) {
            putCell(new HonsCell(special));
        }
    }

    private HonsCell getCell(@Nonnull HonsValue obj) {
        return heap.get(obj.toObjectHash());
    }

    private void putCell(@Nonnull HonsCell cell) {
        heap.put(cell.getObjectHash(), cell);
    }
    
    private HonsCell getCell(@Nonnull HonsCell cell) {
        return heap.get(cell.getObjectHash());
    }

    @Override
    public HonsValue nil() {
        return HonsValue.nil;
    }

    @Override
    public HonsValue makeShortInt(int num) {
        return HonsValue.fromShortInt(num);
    }

    @Override
    public HonsValue symbolTag() {
        return HonsValue.symbolTag;
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
                return accum + val;
            var special = cell.getSpecial();
            if (special != null)
                return String.format("#%d:%s", cell.getObjectHash(), special);
            if (cell.getFst().equals(HonsValue.symbolTag)) {
                String symName = listAsString(this, cell.getSnd());
                if (symName != null)
                    return accum + symName;
            }
            return accum + "(" + listToString(cell.getFst(), cell.getSnd()) + ")";
        } else {
            return accum + val;
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
    public ConsPair<HonsValue> uncons(HonsValue val) {
        if (!val.isObjectHash())
            throw new IllegalArgumentException("Cannot uncons not-cons: " + val);
        var cell = getCell(val);
        if (cell == null)
            throw new IllegalStateException("Failed to find cell in heap: " + val);
        return cell.getPair();
    }
    
    /* XXX getCell is buggy?  What if it's called with a Value that's not a cons? */
    public Optional<HonsValue> getMemoEval(HonsValue val) {
        var cell = getCell(val);
        return Optional.ofNullable(cell.getMemoEval());
    }
    
    /* XXX what if the cell doesn't exist? */
    public void setMemoEval(HonsValue val, HonsValue evalResult) {
        var cell = getCell(val);
        cell.setMemoEval(evalResult);
    }
    
    public void visitValue(HonsValue val, IHeapVisitor<HonsValue> visitor) throws Exception {
        if (val.isNil())
            visitor.visitNil(val);
        else if (val.isShortInt())
            visitor.visitShortInt(val, val.toShortInt());
        else if (this.isSymbol(val))
            visitor.visitSymbol(val, this.symbolName(val));
        else if (val.isConsRef()) {
            var uncons = this.uncons(val);
            visitor.visitCons(val, uncons.fst(), uncons.snd());
        }
        else {
            throw new IllegalArgumentException("couldn't identify value: " + val);
        }
    }
}
