package uk.bs338.hashLisp.jproto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsPairTest {
    record MockValue(int value) implements IValue {

        @Override
        public boolean isNil() {
            return false;
        }
    
        @Override
        public boolean isSymbolTag() {
            return false;
        }
    
        @Override
        public boolean isSmallInt() {
            return true;
        }
    
        @Override
        public boolean isConsRef() {
            return false;
        }
    
        @Override
        public int toSmallInt() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("MockValue{%d}", toSmallInt());
        }
    }
    
    MockValue fstMock;
    MockValue sndMock;
    ConsPair<MockValue> consPair; 
        
    @BeforeEach void setUp() {
        fstMock = new MockValue(123);
        sndMock = new MockValue(456);
        consPair = new ConsPair<>(fstMock, sndMock);
    }
    
    @Test void hasFst() {
        assertEquals(fstMock, consPair.fst());
    }
    
    @Test void hasSnd() {
        assertEquals(sndMock, consPair.snd());
    }
    
    @Test void equalsIsByValue() {
        ConsPair<MockValue> secondPair = new ConsPair<>(fstMock, sndMock);
        assertEquals(consPair, secondPair);
    }

    @Test void hashCodeIsByValue() {
        ConsPair<MockValue> secondPair = new ConsPair<>(fstMock, sndMock);
        assertEquals(consPair.hashCode(), secondPair.hashCode());
    }
    
    @Test void hasUsefulToString() {
        String expected = "ConsPair[fst=%s, snd=%s]".formatted(fstMock, sndMock);
        assertEquals(expected, consPair.toString());
    }

    @Test void fmapAppliesFunction() {
        ConsPair<MockValue> retval = consPair.fmap((mock) -> new MockValue(mock.toSmallInt()*2));
        assertEquals(123*2, retval.fst().toSmallInt());
        assertEquals(456*2, retval.snd().toSmallInt());
    }
}