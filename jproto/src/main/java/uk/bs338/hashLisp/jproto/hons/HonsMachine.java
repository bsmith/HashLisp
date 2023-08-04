package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.IMachine;
import uk.bs338.hashLisp.jproto.ISymbolMixin;

import java.util.Optional;

public class HonsMachine implements IMachine<HonsValue>, ISymbolMixin<HonsValue> {
    private final HonsHeap heap;
    
    public HonsMachine() {
        heap = new HonsHeap();
    }

    public HonsMachine(HonsHeap heap) {
        this.heap = heap;
    }

    public HonsHeap getHeap() {
        return heap;
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

    @Override
    @NotNull
    public HonsValue cons(@NotNull HonsValue fst, @NotNull HonsValue snd) {
        return heap.cons(fst, snd);
    }

    @Override
    public @NotNull ConsPair<HonsValue> uncons(@NotNull HonsValue val) {
        return heap.uncons(val);
    }

    @Override
    public @NotNull Optional<HonsValue> getMemoEval(@NotNull HonsValue val) {
        return heap.getMemoEval(val);
    }

    @Override
    public void setMemoEval(@NotNull HonsValue val, @Nullable HonsValue evalResult) {
        heap.setMemoEval(val, evalResult);
    }

    @Contract("_ -> param1")
    public <V extends IIterateHeapVisitor> @NotNull V iterateHeap(@NotNull V visitor) {
        return heap.iterateHeap(visitor);
    }

    @Nullable
    public HonsCell getCell(@NotNull HonsValue obj) {
        return heap.getCell(obj);
    }
}
