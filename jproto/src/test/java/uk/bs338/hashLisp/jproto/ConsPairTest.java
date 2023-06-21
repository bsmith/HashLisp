package uk.bs338.hashLisp.jproto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsPairTest {
    class MockValue implements IValue {
        int value;
        
        public MockValue(int value) {
            this.value = value;
        }
        
        @Override public boolean isNil() {
            return false;
        }

        @Override public boolean isSymbolTag() {
            return false;
        }

        @Override public boolean isShortInt() {
            return true;
        }

        @Override public boolean isConsRef() {
            return false;
        }

        @Override public int toShortInt() {
            return 123;
        }
        
        @Override public String toString() {
            return String.format("MockValue{%d}", toShortInt());
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
}