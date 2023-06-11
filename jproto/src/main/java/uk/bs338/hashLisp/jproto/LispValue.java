package uk.bs338.hashLisp.jproto;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LispValue {
    /* 31-bit signed integers have the range [-(2**30-1)-1, 2**30-1] */
    private final static int int_min = -1073741823 - 1;
    private final static int int_max = 1073741823;

    private int value;

    private LispValue(int value) {
        this.value = value;
    }

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

    /* XXX Are these two operations the best?  Most javaish? */
    /* XXX Should these return nil? */
    public static Optional<LispValue> applyIntegerOperation(Function<Integer, Integer> func,
            LispValue val) {
        var valOpt = val.toInteger();
        if (valOpt.isEmpty()) {
            return Optional.empty();
        }
        var rvInt = func.apply(valOpt.get());
        return Optional.of(LispValue.fromInteger(rvInt));
    }

    public static Optional<LispValue> applyIntegerOperation(BiFunction<Integer, Integer, Integer> func,
            LispValue left, LispValue right) {
        var leftOpt = left.toInteger();
        var rightOpt = right.toInteger();
        if (leftOpt.isEmpty() || rightOpt.isEmpty()) {
            return Optional.empty();
        }
        var rvInt = func.apply(leftOpt.get(), rightOpt.get());
        return Optional.of(LispValue.fromInteger(rvInt));
    }
}
