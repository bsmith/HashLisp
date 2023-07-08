package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class HonsValueTest {
    @Test void getAllSpecials() {
        List<HonsValue> allSpecials = new ArrayList<>();
        HonsValue.getAllSpecials().forEach(allSpecials::add);
        assertEquals(2, allSpecials.size());
        assertEquals(HonsValue.nil, allSpecials.get(0));
        assertEquals(HonsValue.symbolTag, allSpecials.get(1));
    }
    
    @Test void integerValuesWork() {
        HonsValue val = HonsValue.fromSmallInt(17);
        assertTrue(val.isSmallInt());
        assertFalse(val.isObjectHash());
        assertFalse(val.isNil());
        assertEquals(17, val.toSmallInt());
    }

    @Test void objectHashValuesWork() {
        HonsValue val = HonsValue.fromObjectHash(17);
        assertTrue(val.isObjectHash());
        assertFalse(val.isSmallInt());
        assertFalse(val.isNil());
        assertEquals(17, val.toObjectHash());
    }

    @Test void nilIsNil() {
        var nil = HonsValue.nil;
        assertTrue(nil.isNil());
        assertFalse(nil.isSmallInt());
        assertFalse(nil.isObjectHash());
    }

    @Test void canApplyUnaryIntegerOperation() {
        HonsValue val = HonsValue.fromSmallInt(17);
        IntUnaryOperator operation = n -> -n;
        HonsValue rv = HonsValue.applySmallIntOperation(operation, val);
        assertTrue(rv.isSmallInt());
        assertEquals(-17, rv.toSmallInt());
    }

    @Test void cannotApplyUnaryIntegerOperation() {
        HonsValue val = HonsValue.fromObjectHash(17);
        IntUnaryOperator operation = n -> -n;
        HonsValue rv = HonsValue.applySmallIntOperation(operation, val);
        assertTrue(rv.isNil());
    }

    @Test void canApplyBinaryIntegerOperation() {
        HonsValue left = HonsValue.fromSmallInt(17);
        HonsValue right = HonsValue.fromSmallInt(21);
        IntBinaryOperator operation = Integer::sum;
        HonsValue rv = HonsValue.applySmallIntOperation(operation, left, right);
        assertTrue(rv.isSmallInt());
        assertEquals(38, rv.toSmallInt());
    }

    @Test void cannotApplyBinaryIntegerOperation() {
        HonsValue left = HonsValue.fromObjectHash(17);
        HonsValue right = HonsValue.fromSmallInt(21);
        IntBinaryOperator operation = Integer::sum;
        HonsValue rv = HonsValue.applySmallIntOperation(operation, left, right);
        assertTrue(rv.isNil());
    }
    
    @Test void getSpecialNameOfSpecial() {
        assertEquals("nil", HonsValue.nil.getSpecialName());
    }
    
    @Test void getSpecialNameReturnsNullWithNonSpecial() {
        assertNull(HonsValue.fromSmallInt(123).getSpecialName());
    }
    
    @Test void symbolTagIsSymbolTag() {
        assertTrue(HonsValue.symbolTag.isSymbolTag());
    }
    
    @Test void nonTagIsNotSymbolTag() {
        assertFalse(HonsValue.fromSmallInt(123).isSymbolTag());
    }
    
    @Test void exceptionThrownByToSmallIntThatIsNotSmallInt() {
        assertThrows(NoSuchElementException.class, () -> {
            HonsValue.fromObjectHash(123).toSmallInt();
        });
    }

    @Test void exceptionThrownByToObjectHashThatIsNotSmallInt() {
        assertThrows(NoSuchElementException.class, () -> {
            HonsValue.fromSmallInt(123).toObjectHash();
        });
    }
    
    @Nested class isConsRef {
        @Test void trueForObjectHash() {
            assertTrue(HonsValue.fromObjectHash(123).isConsRef());
        }
        
        @Test void falseForSmallInt() {
            assertFalse(HonsValue.fromSmallInt(123).isConsRef());
        }
        
        @Test void falseForNil() {
            assertFalse(HonsValue.nil.isConsRef());
        }
        
        @Test void falseForSymbolTag() {
            assertFalse(HonsValue.symbolTag.isConsRef());
        }
    }

    @Nested
    class toString {
        @Test void nilToString() {
            assertEquals("nil", HonsValue.nil.toString());
        }
        
        @Test void symbolTagToString() {
            assertEquals("#1:symbol", HonsValue.symbolTag.toString());
        }
        
        @Test void integerToString() {
            HonsValue val = HonsValue.fromSmallInt(17);
            assertEquals("17", val.toString());
        }

        @Test void objectHashToString() {
            HonsValue val = HonsValue.fromObjectHash(17);
            assertEquals("#17", val.toString());
        }
    }

    /*
     * hashCode contract
     */
    @Test void hashCodeHasEqualsConsistencyForInteger() {
        HonsValue val = HonsValue.fromSmallInt(17);
        HonsValue similar = HonsValue.fromSmallInt(17);
        HonsValue dissimilar = HonsValue.fromSmallInt(-17);
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
        HonsValue val = HonsValue.fromSmallInt(17);
        HonsValue similar = HonsValue.fromSmallInt(17);
        HonsValue dissimilar = HonsValue.fromSmallInt(-17);
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
        HonsValue val = HonsValue.fromSmallInt(17);
        HonsValue similar = HonsValue.fromSmallInt(17);
        HonsValue dissimilar = HonsValue.fromSmallInt(-17);
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
        HonsValue val = HonsValue.fromSmallInt(17);
        //noinspection EqualsWithItself
        assertEquals(val, val);
    }

    @Test void equalsIsReflexiveForObjectHash() {
        HonsValue val = HonsValue.fromObjectHash(17);
        //noinspection EqualsWithItself
        assertEquals(val, val);
    }
}
