package uk.bs338.hashLisp.jproto.driver;

import uk.bs338.hashLisp.jproto.IEvaluator;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public class Driver {
    private HonsHeap heap;
    private IReader<HonsValue> reader;
    private IEvaluator<HonsValue> evaluator;

    public Driver(HonsHeap heap, IReader<HonsValue> reader, IEvaluator<HonsValue> evaluator) {
        this.heap = heap;
        this.evaluator = evaluator;
        this.reader = reader;
    }

    protected void runOneProgram(HonsValue program) {
        System.out.printf("program = %s%n", heap.valueToString(program));
        
        var result = evaluator.eval_one(program);
        if (result.isConsRef())
            System.out.printf("head = %s%n", heap.valueToString(heap.fst(result)));
        
        /* XXX: interpret result */
        if (!result.isConsRef() || !heap.isSymbol(heap.fst(result))) {
            /* Not a io-monad command: print it */
            System.out.printf("result = %s%n", heap.valueToString(result));
            return;
        }
        
        /* Is it a recognised io-monad value? */
        var head_name = heap.symbolNameAsString(heap.fst(result)); 
        if (head_name.equals("io-print!")) {
            var value = heap.fst(heap.snd(result));
            /* XXX another bug in PrintOnlyEvaluator — it needs to quote its argument! */
            /* alternatively, ditch eval_hnf, and register the io-monad primitives? */
            /* I like the idea of unregistered symbols as constructors though, very Wolframish */
            /* eval rules for quote vs hold?  eval("(quote <x>)") == "<x>", eval("(hold <x>)") == "(hold <x>)"
             * actually the latter rule is for any unregistered symbol!
             */
            System.out.println(heap.valueToString(value));
            
            /* handle continuation, is it lambda wrapped? */
            /* XXX share a head with all IO?  eg (io! print <val> <cont>?) */
            /* XXX write a matcher! */
        }
        
        /* XXX: The below discussion is now fixed: turn this into proper docs */
        /* I think my PrintOnlyEvaluator eval_hnf is wrong.
         * I want to do an eval_hnf, then check which monad value I have, and handle the "apply" of that monad *here*
         * because the monad heads are not primitives!
         * Okay, so where do I do the wrapping?  Okay, wrapping violates the idempotence laws for eval? ie eval(eval(...(eval(x)))) == eval(x)
         *   Is this a true law?  because lists are data: eval("(list 1 2 3)") == "(1 2 3)"
         *   Haskell and Wolfram avoid this by having a head of List on lists, so programs and lists are distinct
         *   OKAY:  so I need eval_hnf to be more subtle!
         *     maybe, you *must* call apply, not eval_one after it!
         *     and eval_one is simply, uncurry(apply).eval_hnf?
         * So I need to wrap around the ReaderIterator or the Reader! [No.]
         * Maybe ReaderIterator is an Iterator over ReaderResult not IValues?
         */
    }
    
    public void runSource(String source) {
        var iterator = new ReaderIterator<>(reader, source);
        iterator.forEachRemaining(this::runOneProgram);
        /* XXX handle failure to read */
        if (iterator.getCurResult().getRemaining().length() != 0)
            throw new Error("Reading failed: " + iterator.getCurResult());
    }
}
