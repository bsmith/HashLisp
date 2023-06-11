package uk.bs338.hashLisp.jproto;

import java.util.Optional;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public final class LispValue {
    /* 31-bit signed integers have the range [-(2**30-1)-1, 2**30-1] */
    private final static int int_min = -1073741823 - 1;
    private final static int int_max = 1073741823;

    /* We are a immutable object wrapping this primitive */
    private final int value;

    private LispValue(int value) {
        this.value = value;
    }

    /* nil is the object hash 0 */
    public final static LispValue nil = new LispValue(1);
    // public final static LispValue nil = LispValue.fromObjectHash(0);

    public static LispValue fromInteger(int num) {
        assert int_min < num && num < int_max;
        return new LispValue((num << 1) | 0);
    }

    public static LispValue fromObjectHash(int hash) {
        assert int_min < hash && hash < int_max;
        return new LispValue((hash << 1) | 1);
    }

    public Optional<Integer> toInteger() {
        if ((value & 1) == 0) {
            return Optional.of(value >> 1);
        }
        return Optional.empty(); 
    }

    public Optional<Integer> toObjectHash() {
        if ((value & 1) == 1) {
            return Optional.of(value >> 1);
        }
        return Optional.empty();
    }

    public boolean isInteger() {
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
    public static LispValue applyIntegerOperation(IntUnaryOperator func, LispValue val) {
        var rvInt = func.applyAsInt(val.value >> 1);
        return val.isInteger() ? LispValue.fromInteger(rvInt) : nil;
    }

    public static LispValue applyIntegerOperation(IntBinaryOperator func, LispValue left, LispValue right) {
        if (!left.isInteger() || !right.isInteger()) {
            return nil;
        }
        var rvInt = func.applyAsInt(left.value >> 1, right.value >> 1);
        return LispValue.fromInteger(rvInt);
    }
}
