package uk.bs338.hashLisp.jproto.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.reader.CharClassifier.CharClass;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class TokeniserTest {
    String source;
    CharClassifier charClassifier;
    Tokeniser tokeniser;
    
    @BeforeEach void setUp() {
        source = "(add 2 3 (add 4 5))";
        charClassifier = new CharClassifier();
        tokeniser = new Tokeniser(source, charClassifier);
    }

    @Nested
    class InitialState {
        @Test void notAtEnd() {
            assertFalse(tokeniser.isAtEnd());
        }
        
        @Test void firstCharIsCorrectCodepoint() {
            int expected = source.codePointAt(0);
            int actual = tokeniser.getFirstCharAsCodepoint();
            assertEquals(expected, actual);
        }

        @Test void getRemainingReturnsWholeInput() {
            String expected = source;
            String actual = tokeniser.getRemaining();
            assertEquals(expected, actual);
        }
        
        @Test void classifyFirstCharIsOpenParens() {
            EnumSet<CharClass> expected = EnumSet.of(CharClass.OPEN_PARENS);
            assertEquals("(", source.substring(0, 1));
            EnumSet<CharClass> actual = tokeniser.classifyFirstChar();
            assertEquals(expected, actual);
        }
        
        @Test void startPosIsZero() {
            assertEquals(0, tokeniser.getStartPos());
        }
    }
    
    @Nested
    class AdvancesThroughCodepoints {
        @Test void advanceAndCheckStartPos() {
            int expectedStartPos = 0;
            for (; !tokeniser.isAtEnd(); tokeniser.advancePosition()) {
                assertEquals(expectedStartPos, tokeniser.getStartPos());
                expectedStartPos++;
            }
            assertTrue(tokeniser.isAtEnd());
        }
        
        @Test void advanceAndCheckRemaining() {
            int expectedStartPos = 0;
            for (; !tokeniser.isAtEnd(); tokeniser.advancePosition()) {
                String expectedRemaining = source.substring(expectedStartPos);
                assertEquals(expectedRemaining, tokeniser.getRemaining());
                expectedStartPos++;
            }
            assertTrue(tokeniser.isAtEnd());
        }
        
        @Test void advanceAndFetchCodepoint() {
            var expectedCodepoints = source.codePoints().iterator();
            for (; !tokeniser.isAtEnd(); tokeniser.advancePosition()) {
                int expectedCodepoint = expectedCodepoints.nextInt();
                assertEquals(expectedCodepoint, tokeniser.getFirstCharAsCodepoint());
            }
            assertTrue(tokeniser.isAtEnd());
        }
        
        @Test void advanceAndClassifyCodepoints() {
            /* we just check the EnumSets are all non-empty */
            for (; !tokeniser.isAtEnd(); tokeniser.advancePosition()) {
                EnumSet<CharClass> actual = tokeniser.classifyFirstChar();
                assertFalse(actual.isEmpty());
            }
            assertTrue(tokeniser.isAtEnd());
        }
    }
    
    @Nested
    class EatWhitespace {
        @BeforeEach void setUp() {
            tokeniser = new Tokeniser("   a\tb\r\n", charClassifier);
        }
        
        @Test void atBeginning() {
            tokeniser.eatWhitespace();
            assertEquals(3, tokeniser.getStartPos());
            assertFalse(tokeniser.classifyFirstChar().contains(CharClass.WHITESPACE));
        }
        
        @Test void inMiddle() {
            while (tokeniser.getStartPos() < 4)
                tokeniser.advancePosition();
            tokeniser.eatWhitespace();
            assertEquals(5, tokeniser.getStartPos());
            assertFalse(tokeniser.classifyFirstChar().contains(CharClass.WHITESPACE));
        }

        @Test void atEnd() {
            while (tokeniser.getStartPos() < 6)
                tokeniser.advancePosition();
            tokeniser.eatWhitespace();
            assertEquals(8, tokeniser.getStartPos());
            assertTrue(tokeniser.isAtEnd());
        }
        
        @Test void handlesTheEnd() {
            while (!tokeniser.isAtEnd())
                tokeniser.advancePosition();
            int expectedStartPos = tokeniser.getStartPos();
            tokeniser.eatWhitespace();
            assertEquals(expectedStartPos, tokeniser.getStartPos());
            assertTrue(tokeniser.isAtEnd());
        }
        
        @Test void doesntEatToken() {
            while (tokeniser.getStartPos() < 3)
                tokeniser.advancePosition();
            tokeniser.eatWhitespace();
            assertEquals(3, tokeniser.getStartPos());
        }
    }
    
    @Nested
    class OneTokenOnly {
        @Test void intToken() {
            source = "123";
            tokeniser = new Tokeniser(source, charClassifier);
            Token token = tokeniser.next();
            assertFalse(tokeniser.hasNext());
            assertEquals(source.length(), tokeniser.getStartPos());
            assertEquals("123", token.getToken());
            assertEquals("0-3", token.getPositionAsString());
        }
        
        @Test void symbolToken() {
            source = "abc";
            tokeniser = new Tokeniser(source, charClassifier);
            Token token = tokeniser.next();
            assertFalse(tokeniser.hasNext());
            assertEquals(source.length(), tokeniser.getStartPos());
            assertEquals("abc", token.getToken());
            assertEquals("0-3", token.getPositionAsString());
        }

        @Test void openParens() {
            source = "(";
            tokeniser = new Tokeniser(source, charClassifier);
            Token token = tokeniser.next();
            assertFalse(tokeniser.hasNext());
            assertEquals(source.length(), tokeniser.getStartPos());
            assertEquals("(", token.getToken());
            assertEquals("0-1", token.getPositionAsString());
        }

        @Test void closeParens() {
            source = ")";
            tokeniser = new Tokeniser(source, charClassifier);
            Token token = tokeniser.next();
            assertFalse(tokeniser.hasNext());
            assertEquals(source.length(), tokeniser.getStartPos());
            assertEquals(")", token.getToken());
            assertEquals("0-1", token.getPositionAsString());
        }
        
        @Test void hash() {
            source = "#";
            tokeniser = new Tokeniser(source, charClassifier);
            Token token = tokeniser.next();
            assertFalse(tokeniser.hasNext());
            assertEquals(source.length(), tokeniser.getStartPos());
            assertEquals("#", token.getToken());
            assertEquals("0-1", token.getPositionAsString());
        }

        @Test void colon() {
            source = ":";
            tokeniser = new Tokeniser(source, charClassifier);
            Token token = tokeniser.next();
            assertFalse(tokeniser.hasNext());
            assertEquals(source.length(), tokeniser.getStartPos());
            assertEquals(":", token.getToken());
            assertEquals("0-1", token.getPositionAsString());
        }
    }
}