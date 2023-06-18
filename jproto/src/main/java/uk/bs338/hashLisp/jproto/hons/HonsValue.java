package uk.bs338.hashLisp.jproto.hons;

import uk.bs338.hashLisp.jproto.IValue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;

/* this is a value class */
public final class HonsValue implements IValue {
    /* 31-bit signed integers have the range [-(2**30-1)-1, 2**30-1] */
    public final static int SHORTINT_MIN = -1073741823 - 1;
    public final static int SHORTINT_MAX = 1073741823;

    /* We are an immutable object wrapping this primitive */
    private final int value;

    private HonsValue(int value) {
        this.value = value;
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
    
    public String getSpecialName() {
        if (this.isSpecial())
            return specialNames.get(this.value >> 1);
        else
            return null;
    }

    @Override
    public boolean isSymbolTag() {
        return this.value == symbolTag.value;
    }

    @Nonnull
    public static HonsValue fromShortInt(int num) {
        assert SHORTINT_MIN < num && num < SHORTINT_MAX;
        //noinspection PointlessBitwiseExpression
        return new HonsValue((num << 1) | 0);
    }

    @Nonnull
    public static HonsValue fromObjectHash(int hash) {
        assert SHORTINT_MIN < hash && hash < SHORTINT_MAX;
        return new HonsValue((hash << 1) | 1);
    }

    public int toShortInt() throws NoSuchElementException {
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

    public boolean isShortInt() {
        return (value & 1) == 0;
    }

    public boolean isObjectHash() {
        /* nil is not an object hash */
        return (value & 1) == 1 && value != 1;
    }
    
    public boolean isConsRef() {
        return isObjectHash() && !isSpecial();
    }

    public boolean isNil() {
        return value == 1;
    }

    /* XXX Are these two operations the best?  Most javaish? */
    /* XXX using fromInteger does some checks for overflow, but not all? */
    public static HonsValue applyShortIntOperation(IntUnaryOperator func, HonsValue val) {
        var rvInt = func.applyAsInt(val.value >> 1);
        return val.isShortInt() ? HonsValue.fromShortInt(rvInt) : nil;
    }

    public static HonsValue applyShortIntOperation(IntBinaryOperator func, HonsValue left, HonsValue right) {
        if (!left.isShortInt() || !right.isShortInt()) {
            return nil;
        }
        var rvInt = func.applyAsInt(left.value >> 1, right.value >> 1);
        return HonsValue.fromShortInt(rvInt);
    }

    /* this is an immutable record, ie the Object is equivalent to its int value */
    @Override
    public String toString() {
        if (this.isNil())
            return "nil";
        if ((this.value & 1) == 1) {
            if (this.isSpecial())
                return "#" + (this.value >> 1) + ":" + this.getSpecialName();
            return "#" + (this.value >> 1);
        } else {
            return Integer.toString(this.value >> 1);
        }
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
