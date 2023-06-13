package uk.bs338.hashLisp.jproto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;

/* this is a value class */
public final class LispValue {
    /* 31-bit signed integers have the range [-(2**30-1)-1, 2**30-1] */
    public final static int SHORTINT_MIN = -1073741823 - 1;
    public final static int SHORTINT_MAX = 1073741823;

    /* We are a immutable object wrapping this primitive */
    private final int value;

    private LispValue(int value) {
        this.value = value;
    }

    /* nil is the object hash 0 */
    public final static LispValue nil = LispValue.fromObjectHash(0);
    public final static LispValue tagSymbol = LispValue.fromObjectHash(1);
    private final static List<LispValue> allSpecials = List.of(nil, tagSymbol);
    private final static List<String> specialNames = List.of("nil", "symbol");
    
    public static Iterable<LispValue> getAllSpecials() {
        return List.of(nil, tagSymbol);
    }
    
    public String getSpecialName() {
        int objectHash = this.value >> 1;
        if (objectHash >= 0 && objectHash < specialNames.size())
            return specialNames.get(this.value >> 1);
        else
            return null;
    }

    @Nonnull
    public static LispValue fromShortInt(int num) {
        assert SHORTINT_MIN < num && num < SHORTINT_MAX;
        //noinspection PointlessBitwiseExpression
        return new LispValue((num << 1) | 0);
    }

    @Nonnull
    public static LispValue fromObjectHash(int hash) {
        assert SHORTINT_MIN < hash && hash < SHORTINT_MAX;
        return new LispValue((hash << 1) | 1);
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

    public boolean isNil() {
        return value == 1;
    }

    /* XXX Are these two operations the best?  Most javaish? */
    /* XXX using fromInteger does some checks for overflow, but not all? */
    public static LispValue applyShortIntOperation(IntUnaryOperator func, LispValue val) {
        var rvInt = func.applyAsInt(val.value >> 1);
        return val.isShortInt() ? LispValue.fromShortInt(rvInt) : nil;
    }

    public static LispValue applyShortIntOperation(IntBinaryOperator func, LispValue left, LispValue right) {
        if (!left.isShortInt() || !right.isShortInt()) {
            return nil;
        }
        var rvInt = func.applyAsInt(left.value >> 1, right.value >> 1);
        return LispValue.fromShortInt(rvInt);
    }

    /* this is an immutable record, ie the Object is equivalent to it's int value */
    @Override
    public String toString() {
        if (this.isNil())
            return "nil";
        return ((this.value & 1) == 1 ? "#" : "") + Integer.toString(this.value >> 1);
    }

    /* this is an immutable record, ie the Object is equivalent to it's int value */
    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof LispValue)) return false;
        return this.value == ((LispValue)other).value;
    }
}
