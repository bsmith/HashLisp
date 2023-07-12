/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package uk.bs338.hashLisp.jproto;

import org.junit.jupiter.api.Test;

import com.beust.jcommander.ParameterException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

class AppTest {
    App app;

    @BeforeEach void setUp() {
        app = new App();
    }

    @Test void appHasAGreeting() {
        assertNotNull(app.getGreeting(), "app should have a greeting");
    }

    @Nested
    class CommandLineOptions {
        @Test void debugFlag() {
            assertFalse(app.debug);
            assertTrue(app.parseArgs(new String[]{"--debug"}));
            assertTrue(app.debug);
        }

        @Test void dumpHeapFlag() {
            assertFalse(app.dumpHeap);
            assertTrue(app.parseArgs(new String[]{"-dumpHeap"}));
            assertTrue(app.dumpHeap);
        }

        @Test void demoMode() {
            assertFalse(app.demoMode);
            assertTrue(app.parseArgs(new String[]{"--demo"}));
            assertTrue(app.demoMode);
        }

        @Test void readMode() {
            assertFalse(app.readMode);
            assertTrue(app.parseArgs(new String[]{"--read"}));
            assertTrue(app.readMode);
        }

        @Test void evalMode() {
            assertFalse(app.evalMode);
            assertTrue(app.parseArgs(new String[]{"--eval"}));
            assertTrue(app.evalMode);
        }

        @Test void readAndEvalModesAreMutuallyExclusive() {
            assertFalse(app.parseArgs(new String[]{"--read", "--eval"}));
        }

        @Test void fileTakesAParameter() {
            assertNull(app.sourceFilename);
            assertTrue(app.parseArgs(new String[]{"--file", "filename.hl"}));
            assertEquals("filename.hl", app.sourceFilename);
        }

        @Test void exprTakesAParameter() {
            assertNull(app.sourceFilename);
            assertTrue(app.parseArgs(new String[]{"--expr", "(example)"}));
            assertEquals("(example)", app.sourceExpr);
        }

        @Test void fileAndExprAreMutuallyExclusive() {
            assertFalse(app.parseArgs(new String[]{"--file", "example.hl", "--expr", "(example)"}));
        }

        @Test void filenameIsTakenFromFirstNonOption() {
            assertTrue(app.parseArgs(new String[]{"--eval", "filename.hl"}));
            assertEquals("filename.hl", app.sourceFilename);
        }

        @Test void whenExprSuppliedDontTakeFilenameFromFirstNonOption() {
            assertTrue(app.parseArgs(new String[]{"--expr", "(example)", "not_filename"}));
            assertEquals("(example)", app.sourceExpr);
            assertEquals(List.of("not_filename"), app.userArguments);
        }

        @Test void onlyFilenameIsAllowedAsNonOptionBeforeDoubleDash() {
            assertFalse(app.parseArgs(new String[]{"--eval", "filename.hl", "notAllowed", "--"}));
        }

        @Test void everythingAfterDoubleDashIsAUserArgument() {
            assertTrue(app.parseArgs(new String[]{"filename.hl", "--", "arg", "-r", "-E", "-e"}));
            assertEquals("filename.hl", app.sourceFilename);
            assertEquals(List.of("arg", "-r", "-E", "-e"), app.userArguments);
        }
    }
}
