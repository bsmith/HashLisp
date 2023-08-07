package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.IValue;
import uk.bs338.hashLisp.jproto.ValueType;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/* this is a value class */
public final class HonsValue implements IValue {
    /* 31-bit signed integers have the range [-(2**30-1)-1, 2**30-1] */
    public final static int SMALLINT_MIN = -1073741823 - 1;
    public final static int SMALLINT_MAX = 1073741823;

    /* We are an immutable object wrapping this primitive */
    private final int value;

    private HonsValue(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }

    /* nil is the object hash 0 */
    public final static HonsValue nil = HonsValue.fromObjectHash(0);
    public final static HonsValue symbolTag = HonsValue.fromObjectHash(1);
    private final static List<HonsValue> allSpecials = List.of(nil, symbolTag);
    private final static List<String> specialNames = List.of("nil", "symbol");
    
    public static Iterable<HonsValue> getAllSpecials() {
        return allSpecials;
    }
    
    public boolean isSpecial() {
        int objectHash = this.value >> 1;
        return (this.value & 1) == 1 && objectHash >= 0 && objectHash < specialNames.size();
    }
    
    public @Nullable String getSpecialName() {
        if (this.isSpecial())
            return specialNames.get(this.value >> 1);
        else
            return null;
    }

    @NotNull
    public static HonsValue fromSmallInt(int num) {
        assert SMALLINT_MIN <= num && num <= SMALLINT_MAX;
        //noinspection PointlessBitwiseExpression
        return new HonsValue((num << 1) | 0);
    }

    @NotNull
    public static HonsValue fromObjectHash(int hash) {
        assert SMALLINT_MIN < hash && hash < SMALLINT_MAX;
        return new HonsValue((hash << 1) | 1);
    }

    public int toSmallInt() throws NoSuchElementException {
        if ((value & 1) == 0) {
            return value >> 1;
        }
        throw new NoSuchElementException(); 
    }

    public int toObjectHash() throws NoSuchElementException {
        if ((value & 1) == 1) {
            return value >> 1;
        }
        throw new NoSuchElementException();
    }

    @Override
    public @NotNull ValueType getType() {
        if ((value & 1) == 0) {
            return ValueType.SMALL_INT;
        }
        else if (value == 1) {
            return ValueType.NIL;
        }
        else if (value == 3) {
            return ValueType.SYMBOL_TAG;
        }
        else {
            return ValueType.CONS_REF;
        }
    }

    /* XXX Are these two operations the best?  Most javaish? */
    /* XXX using fromInteger does some checks for overflow, but not all? */
    public static HonsValue applySmallIntOperation(@NotNull IntUnaryOperator func, @NotNull HonsValue val) {
        if (val.getType() != ValueType.SMALL_INT)
            return nil;
        var rvInt = func.applyAsInt(val.value >> 1);
        return HonsValue.fromSmallInt(rvInt);
    }

    public static HonsValue applySmallIntOperation(@NotNull IntBinaryOperator func, @NotNull HonsValue left, @NotNull HonsValue right) {
        if (left.getType() != ValueType.SMALL_INT || right.getType() != ValueType.SMALL_INT) {
            return nil;
        }
        var rvInt = func.applyAsInt(left.value >> 1, right.value >> 1);
        return HonsValue.fromSmallInt(rvInt);
    }

    /* this is an immutable record, ie the Object is equivalent to its int value */
    @Override
    public @NotNull String toString() {
        return switch (this.getType()) {
            case NIL -> "nil";
            case SYMBOL_TAG -> "#1:symbol";
            case SMALL_INT -> Integer.toString(this.value >> 1);
            case CONS_REF -> "#" + (this.value >> 1);
        };
    }

    /* this is an immutable record, ie the Object is equivalent to its int value */
    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof HonsValue)) return false;
        return this.value == ((HonsValue)other).value;
    }
}
