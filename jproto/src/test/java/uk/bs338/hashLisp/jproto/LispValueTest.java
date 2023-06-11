package uk.bs338.hashLisp.jproto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

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
        IntUnaryOperator operation = n -> -n;
        LispValue rv = LispValue.applyIntegerOperation(operation, val);
        assertTrue(rv.isInteger());
        assertEquals(Optional.of(-17), rv.toInteger());
    }

    @Test void cannotApplyUnaryIntegerOperation() {
        LispValue val = LispValue.fromObjectHash(17);
        IntUnaryOperator operation = n -> -n;
        LispValue rv = LispValue.applyIntegerOperation(operation, val);
        assertTrue(rv.isNil());
    }

    @Test void canApplyBinaryIntegerOperation() {
        LispValue left = LispValue.fromInteger(17);
        LispValue right = LispValue.fromInteger(21);
        IntBinaryOperator operation = (a, b) -> a + b;
        LispValue rv = LispValue.applyIntegerOperation(operation, left, right);
        assertTrue(rv.isInteger());
        assertEquals(Optional.of(38), rv.toInteger());
    }

    @Test void cannotApplyBinaryIntegerOperation() {
        LispValue left = LispValue.fromObjectHash(17);
        LispValue right = LispValue.fromInteger(21);
        IntBinaryOperator operation = (a, b) -> a + b;
        LispValue rv = LispValue.applyIntegerOperation(operation, left, right);
        assertTrue(rv.isNil());
    }
}
