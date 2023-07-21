package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.hons.Strings.*;

class StringsTest {
    
    @Nested
    class QuotingString {
        @Test void tab() {
            assertEquals("\"\\t\"", quoteString("\t"));
        }

        @Test void backspace() {
            assertEquals("\"\\b\"", quoteString("\b"));
        }

        @Test void newline() {
            assertEquals("\"\\n\"", quoteString("\n"));
        }

        @Test void carriageReturn() {
            assertEquals("\"\\r\"", quoteString("\r"));
        }

        @Test void formfeed() {
            assertEquals("\"\\f\"", quoteString("\f"));
        }

        @Test void singleQuote() {
            assertEquals("\"\\'\"", quoteString("'"));
        }

        @Test void doubleQuote() {
            assertEquals("\"\\\"\"", quoteString("\""));
        }

        @Test void backslash() {
            assertEquals("\"\\\\\"", quoteString("\\"));
        }

        @Test void unicodeEmojis() {
            assertEquals("\"\\u{1f1ec}\\u{1f1e7}\"", quoteString("🇬🇧"));
        }
    }

    @Nested
    class UnescapingString {
        @Test
        void tab() {
            assertEquals("\t", unescapeString("\\t"));
        }

        @Test void backspace() {
            assertEquals("\b", unescapeString("\\b"));
        }

        @Test void newline() {
            assertEquals("\n", unescapeString("\\n"));
        }

        @Test void carriageReturn() {
            assertEquals("\r", unescapeString("\\r"));
        }

        @Test void formfeed() {
            assertEquals("\f", unescapeString("\\f"));
        }

        @Test void singleQuote() {
            assertEquals("'", unescapeString("\\'"));
        }

        @Test void doubleQuote() {
            assertEquals("\"", unescapeString("\\\""));
        }

        @Test void backslash() {
            assertEquals("\\", unescapeString("\\\\"));
        }
        
        @Test void unicodeEmojis() {
            assertEquals("🇬🇧", unescapeString("\\u{1F1EC}\\u{1f1e7}"));
        }
    }
}