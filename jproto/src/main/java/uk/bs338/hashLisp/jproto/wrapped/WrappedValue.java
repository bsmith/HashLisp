package uk.bs338.hashLisp.jproto.wrapped;

import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;

public class WrappedValue implements IWrappedValue<HonsValue> {
    private final HonsHeap heap;
    private final HonsValue value;

    public WrappedValue(HonsHeap heap, HonsValue value) {
        this.heap = heap;
        this.value = value;
    }
    
    public HonsHeap getHeap() {
        return heap;
    }

    @Override
    public HonsValue getValue() {
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
    public ConsPair<HonsValue> uncons() {
        return heap.uncons(value);
    }

    @Override
    public boolean isSymbol() {
        return heap.isSymbol(value);
    }

    @Override
    public HonsValue symbolName() {
        return heap.symbolName(value);
    }

    @Override
    public String symbolNameAsString() {
        return heap.symbolNameAsString(value);
    }

    @Override
    public String toString() {
        return heap.valueToString(value);
    }

    @Override
    public boolean equals(Object o) {
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
