package uk.bs338.hashLisp.jproto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.driver.Driver;
import uk.bs338.hashLisp.jproto.driver.PrintOnlyReader;
import uk.bs338.hashLisp.jproto.eval.LazyEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsCell;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import static uk.bs338.hashLisp.jproto.Utilities.*;

@SuppressWarnings("CanBeFinal")
public class App {
    private final @NotNull HonsHeap heap;

    @Parameter(
        names = {"--help"},
        help = true
    )
    public boolean showHelp = false;
    @Parameter(
        names = {"--debug"},
        description = "Enable debug flag"
    )
    public boolean debug = false;
    @Parameter(
        names = {"--dump-heap", "-dumpHeap"},
        description = "Dump the heap at the end of the run"
    )
    public boolean dumpHeap = false;
    @Parameter(
        names = {"--demo"},
        description = "Enable a short demonstration"
    )
    public boolean demoMode = false;
    @Parameter(
        names = {"-r", "--read"},
        description = "Read then print the program without evaluation"
    )
    public boolean readMode = false;
    @Parameter(
        names = {"-E", "--eval"},
        description = "Read the program and then evaluate the program"
    )
    public boolean evalMode = false;
    @Parameter(
        names = {"-f", "--file"},
        description = "The file to use as the source of the program"
    )
    public @Nullable String sourceFilename = null;
    @Parameter(
        names = {"-e", "--expr"},
        description = "Read the given expression as the source of the program"
    )
    public @Nullable String sourceExpr = null;
    @Parameter
    public @Nullable List<String> userArguments = null;
    boolean argsParsed = false;

    public App() {
        heap = new HonsHeap();
    }

    @SuppressWarnings("SameReturnValue")
    public @NotNull String getGreeting() {
        return "jproto --- prototype for HashLisp";
    }

    public @NotNull IReader<HonsValue> getReader() {
        var reader = new Reader(heap, Tokeniser.getFactory(new CharClassifier()));
        if (readMode)
            return new PrintOnlyReader<>(heap, reader);
        else
            return reader;
    }
    
    public @NotNull IEvaluator<HonsValue> getEvaluator() {
        return new LazyEvaluator(heap);
    }
    
