package uk.bs338.hashLisp.jproto.hons;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static uk.bs338.hashLisp.jproto.hons.Strings.*;

class StringsTest {

    @Nested
    class interpretingEscapeChars {
        @Test
        void tab() {
            assertEquals("\t", interpretEscapedChar("t"));
        }

        @Test void backspace() {
            assertEquals("\b", interpretEscapedChar("b"));
        }

        @Test void newline() {
            assertEquals("\n", interpretEscapedChar("n"));
        }

        @Test void carriageReturn() {
            assertEquals("\r", interpretEscapedChar("r"));
        }

        @Test void formfeed() {
            assertEquals("\f", interpretEscapedChar("f"));
        }

        @Test void singleQuote() {
            assertEquals("'", interpretEscapedChar("'"));
        }

        @Test void doubleQuote() {
            assertEquals("\"", interpretEscapedChar("\""));
        }

        @Test void backslash() {
            assertEquals("\\", interpretEscapedChar("\\"));
        }
    }
}