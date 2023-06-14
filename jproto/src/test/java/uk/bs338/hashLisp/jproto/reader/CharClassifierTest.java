package uk.bs338.hashLisp.jproto.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.reader.CharClassifier.CharClass.*;

class CharClassifierTest {
    CharClassifier classifier;
    
    @BeforeEach void setUp() {
        classifier = new CharClassifier();
    }

    @Test void digits() {
        String digits = "1234567890";
        for (var digit : (Iterable<String>) () -> digits.chars().mapToObj(ch -> String.valueOf((char) ch)).iterator()) {
            var classes = classifier.classifyChar(digit);
            assertEquals(EnumSet.of(DIGIT_CHAR, SYMBOL_CHAR), classes);
        }
    }
    
    @Test void parens() {
        assertEquals(EnumSet.of(OPEN_PARENS), classifier.classifyChar("("));
        assertEquals(EnumSet.of(CLOSE_PARENS), classifier.classifyChar(")"));
    }
    
    @Test void alphas() {
        for (char ch = 'a'; ch < 'z'; ch++) {
            String lower = String.valueOf(ch);
            String upper = lower.toUpperCase(Locale.ROOT);
            assertEquals(EnumSet.of(SYMBOL_CHAR), classifier.classifyChar(lower));
            assertEquals(EnumSet.of(SYMBOL_CHAR), classifier.classifyChar(upper));
        }
    }
    
    @Test void specialSymbols() {
        assertEquals(EnumSet.of(SYMBOL_CHAR), classifier.classifyChar("-"));
        assertEquals(EnumSet.of(SYMBOL_CHAR), classifier.classifyChar("?"));
        assertEquals(EnumSet.of(SYMBOL_CHAR), classifier.classifyChar("!"));
        assertEquals(EnumSet.of(SYMBOL_CHAR), classifier.classifyChar("$"));
        assertEquals(EnumSet.of(SYMBOL_CHAR), classifier.classifyChar("@"));
    }
    
    @Test void hash() {
        assertEquals(EnumSet.of(HASH_CHAR), classifier.classifyChar("#"));
    }
    
    @Test void whitespace() {
        assertEquals(EnumSet.of(WHITESPACE), classifier.classifyChar(" "));
        assertEquals(EnumSet.of(WHITESPACE), classifier.classifyChar("\n"));
        assertEquals(EnumSet.of(WHITESPACE), classifier.classifyChar("\r"));
        assertEquals(EnumSet.of(WHITESPACE), classifier.classifyChar("\t"));
    }
    
    @Test void other() {
        /* just one random example */
        assertTrue(classifier.classifyChar("%").isEmpty());
    }
    
    /* make sure no exceptions or nulls happen over lots of reasonable chars */
    @Test void checkAllAscii() {
        for (char ch = 0; ch < 128; ch++) {
            var classes = classifier.classifyChar(String.valueOf(ch));
            assertNotNull(classes);
        }
    }
}