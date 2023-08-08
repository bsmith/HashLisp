package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static org.junit.jupiter.api.Assertions.*;

import static uk.bs338.hashLisp.jproto.Utilities.*;

class ReaderTest {
    HonsMachine machine;
    CharClassifier charClassifier;
    Reader reader;

    @BeforeEach
    void setUp() {
        /* reuse heap */
        if (machine == null)
            machine = new HonsMachine();
        if (charClassifier == null)
            charClassifier = new CharClassifier();
        var tokeniserFactory = Tokeniser.getFactory(charClassifier); 
        reader = new Reader(machine, tokeniserFactory);
    }
    
    @AfterEach void validateHeap() {
        machine.getHeap().validateHeap();
    }
    
    @Nested
    class SimpleValues {
        @Test void smallInt() {
            var input = "123";
            var expected = ReadResult.successfulRead("", HonsValue.fromSmallInt(123));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }

        @Test void symbol() {
            var input = "abc";
            var expected = ReadResult.successfulRead("", machine.makeSymbol("abc"));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void nilAsHash() {
            var input = "#0";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
    }
    
    @Nested
    class ConsValues {
        @Test void emptyListIsNil() {
            var input = "()";
            var expected = ReadResult.successfulRead("", HonsValue.nil);
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void pairOfInts() {
            var input = "(123 . 345)";
            var expected = ReadResult.successfulRead("",
                machine.cons(
                    HonsValue.fromSmallInt(123),
                    HonsValue.fromSmallInt(345)
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void oneElementList() {
            var input = "(123)";
            var expected = ReadResult.successfulRead("",
                machine.cons(
                    HonsValue.fromSmallInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void twoElementList() {
            var input = "(123 456)";
            var expected = ReadResult.successfulRead("",
                machine.cons(
                    HonsValue.fromSmallInt(123),
                    machine.cons(
                        HonsValue.fromSmallInt(456),
                        HonsValue.nil
                    )
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }

        @Test void oneElementListWrittenAsPair() {
            var input = "(123 . ())";
            var expected = ReadResult.successfulRead("",
                machine.cons(
                    HonsValue.fromSmallInt(123),
                    HonsValue.nil
                ));
            var actual = reader.read(input);
            assertEquals(expected, actual);
        }
        
        @Test void dotCannotAppearAtStartOfList(@NotNull TestReporter testReporter) {
            var input = "( . 123)";
            var actual = reader.read(input);
            assertTrue(actual.isFailure());
            testReporter.publishEntry(actual.getFailureMessage());
            assertEquals(input, actual.getRemaining());
        }
    }
    
    @Nested
    class SmallIntValues {
        void assertSmallIntRead(int expected, @NotNull String input) {
            var actual = reader.read(input);
            assertTrue(actual.isSuccess());
            assertEquals(HonsValue.fromSmallInt(expected), actual.getValue());
            /* check this too in case of range bugs in fromSmallInt */
            assertEquals(expected, actual.getValue().toSmallInt());
        }
        
        @Test void zero() {
            assertSmallIntRead(0, "0");
        }
        
        @Test void one() {
            assertSmallIntRead(1, "1");
        }
        
        @Test void minusOne() {
            assertSmallIntRead(-1, "-1");
        }
        
        @Test void minimum() {
            assertSmallIntRead(HonsValue.SMALLINT_MIN, String.valueOf(HonsValue.SMALLINT_MIN));
        }

        @Test void maximum() {
            assertSmallIntRead(HonsValue.SMALLINT_MAX, String.valueOf(HonsValue.SMALLINT_MAX));
        }
    }
    
    @Nested
    class StringValues {
        void assertStringRead(@NotNull String expectedStr, @NotNull String input) {
            var actual = reader.read(input);
            var expected = machine.cons(machine.makeSymbol("*string"), stringAsList(machine, expectedStr));
            assertTrue(actual.isSuccess());
            /* test this in two ways, as it gives nicer failure reporting! */
            assertEquals(machine.valueToString(expected), machine.valueToString(actual.getValue()));
            assertEquals(expected, actual.getValue());
            assertEquals("", actual.getRemaining());
        }
        
        @Test void emptyString() {
            assertStringRead("", "\"\"");
        }
        
        @Test void stringWithSpaces() {
            assertStringRead("   ", "\"   \"");
        }
        
        @Test void multilineString() {
            assertStringRead("abc\nmno\nxyz", "\"abc\nmno\nxyz\"");
        }
        
        @Test void backquoteString() {
            /* Java backslash sequences are \t, \b, \n, \r, \f, \', \", \\ */
            assertStringRead("abc\"xyz", "\"abc\\\"xyz\"");
            assertStringRead("\t\b\n\r\f'\"", "\"\\t\\b\\n\\r\\f\\'\\\"\"");
        }
        
        @Test void emojiString() {
            /* "🇬🇧" == "\\u{1F1EC}\\u{1f1e7}" */
            /* Build the string explicitly using codepoints not UTF-16 */
            var expectedStr = new String(new int[]{0x1f1ec, 0x1f1e7}, 0, 2);
            assertStringRead(expectedStr, "\"\\u{1F1EC}\\u{1f1e7}\"");
        }
    }
    
    @Test void comments() {
        var input = """
            (1 ;comment
            .;comment
            ;comment
            2) ;comment
            """;
        var expected = machine.cons(HonsValue.fromSmallInt(1), HonsValue.fromSmallInt(2));
        ReadResult<HonsValue> rv = reader.read(input);
        assertTrue(rv.isSuccess());
        assertEquals(expected, rv.getValue());
        assertEquals("", rv.getRemaining());
    }
    
    @Test
    void read(@NotNull TestReporter testReporter) {
        var input = "(add (add 1 2) 3 4)";
        var addSym = machine.makeSymbol("add");
        var expected = makeList(machine,
            addSym,
            makeList(machine,
                addSym,
                HonsValue.fromSmallInt(1),
                HonsValue.fromSmallInt(2)
            ),
            HonsValue.fromSmallInt(3),
            HonsValue.fromSmallInt(4)
            );
        ReadResult<HonsValue> rv = reader.read(input);
        assertTrue(rv.isSuccess());
        assertEquals(expected, rv.getValue());
        testReporter.publishEntry(rv.toString());
        assertEquals("", rv.getRemaining());
    }

}