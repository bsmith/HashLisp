package uk.bs338.hashLisp.jproto.driver;

import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.wrapped.Context;

public class IODriver {
    private final HonsHeap heap;
    private final IReader<HonsValue> reader;
    private final IEvaluator<HonsValue> evaluator;

    public IODriver(Context context) {
        this.heap = context.getHeap();
        this.evaluator = context.getEvaluator();
        this.reader = context.getReader();
    }

    protected void interpretIO(HonsValue program) {
        System.out.printf("interpreting IO = %s%n", heap.valueToString(program));

        throw new Error("unimplemented");
    }
}
