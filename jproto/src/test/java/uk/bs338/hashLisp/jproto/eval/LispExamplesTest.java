package uk.bs338.hashLisp.jproto.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import static org.junit.jupiter.api.Assertions.*;

public class LispExamplesTest {
    HonsHeap heap;
    Reader reader;
    LazyEvaluator evaluator;
    
    @BeforeEach void setUp() throws Exception {
        if (heap == null)
            heap = new HonsHeap();
        if (reader == null)
            reader = new Reader(heap, Tokeniser.getFactory(new CharClassifier()));
        evaluator = new LazyEvaluator(heap);
        evaluator.setDebug(true);
    }
    
    void assertEval(String expectedStr, String programStr) throws Exception {
        var expected = reader.read(expectedStr).getValue().get();
        var program = reader.read(programStr).getValue().get();
        var actual = evaluator.eval(program);
        assertEquals(expected, actual);
    }
    
    @Test void fstSnd() throws Exception {
        assertEval("1", "(fst (1 . 2))");
        assertEval("2", "(snd (1 . 2))");
    }
}
