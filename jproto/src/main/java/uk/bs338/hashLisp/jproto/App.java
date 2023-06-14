/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package uk.bs338.hashLisp.jproto;

import uk.bs338.hashLisp.jproto.hons.HonsCell;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static uk.bs338.hashLisp.jproto.Symbols.makeSymbol;
import static uk.bs338.hashLisp.jproto.Utilities.intList;

public class App {
    private final HonsHeap heap;

    public App() {
        heap = new HonsHeap();
    }

    public String getGreeting() {
        return "jproto --- prototype for HashLisp";
    }

    public void forceCollision() throws Exception {
        HonsCell cell = new HonsCell(HonsValue.fromShortInt(5), HonsValue.nil);
        System.out.println("Can we force a collision?");

        collision:
        for (int i = 0; i < HonsValue.SHORTINT_MAX; i++) {
            if (i == 5) continue collision;
            HonsCell test = new HonsCell(HonsValue.fromShortInt(i), HonsValue.nil);
            if (test.getObjectHash() == cell.getObjectHash()) {
                System.out.println(cell);
                System.out.println(test);
                System.out.println(new HonsCell(HonsValue.fromShortInt(5+1), HonsValue.nil));
                System.out.println(new HonsCell(HonsValue.fromShortInt(i+1), HonsValue.nil));

                var heaped = heap.cons(HonsValue.fromShortInt(i), HonsValue.nil);
                System.out.println(heaped);

                System.out.println(heap.cons(HonsValue.fromShortInt(5), HonsValue.nil));
                System.out.println(heap.cons(HonsValue.fromShortInt(i), HonsValue.nil));
                break collision;
            }
        }
    }

    public HonsValue sumList(HonsValue list) throws Exception {
        if (list.isNil())
            return HonsValue.fromShortInt(0);
        else if (list.isShortInt())
            return list;
        else {
            HonsValue head = sumList(heap.fst(list));
            HonsValue rest = sumList(heap.snd(list));
            return HonsValue.applyShortIntOperation((a, b) -> a + b, head, rest);
        }
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        System.out.println(app.getGreeting());

        System.out.printf("nil:             %s%n", HonsValue.nil);
        System.out.printf("tagSymbol:       %s%n", HonsValue.tagSymbol);
        System.out.printf("short int -17:   %s%n", HonsValue.fromShortInt(-17));
        System.out.printf("object hash -19: %s%n", HonsValue.fromObjectHash(-19));
        System.out.println();

        HonsCell cell = new HonsCell(HonsValue.fromShortInt(5), HonsValue.nil);
        System.out.printf("cell: %s%n", cell);

        HonsHeap heap = app.heap;
        HonsValue val = heap.cons(HonsValue.fromShortInt(5), HonsValue.nil);
        System.out.printf("hons: %s%n", val);
        System.out.printf("      %s%n", heap.valueToString(val));

        System.out.print("again: ");
        System.out.println(heap.cons(HonsValue.fromShortInt(5), HonsValue.nil));
        System.out.println();

        System.out.print("pair: ");
        System.out.println(heap.valueToString(heap.cons(
                HonsValue.fromShortInt(HonsValue.SHORTINT_MIN),
                HonsValue.fromShortInt(HonsValue.SHORTINT_MAX)
            )));

        var list = intList(heap, new int[]{1, 2, 3, 4, 5});
        System.out.print("list: ");
        System.out.println(heap.valueToString(list));
        System.out.println();

        System.out.printf("sum: %s%n", app.sumList(list));
        System.out.println();
        
        System.out.printf("symbol: %s%n", heap.valueToString(makeSymbol(heap, "example")));
        System.out.println();

        app.forceCollision();

        System.out.println();
        heap.dumpHeap(System.out);
    }
}
