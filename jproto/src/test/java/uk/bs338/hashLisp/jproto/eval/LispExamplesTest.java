package uk.bs338.hashLisp.jproto.eval;

import com.opencsv.CSVReaderBuilder;
import org.junit.jupiter.api.*;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.reader.CharClassifier;
import uk.bs338.hashLisp.jproto.reader.Reader;
import uk.bs338.hashLisp.jproto.reader.Tokeniser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LispExamplesTest {
    HonsHeap heap;
    Reader reader;
    LazyEvaluator evaluator;
    
    @BeforeEach void setUp() {
        if (heap == null)
            heap = new HonsHeap();
        if (reader == null)
            reader = new Reader(heap, Tokeniser.getFactory(new CharClassifier()));
        evaluator = new LazyEvaluator(heap);
        evaluator.setDebug(true);
    }
    
    @AfterAll void dumpHeap() {
        heap.dumpHeap(System.out);
    }
    
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void assertEval(String expectedStr, String programStr) {
        var expected = reader.read(expectedStr).getValue().get();
        var program = reader.read(programStr).getValue().get();
        var actual = evaluator.eval(program);
        assertEquals(expected, actual);
    }
    
    @Test void fstSndCons() {
        assertEval("1", "(fst (cons 1 2))");
        assertEval("2", "(snd (cons 1 2))");
    }
    
    @Test void lambda() {
        assertEval("(lambda (x) (add 1 x))", "(lambda (x) (add 1 x))");
        assertEval("1", "((lambda (x) (add 1 x)) 0)");
        assertEval("2", "((lambda (f) (f 1)) (lambda (x) (add 1 x)))");
    }
    
    @TestFactory
    Stream<DynamicTest> csvTestCases() throws IOException {
        var resource = getClass().getClassLoader().getResourceAsStream("lispExamples.csv");
        assert resource != null;
        try (var reader = new CSVReaderBuilder(new InputStreamReader(resource)).build()) {
            return DynamicTest.stream(reader.iterator(),
                (String[] fields) -> fields[0],
                (String[] fields) -> {
                    assertEval(fields[2], fields[1]);
                }
            );
        }
    }
}
