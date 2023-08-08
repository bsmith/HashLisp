package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.IValue;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.ValueType;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public class WrappedValue implements IValue, IWrappedValue, IWrappedValue.IGetValue<HonsValue>, IWrappedCons, IWrappedSymbol {
    private final HonsMachine machine;
    private final HonsValue value;

    public WrappedValue(HonsMachine machine, HonsValue value) {
        this.machine = machine;
        this.value = value;
    }
    
    public static @NotNull WrappedValue wrap(HonsMachine machine, HonsValue value) {
        return new WrappedValue(machine, value);
    }
    
    private @NotNull WrappedValue wrap(HonsValue newValue) {
        return new WrappedValue(machine, newValue);
    }
    
    public HonsMachine getMachine() {
        return machine;
    }

    @Override
    public @NotNull HonsValue getValue() {
        return value;
    }

    @Override
    public @NotNull ValueType getType() {
        return value.getType();
    }

    @Override
    public boolean isCons() {
        return value.getType() == ValueType.CONS_REF;
    }

    @Override
    public int toSmallInt() {
        return value.toSmallInt();
    }

//    @Override
    public @NotNull ConsPair<WrappedValue> uncons() {
        return machine.uncons(value).fmap(this::wrap);
    }

    @Override
    public boolean isSymbol() {
        return machine.isSymbol(value);
    }

    @Override
    public IWrappedSymbol makeSymbol() {
        return wrap(machine.makeSymbol(value));
    }

    @Override
    public @NotNull WrappedValue symbolName() {
        return wrap(machine.symbolName(value));
    }

    @Override
    public @NotNull String symbolNameAsString() {
        return machine.symbolNameAsString(value);
    }

    @Override
    public String toString() {
        return machine.valueToString(value);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedValue that = (WrappedValue) o;
        return Objects.equals(machine, that.machine) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(machine, value);
    }

    @Override
    public @NotNull Optional<IWrappedValue> getMemoEval() {
        throw new Error();
    }

    @Override
    public void setMemoEval(@Nullable IWrappedValue expr) {
//        heap.setMemoEval(value, unwrap(expr));
        throw new Error();
    }

    @Override
    public @NotNull String valueToString() {
        return machine.valueToString(value);
    }

    @Override
    public IWrappedCons asCons() {
        return this;
    }

    @Override
    public IWrappedSymbol asSymbol() {
        return this;
    }

    @Override
    public @NotNull WrappedValue fst() {
        return uncons().fst();
    }

    @Override
    public @NotNull WrappedValue snd() {
        return uncons().snd();
    }
}
