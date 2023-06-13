package uk.bs338.hashLisp.jproto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class LispValueTest {
    @Test void integerValuesWork() {
        LispValue val = LispValue.fromShortInt(17);
        assertTrue(val.isShortInt());
        assertFalse(val.isObjectHash());
        assertFalse(val.isNil());
        assertEquals(17, val.toShortInt());
    }

    @Test void objectHashValuesWork() {
        LispValue val = LispValue.fromObjectHash(17);
        assertTrue(val.isObjectHash());
        assertFalse(val.isShortInt());
        assertFalse(val.isNil());
        assertEquals(17, val.toObjectHash());
    }

    @Test void nilIsNil() {
        var nil = LispValue.nil;
        assertTrue(nil.isNil());
        assertFalse(nil.isShortInt());
        assertFalse(nil.isObjectHash());
    }

    @Test void canApplyUnaryIntegerOperation() {
        LispValue val = LispValue.fromShortInt(17);
        IntUnaryOperator operation = n -> -n;
        LispValue rv = LispValue.applyShortIntOperation(operation, val);
        assertTrue(rv.isShortInt());
        assertEquals(-17, rv.toShortInt());
    }

    @Test void cannotApplyUnaryIntegerOperation() {
        LispValue val = LispValue.fromObjectHash(17);
        IntUnaryOperator operation = n -> -n;
        LispValue rv = LispValue.applyShortIntOperation(operation, val);
        assertTrue(rv.isNil());
    }

    @Test void canApplyBinaryIntegerOperation() {
        LispValue left = LispValue.fromShortInt(17);
        LispValue right = LispValue.fromShortInt(21);
        IntBinaryOperator operation = Integer::sum;
        LispValue rv = LispValue.applyShortIntOperation(operation, left, right);
        assertTrue(rv.isShortInt());
        assertEquals(38, rv.toShortInt());
    }

    @Test void cannotApplyBinaryIntegerOperation() {
        LispValue left = LispValue.fromObjectHash(17);
        LispValue right = LispValue.fromShortInt(21);
        IntBinaryOperator operation = Integer::sum;
        LispValue rv = LispValue.applyShortIntOperation(operation, left, right);
        assertTrue(rv.isNil());
    }

    @Test void integerToString() {
        LispValue val = LispValue.fromShortInt(17);
        assertEquals("17", val.toString());
    }

    @Test void objectHashToString() {
        LispValue val = LispValue.fromObjectHash(17);
        assertEquals("#17", val.toString());
    }

    /*
     * hashCode contract
     */
    @Test void hashCodeHasEqualsConsistencyForInteger() {
        LispValue val = LispValue.fromShortInt(17);
        LispValue similar = LispValue.fromShortInt(17);
        LispValue dissimilar = LispValue.fromShortInt(-17);
        assertEquals(val.hashCode(), similar.hashCode());
        assertNotEquals(val.hashCode(), dissimilar.hashCode());
    }

    @Test void hashCodeHasEqualsConsistencyForObjectHash() {
        LispValue val = LispValue.fromObjectHash(17);
        LispValue similar = LispValue.fromObjectHash(17);
        LispValue dissimilar = LispValue.fromObjectHash(-17);
        assertEquals(val.hashCode(), similar.hashCode());
        assertNotEquals(val.hashCode(), dissimilar.hashCode());
    }

    /* 
     * equals contract
     */
    @Test void equalsHasConsistencyForInteger() {
        LispValue val = LispValue.fromShortInt(17);
        LispValue similar = LispValue.fromShortInt(17);
        LispValue dissimilar = LispValue.fromShortInt(-17);
        assertEquals(val, similar);
        assertNotEquals(val, dissimilar);
    }

    @Test void equalsHasConsistencyForObjectHash() {
        LispValue val = LispValue.fromObjectHash(17);
        LispValue similar = LispValue.fromObjectHash(17);
        LispValue dissimilar = LispValue.fromObjectHash(-17);
        assertEquals(val, similar);
        assertNotEquals(val, dissimilar);
    }

    @Test void equalsHasSymmetryForInteger() {
        LispValue val = LispValue.fromShortInt(17);
        LispValue similar = LispValue.fromShortInt(17);
        LispValue dissimilar = LispValue.fromShortInt(-17);
        assertEquals(val.equals(similar), similar.equals(val));
        assertEquals(val.equals(dissimilar), dissimilar.equals(val));
    }

    @Test void equalsHasSymmetryForObjectHash() {
        LispValue val = LispValue.fromObjectHash(17);
        LispValue similar = LispValue.fromObjectHash(17);
        LispValue dissimilar = LispValue.fromObjectHash(-17);
        assertEquals(val.equals(similar), similar.equals(val));
        assertEquals(val.equals(dissimilar), dissimilar.equals(val));
    }

    @Test void equalsIsReflexiveForInteger() {
        LispValue val = LispValue.fromShortInt(17);
        assertEquals(val, val);
    }

    @Test void equalsIsReflexiveForObjectHash() {
        LispValue val = LispValue.fromObjectHash(17);
        assertEquals(val, val);
    }
}
