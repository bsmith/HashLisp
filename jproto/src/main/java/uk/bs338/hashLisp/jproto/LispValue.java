package uk.bs338.hashLisp.jproto;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

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
    public final static LispValue nil = new LispValue(1);
    // public final static LispValue nil = LispValue.fromObjectHash(0);

    public static LispValue fromShortInt(int num) {
        assert SHORTINT_MIN < num && num < SHORTINT_MAX;
        return new LispValue((num << 1) | 0);
    }

    public static LispValue fromObjectHash(int hash) {
        assert SHORTINT_MIN < hash && hash < SHORTINT_MAX;
        return new LispValue((hash << 1) | 1);
    }

    public OptionalInt toShortInt() {
        if ((value & 1) == 0) {
            return OptionalInt.of(value >> 1);
        }
        return OptionalInt.empty(); 
    }

    public OptionalInt toObjectHash() {
        if ((value & 1) == 1) {
            return OptionalInt.of(value >> 1);
        }
        return OptionalInt.empty();
    }

    public boolean isShortInt() {
        return (value & 1) == 0;
    }

    public boolean isObjectHash() {
        return (value & 1) == 1;
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
