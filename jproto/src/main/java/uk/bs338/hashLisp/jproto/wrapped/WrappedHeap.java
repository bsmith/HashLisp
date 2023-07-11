package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;

/* XXX: instead of just wrapping a heap, wrap a whole Context, optionally with Evaluators, Readers etc */

public class WrappedHeap implements IHeap<WrappedValue> {
    private final HonsHeap heap;
    private WrappedValue nil;
    private WrappedValue symbolTag;

    public WrappedHeap(HonsHeap heap) {
        this.heap = heap;
    }

    public HonsHeap getHeap() {
        return heap;
    }
    
    public WrappedValue wrap(HonsValue value) {
        return WrappedValue.wrap(heap, value);
    }
    
    /* was 'checkSameHeap' */
    public HonsValue unwrap(WrappedValue wrapped) {
        if (heap != wrapped.getHeap())
            throw new IllegalArgumentException("Mismatched heap between WrappedValue and WrappedHeap");
        return wrapped.getValue();
    }

    @NotNull
    @Override
    public WrappedValue cons(@NotNull WrappedValue fst, @NotNull WrappedValue snd) {
        return wrap(heap.cons(unwrap(fst), unwrap(snd)));
    }
    
    @NotNull
    @Override
    public ConsPair<WrappedValue> uncons(@NotNull WrappedValue cons) {
        var pair = heap.uncons(unwrap(cons));
        return pair.fmap(this::wrap);
    }

    @NotNull
    @Override
    public WrappedValue makeSymbol(@NotNull WrappedValue name) {
        return wrap(heap.makeSymbol(unwrap(name)));
    }

    @NotNull
    @Override
    public WrappedValue makeSymbol(@NotNull String name) {
        return wrap(heap.makeSymbol(name));
    }

    @Override
    public boolean isSymbol(@NotNull WrappedValue symbol) {
        return heap.isSymbol(unwrap(symbol));
    }

    @NotNull
    @Override
    public WrappedValue symbolName(@NotNull WrappedValue symbol) {
        return wrap(heap.symbolName(unwrap(symbol)));
    }

    @NotNull
    @Override
    public String symbolNameAsString(@NotNull WrappedValue symbol) {
        return heap.symbolNameAsString(unwrap(symbol));
    }

    @Override
    public @NotNull WrappedValue nil() {
        if (nil == null)
            nil = wrap(HonsValue.nil);
        return nil;
    }

    @Override
    public @NotNull WrappedValue makeSmallInt(int num) {
        return wrap(HonsValue.fromSmallInt(num));
    }

    @Override
    public @NotNull WrappedValue symbolTag() {
        if (symbolTag == null)
            symbolTag = wrap(HonsValue.symbolTag);
        return symbolTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedHeap that = (WrappedHeap) o;
        return Objects.equals(heap, that.heap);
    }

    @Override
    public int hashCode() {
        return heap.hashCode();
    }
}
