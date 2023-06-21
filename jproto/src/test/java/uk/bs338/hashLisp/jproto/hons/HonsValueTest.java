package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class HonsValueTest {
    @Test void integerValuesWork() {
        HonsValue val = HonsValue.fromShortInt(17);
        assertTrue(val.isShortInt());
        assertFalse(val.isObjectHash());
        assertFalse(val.isNil());
        assertEquals(17, val.toShortInt());
    }

    @Test void objectHashValuesWork() {
        HonsValue val = HonsValue.fromObjectHash(17);
        assertTrue(val.isObjectHash());
        assertFalse(val.isShortInt());
        assertFalse(val.isNil());
        assertEquals(17, val.toObjectHash());
    }

    @Test void nilIsNil() {
        var nil = HonsValue.nil;
        assertTrue(nil.isNil());
        assertFalse(nil.isShortInt());
        assertFalse(nil.isObjectHash());
    }

    @Test void canApplyUnaryIntegerOperation() {
        HonsValue val = HonsValue.fromShortInt(17);
        IntUnaryOperator operation = n -> -n;
        HonsValue rv = HonsValue.applyShortIntOperation(operation, val);
        assertTrue(rv.isShortInt());
        assertEquals(-17, rv.toShortInt());
    }

    @Test void cannotApplyUnaryIntegerOperation() {
        HonsValue val = HonsValue.fromObjectHash(17);
        IntUnaryOperator operation = n -> -n;
        HonsValue rv = HonsValue.applyShortIntOperation(operation, val);
        assertTrue(rv.isNil());
    }

    @Test void canApplyBinaryIntegerOperation() {
        HonsValue left = HonsValue.fromShortInt(17);
        HonsValue right = HonsValue.fromShortInt(21);
        IntBinaryOperator operation = Integer::sum;
        HonsValue rv = HonsValue.applyShortIntOperation(operation, left, right);
        assertTrue(rv.isShortInt());
        assertEquals(38, rv.toShortInt());
    }

    @Test void cannotApplyBinaryIntegerOperation() {
        HonsValue left = HonsValue.fromObjectHash(17);
        HonsValue right = HonsValue.fromShortInt(21);
        IntBinaryOperator operation = Integer::sum;
        HonsValue rv = HonsValue.applyShortIntOperation(operation, left, right);
        assertTrue(rv.isNil());
    }

    @Test void integerToString() {
        HonsValue val = HonsValue.fromShortInt(17);
        assertEquals("17", val.toString());
    }

    @Test void objectHashToString() {
        HonsValue val = HonsValue.fromObjectHash(17);
        assertEquals("#17", val.toString());
    }

    /*
     * hashCode contract
     */
    @Test void hashCodeHasEqualsConsistencyForInteger() {
        HonsValue val = HonsValue.fromShortInt(17);
        HonsValue similar = HonsValue.fromShortInt(17);
        HonsValue dissimilar = HonsValue.fromShortInt(-17);
        assertEquals(val.hashCode(), similar.hashCode());
        assertNotEquals(val.hashCode(), dissimilar.hashCode());
    }

    @Test void hashCodeHasEqualsConsistencyForObjectHash() {
        HonsValue val = HonsValue.fromObjectHash(17);
        HonsValue similar = HonsValue.fromObjectHash(17);
        HonsValue dissimilar = HonsValue.fromObjectHash(-17);
        assertEquals(val.hashCode(), similar.hashCode());
        assertNotEquals(val.hashCode(), dissimilar.hashCode());
    }

    /* 
     * equals contract
     */
    @Test void equalsHasConsistencyForInteger() {
        HonsValue val = HonsValue.fromShortInt(17);
        HonsValue similar = HonsValue.fromShortInt(17);
        HonsValue dissimilar = HonsValue.fromShortInt(-17);
        assertEquals(val, similar);
        assertNotEquals(val, dissimilar);
    }

    @Test void equalsHasConsistencyForObjectHash() {
        HonsValue val = HonsValue.fromObjectHash(17);
        HonsValue similar = HonsValue.fromObjectHash(17);
        HonsValue dissimilar = HonsValue.fromObjectHash(-17);
        assertEquals(val, similar);
        assertNotEquals(val, dissimilar);
    }

    @Test void equalsHasSymmetryForInteger() {
        HonsValue val = HonsValue.fromShortInt(17);
        HonsValue similar = HonsValue.fromShortInt(17);
        HonsValue dissimilar = HonsValue.fromShortInt(-17);
        assertEquals(val.equals(similar), similar.equals(val));
        assertEquals(val.equals(dissimilar), dissimilar.equals(val));
    }

    @Test void equalsHasSymmetryForObjectHash() {
        HonsValue val = HonsValue.fromObjectHash(17);
        HonsValue similar = HonsValue.fromObjectHash(17);
        HonsValue dissimilar = HonsValue.fromObjectHash(-17);
        assertEquals(val.equals(similar), similar.equals(val));
        assertEquals(val.equals(dissimilar), dissimilar.equals(val));
    }

    @Test void equalsIsReflexiveForInteger() {
        HonsValue val = HonsValue.fromShortInt(17);
        //noinspection EqualsWithItself
        assertEquals(val, val);
    }

    @Test void equalsIsReflexiveForObjectHash() {
        HonsValue val = HonsValue.fromObjectHash(17);
        //noinspection EqualsWithItself
        assertEquals(val, val);
    }
}
