package uk.bs338.hashLisp.jproto.wrapped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.eval.LazyEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
    HonsMachine machine;
    Reader reader;
    LazyEvaluator evaluator;
    Context context;

    @BeforeEach
    void setUp() {
        machine = new HonsMachine();
        reader = new Reader(machine, Tokeniser.getFactory(new CharClassifier()));
        evaluator = new LazyEvaluator(machine);
        context = new Context(machine, reader, evaluator);
    }

    @Test
    void evaluate() {
        /* this also exercises using Context as a WrappedHeap/IHeap! */
        var value = Utilities.makeList(context, context.makeSymbol("add"), context.makeSmallInt(1), context.makeSmallInt(2));
        var retval = context.evaluate(value);
        assertEquals(context.makeSmallInt(3), retval);
    }

    @Test
    void evaluateWith() {
        /* this also exercises using Context as a WrappedHeap/IHeap! */
        var value = Utilities.makeList(context, context.makeSymbol("add"), context.makeSmallInt(1), context.makeSymbol("two"));
        var globals = new HashMap<WrappedValue, WrappedValue>();
        globals.put(context.makeSymbol("two"), context.makeSmallInt(2));
        var retval = context.evaluateWith(globals, value);
        assertEquals(context.makeSmallInt(3), retval);
    }

    @Test
    void read() {
        var readResult = context.read("(1 . 2)");
        assertTrue(readResult.isSuccess());
        assertEquals(context.cons(context.makeSmallInt(1), context.makeSmallInt(2)), readResult.getValue());
    }
}