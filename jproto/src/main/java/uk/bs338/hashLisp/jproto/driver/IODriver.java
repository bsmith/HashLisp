package uk.bs338.hashLisp.jproto.driver;

import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.wrapped.Context;

public class IODriver {
    private final HonsMachine machine;
    private final IReader<HonsValue> reader;
    private final IEvaluator<HonsValue> evaluator;

    public IODriver(Context context) {
        this.machine = context.getMachine();
        this.evaluator = context.getEvaluator();
        this.reader = context.getReader();
    }

    protected void interpretIO(HonsValue program) {
        System.out.printf("interpreting IO = %s%n", machine.valueToString(program));

        throw new Error("unimplemented");
    }
}
