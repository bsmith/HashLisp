package uk.bs338.hashLisp.jproto.driver;

import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.ValueType;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.wrapped.Context;
import uk.bs338.hashLisp.jproto.wrapped.WrappedValue;

public class REPL extends Context {

    public REPL(HonsMachine machine, IReader<HonsValue> reader, IEvaluator<HonsValue> evaluator) {
        super(machine, reader, evaluator);
    }

    protected void runOneProgram(WrappedValue program) {
        System.out.printf("program = %s%n", this.valueToString(program));
        
        var result = eval_one(program);
        
        /* Is it a recognised io-monad value? */
        if (result.getType() == ValueType.CONS_REF) {
            var head_name = this.symbolNameAsString(this.fst(result));
            if (head_name.equals("*io")) {
                /* call out to IODriver */
                System.out.printf("calling IO driver with %s%n", this.valueToString(result));
            }
        }

        /* just print it */
        System.out.printf("result = %s%n", this.valueToString(result));
    }
    
    public void runSource(String source) {
        var iterator = new ReaderIterator<>(this, source);
        iterator.forEachRemaining(this::runOneProgram);
        
        /* XXX handle failure to read better */
        if (iterator.getCurResult().getRemaining().length() != 0) {
            System.out.flush();
            System.err.flush();
            System.err.println("Reading failed:");
            System.err.println(iterator.getCurResult().getFailureMessage());
        }
    }
}
