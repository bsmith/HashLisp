package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;

public class WrappedValue implements IWrappedValue<HonsValue, WrappedValue> {
    private final HonsHeap heap;
    private final HonsValue value;

    public WrappedValue(HonsHeap heap, HonsValue value) {
        this.heap = heap;
        this.value = value;
    }
    
    public static WrappedValue wrap(HonsHeap heap, HonsValue value) {
        return new WrappedValue(heap, value);
    }
    
    private WrappedValue wrap(HonsValue newValue) {
        return new WrappedValue(heap, newValue);
    }
    
    public HonsHeap getHeap() {
        return heap;
    }

    @Override
    public @NotNull HonsValue getValue() {
        return value;
    }

    @Override
    public boolean isNil() {
        return value.isNil();
    }

    @Override
    public boolean isSymbolTag() {
        return value.isSymbolTag();
    }

    @Override
    public boolean isSmallInt() {
        return value.isSmallInt();
    }

    @Override
    public boolean isConsRef() {
        return value.isConsRef();
    }

    @Override
    public int toSmallInt() {
        return value.toSmallInt();
    }

    @Override
    public @NotNull ConsPair<WrappedValue> uncons() {
        return heap.uncons(value).fmap(this::wrap);
    }

    @Override
    public boolean isSymbol() {
        return heap.isSymbol(value);
    }

    @Override
    public @NotNull WrappedValue symbolName() {
        return wrap(heap.symbolName(value));
    }

    @Override
    public @NotNull String symbolNameAsString() {
        return heap.symbolNameAsString(value);
    }

    @Override
    public String toString() {
        return heap.valueToString(value);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedValue that = (WrappedValue) o;
        return Objects.equals(heap, that.heap) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heap, value);
    }
}
