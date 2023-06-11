package uk.bs338.hashLisp.jproto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LispValueTest {
    @Test void integerValuesWork() {
        LispValue val = LispValue.fromInteger(17);
        assertTrue(val.isInteger());
        assertEquals(Optional.of(17), val.toInteger());
    }

    @Test void objectHashValuesWork() {
        LispValue val = LispValue.fromObjectHash(17);
        assertTrue(val.isObjectHash());
        assertEquals(Optional.of(17), val.toObjectHash());
    }

    @Test void canApplyUnaryIntegerOperation() {
        LispValue val = LispValue.fromInteger(17);
        Function<Integer, Integer> operation = n -> -n;
        Optional<LispValue> rv = LispValue.applyIntegerOperation(operation, val);
        assertTrue(rv.isPresent() && rv.get().isInteger());
        assertEquals(Optional.of(-17), rv.get().toInteger());
    }

    @Test void cannotApplyUnaryIntegerOperation() {
        LispValue val = LispValue.fromObjectHash(17);
        Function<Integer, Integer> operation = n -> -n;
        Optional<LispValue> rv = LispValue.applyIntegerOperation(operation, val);
        assertTrue(rv.isEmpty());
    }

    @Test void cannotApplyBinaryIntegerOperation() {
        LispValue left = LispValue.fromObjectHash(17);
        LispValue right = LispValue.fromInteger(21);
        BiFunction<Integer, Integer, Integer> operation = (a, b) -> a + b;
        Optional<LispValue> rv = LispValue.applyIntegerOperation(operation, left, right);
        assertTrue(rv.isEmpty());
    }
}
