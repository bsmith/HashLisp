package uk.bs338.hashLisp.jproto.wrapped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.Utilities;
import uk.bs338.hashLisp.jproto.eval.LazyEvaluator;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
    HonsHeap heap;
    Reader reader;
    LazyEvaluator evaluator;
    Context context;

    @BeforeEach
    void setUp() {
        heap = new HonsHeap();
        reader = new Reader(heap, Tokeniser.getFactory(new CharClassifier()));
        evaluator = new LazyEvaluator(heap);
        context = new Context(heap, reader, evaluator);
    }

    @Test
    void evaluate() {
        /* this also exercises using Context as a WrappedHeap/IHeap! */
        var value = Utilities.makeList(context, context.makeSymbol("add"), context.makeSmallInt(1), context.makeSmallInt(2));
        var retval = context.evaluate(value);
        assertEquals(context.makeSmallInt(3), retval);
    }

    @Test
    void read() {
        var readResult = context.read("(1 . 2)");
        assertTrue(readResult.isSuccess());
        assertEquals(context.cons(context.makeSmallInt(1), context.makeSmallInt(2)), readResult.getValue());
    }
}