    @SuppressWarnings({"UnnecessaryLabelOnContinueStatement", "UnnecessaryLabelOnBreakStatement"})
    public void forceCollision() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(5), HonsValue.nil);
        System.out.println("Can we force a collision?");

        collision:
        for (int i = 0; i < HonsValue.SMALLINT_MAX; i++) {
            if (i == 5) continue collision;
            HonsCell test = new HonsCell(HonsValue.fromSmallInt(i), HonsValue.nil);
            if (test.getObjectHash() == cell.getObjectHash()) {
                System.out.println(cell);
                System.out.println(test);
                System.out.println(new HonsCell(HonsValue.fromSmallInt(5+1), HonsValue.nil));
                System.out.println(new HonsCell(HonsValue.fromSmallInt(i+1), HonsValue.nil));

                var heaped = heap.cons(HonsValue.fromSmallInt(i), HonsValue.nil);
                System.out.println(heaped);

                System.out.println(heap.cons(HonsValue.fromSmallInt(5), HonsValue.nil));
                System.out.println(heap.cons(HonsValue.fromSmallInt(i), HonsValue.nil));
                break collision;
            }
        }
    }

    public void demo() {
        System.out.printf("nil:             %s%n", HonsValue.nil);
        System.out.printf("symbolTag:       %s%n", HonsValue.symbolTag);
        System.out.printf("small int -17:   %s%n", HonsValue.fromSmallInt(-17));
        System.out.printf("object hash -19: %s%n", HonsValue.fromObjectHash(-19));
        System.out.println();

        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(5), HonsValue.nil);
        System.out.printf("cell: %s%n", cell);

        HonsValue val = heap.cons(HonsValue.fromSmallInt(5), HonsValue.nil);
        System.out.printf("hons: %s%n", val);
        System.out.printf("      %s%n", heap.valueToString(val));

        System.out.print("again: ");
        System.out.println(heap.cons(HonsValue.fromSmallInt(5), HonsValue.nil));
        System.out.println();

        System.out.print("pair: ");
        System.out.println(heap.valueToString(heap.cons(
                HonsValue.fromSmallInt(HonsValue.SMALLINT_MIN),
                HonsValue.fromSmallInt(HonsValue.SMALLINT_MAX)
            )));

        var list = intList(heap, new int[]{1, 2, 3, 4, 5});
        System.out.print("list: ");
        System.out.println(heap.valueToString(list));
        System.out.println();

        System.out.printf("sum: %s%n", sumList(heap, list));
        System.out.println();
        
        System.out.printf("symbol: %s%n", heap.valueToString(heap.makeSymbol("example")));
        System.out.println();

        forceCollision();
    }

    private void parseUserArgs(String[] args)
    {
        /* Complicated logic:
         *   If --file or --expr are not given,
         *     then the first arg (if present) of app.userArguments
         *       is removed and used as --file's parameter
         *   Otherwise, don't change app.userArguments.
         * 
         *   You can't have both entries in app.userArguments already,
         *     and have more userArgs!
         *   Specifically, if -- is provided then user args must be after it
         */
        if (sourceFilename == null && sourceExpr == null) {
            if (userArguments != null && userArguments.size() > 0)
                sourceFilename = userArguments.remove(0);
        }
        if (args != null) {
            if (userArguments != null && userArguments.size() > 0)
                throw new ParameterException("Unrecognised parameters before --: " + userArguments);
            if (userArguments == null)
                userArguments = new ArrayList<>();
            userArguments.addAll(Arrays.asList(args));
        }
    }

    private void validateFlagsAreValid()
    {
        int modeFlagsSet = 0;
        if (demoMode)
            modeFlagsSet++;
        if (readMode)
            modeFlagsSet++;
        if (evalMode)
            modeFlagsSet++;
        if (modeFlagsSet > 1)
            throw new ParameterException("--demo, --read and --eval are mutually exclusive: only supply one");
        if (modeFlagsSet == 0)
            evalMode = true;
        
        if (sourceFilename != null && sourceExpr != null)
            throw new ParameterException("Cannot supply an expr and a filename");
    }

    /* Return false if the app doesn't need to run */
    public boolean parseArgs(String[] args)
    {
        if (argsParsed)
            throw new IllegalStateException("App.parseArgs is only able to be called once");
        argsParsed = true;

        // Split the arguments into args for App and for the user's program
        int splitIdx;
        boolean argSplitPresent = false;
        for (splitIdx = 0; splitIdx < args.length; splitIdx++) {
            if (args[splitIdx].equals("--")) {
                argSplitPresent = true;
                break;
            }
        }

        String[] appArgs, userArgs;
        if (argSplitPresent) {
            appArgs = Arrays.copyOfRange(args, 0, splitIdx);
            userArgs = Arrays.copyOfRange(args, splitIdx + 1, args.length);
            System.out.printf("args: %s -- %s%n", List.of(appArgs), List.of(userArgs));
        } else {
            appArgs = args;
            userArgs = null;
            System.out.printf("args: %s%n", List.of(appArgs));
        }

        // Parse argument flags here and configure the app object
        JCommander commander = JCommander.newBuilder()
            .addObject(this)
            .build();
        try {
            commander.parse(appArgs);
            parseUserArgs(userArgs);
            validateFlagsAreValid();
        } catch (ParameterException e) {
            System.err.println(e.getLocalizedMessage());
            commander.usage();
            return false;
        }

        if (this.showHelp) {
            commander.usage();
            return false;
        }
        
        return true;
    }

    @Blocking
    public void run() {
        if (showHelp)
            throw new IllegalStateException("Help should be shown without running app");
        if (!argsParsed)
            throw new IllegalStateException("Please App.parseArgs before App.run");

        if (debug) {
            System.err.printf("App flags: %n");
            System.err.printf("           debug=%s%n", debug);
            System.err.printf("           dumpHeap=%s%n", dumpHeap);
            System.err.printf("           demoMode=%s%n", demoMode);
            System.err.printf("           readMode=%s%n", readMode);
            System.err.printf("           evalMode=%s%n", evalMode);
            System.err.printf("           sourceFilename=%s%n", sourceFilename);
            System.err.printf("           sourceExpr=%s%n", sourceExpr);
            System.err.printf("           userArguments=%s%n", userArguments);
        }

        if (demoMode) {
            demo();
            System.out.println();
            LazyEvaluator.demo(heap);
        } else {
            String source = sourceExpr;

            var reader = getReader();
            var evaluator = getEvaluator();
            if (debug && evaluator instanceof LazyEvaluator)
                ((LazyEvaluator) evaluator).setDebug(true);

            var driver = new Driver(heap, reader, evaluator);

            if (source == null && sourceFilename != null) {
                try {
                    source = Files.readString(Path.of(sourceFilename), StandardCharsets.UTF_8);
                }
                catch (IOException e) {
                    System.err.println("Couldn't read program from " + sourceFilename + ": " + e);
                }
            }
            if (source == null) {
                System.out.println("No program supplied");
            }
            else {
                driver.runSource(source);
            }
        }

        if (dumpHeap) {
            System.err.printf("%n---%nHeap dump:%n");
            heap.dumpHeap(System.err);
            System.err.printf("---%n");
        }
    }

    @Blocking
    public static void main(String[] args) {
        App app = new App();
        System.out.println(app.getGreeting());

        // For development, override debug by default
        app.debug = true;

        boolean runNeeded = app.parseArgs(args);

        // Run the main program/task
        // XXX Inside this we might create a Driver object
        if (runNeeded)
            app.run();
    }
}