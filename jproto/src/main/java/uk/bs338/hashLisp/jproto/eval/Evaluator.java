package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.HashMap;
import java.util.Map;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class Evaluator {
    private final HonsHeap heap;
    private final Map<HonsValue, IPrimitive<HonsValue>> primitives;

    public Evaluator(HonsHeap heap) throws Exception {
        this.heap = heap;
        primitives = new HashMap<>();
        
        primitives.put(stringAsList(heap, "add"), this::add);
    }
    
//    public ILispValue add(ILispValue args) throws Exception {
//    public <T extends ILispValue<T>> T add(T args) throws Exception {
    public HonsValue add(HonsValue args) throws Exception {
        int sum = 0;
        var cur = args;
        while (cur.isConsRef()) {
            var fst = eval(heap.fst(cur));
            if (fst.isShortInt())
                sum += fst.toShortInt();
            cur = heap.snd(cur);
        }
        if (cur.isShortInt())
            sum += cur.toShortInt();
        return heap.makeShortInt(sum);
    }

    public HonsValue eval(HonsValue val) throws Exception {
        System.out.printf("eval: %s%n", heap.valueToString(val));
        if (val.isNil() || val.isShortInt() || heap.isSymbol(val)) {
            return val;
        } else if (val.isConsRef()) {
            /* cons */
            var head = eval(heap.fst(val));
            var args = heap.snd(val);
            if (heap.isSymbol(head)) {
                var prim = primitives.get(heap.snd(head));
                if (prim != null) {
                    return prim.apply(args);
                }
            }
            return heap.cons(head, args);
        }
        return null;
    }
    
    public static void main(String[] args) throws Exception {
        demo(new HonsHeap());
    }
    
    public static void demo(HonsHeap heap) throws Exception {
        System.out.println("Evaluator demo");
        
        var evaluator = new Evaluator(heap);
        
        var add = heap.makeSymbol("add");
        var program = makeList(heap,
            add,
            heap.makeShortInt(5),
            makeList(heap,
                add,
                heap.makeShortInt(2),
                heap.makeShortInt(3)
            )
        );
        System.out.println(heap.valueToString(program));

        System.out.println(heap.valueToString(evaluator.eval(program)));
    }
}
