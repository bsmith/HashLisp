package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ConsPair;
import uk.bs338.hashLisp.jproto.IMachine;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.Objects;
import java.util.Optional;

/* XXX: instead of just wrapping a heap, wrap a whole Context, optionally with Evaluators, Readers etc */

public class WrappedHeap implements IMachine<WrappedValue> {
    private final HonsMachine machine;
    private WrappedValue nil;
    private WrappedValue symbolTag;

    private WrappedHeap(HonsMachine machine) {
        this.machine = machine;
    }
    
    public static @NotNull WrappedHeap wrap(HonsMachine machine) {
        return new WrappedHeap(machine);
    }

    public HonsMachine getMachine() {
        return machine;
    }
    
    public @NotNull WrappedValue wrap(@NotNull HonsValue value) {
        return WrappedValue.wrap(machine, value);
    }
    
    /* was 'checkSameHeap' */
    public @NotNull HonsValue unwrap(@NotNull WrappedValue wrapped) {
        if (machine != wrapped.getMachine())
            throw new IllegalArgumentException("Mismatched heap between WrappedValue and WrappedHeap");
        return wrapped.getValue();
    }

    @NotNull
    @Override
    public WrappedValue cons(@NotNull WrappedValue fst, @NotNull WrappedValue snd) {
        return wrap(machine.cons(unwrap(fst), unwrap(snd)));
    }
    
    @NotNull
    @Override
    public ConsPair<WrappedValue> uncons(@NotNull WrappedValue cons) {
        var pair = machine.uncons(unwrap(cons));
        return pair.fmap(this::wrap);
    }

    @NotNull
    @Override
    public WrappedValue makeSymbol(@NotNull WrappedValue name) {
        return wrap(machine.makeSymbol(unwrap(name)));
    }

    @NotNull
    @Override
    public WrappedValue makeSymbol(@NotNull String name) {
        return wrap(machine.makeSymbol(name));
    }

    @Override
    public boolean isSymbol(@NotNull WrappedValue symbol) {
        return machine.isSymbol(unwrap(symbol));
    }

    @NotNull
    @Override
    public WrappedValue symbolName(@NotNull WrappedValue symbol) {
        return wrap(machine.symbolName(unwrap(symbol)));
    }

    @NotNull
    @Override
    public String symbolNameAsString(@NotNull WrappedValue symbol) {
        return machine.symbolNameAsString(unwrap(symbol));
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
    public @NotNull Optional<WrappedValue> getMemoEval(@NotNull WrappedValue val) {
        throw new Error("unimplemented");
    }

    @Override
    public void setMemoEval(@NotNull WrappedValue val, @Nullable WrappedValue evalResult) {
        throw new Error("unimplemented");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedHeap that = (WrappedHeap) o;
        return Objects.equals(machine, that.machine);
    }

    @Override
    public int hashCode() {
        return machine.hashCode();
    }
}
