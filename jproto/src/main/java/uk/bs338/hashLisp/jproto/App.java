/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package uk.bs338.hashLisp.jproto;

import uk.bs338.hashLisp.jproto.hons.HonsCell;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class App {
    private final HonsHeap heap;

    public App() {
        heap = new HonsHeap();
    }

    public String getGreeting() {
        return "jproto --- prototype for HashLisp";
    }

    public void forceCollision() {
        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(5), HonsValue.nil);
        System.out.println("Can we force a collision?");

        collision:
        for (int i = 0; i < HonsValue.SMALLINT_MAX; i++) {
            if (i == 5) continue collision;
            HonsCell test = new HonsCell(HonsValue.fromSmallInt(i), HonsValue.nil);
            if (test.getObjectHash() == cell.getObjectHash()) {
                System.out.println(cell);
                System.out.println(test);
                System.out.println(new HonsCell(HonsValue.fromSmallInt(5+1), HonsValue.nil));
                System.out.println(new HonsCell(HonsValue.fromSmallInt(i+1), HonsValue.nil));

                var heaped = heap.cons(HonsValue.fromSmallInt(i), HonsValue.nil);
                System.out.println(heaped);

                System.out.println(heap.cons(HonsValue.fromSmallInt(5), HonsValue.nil));
                System.out.println(heap.cons(HonsValue.fromSmallInt(i), HonsValue.nil));
                break collision;
            }
        }
    }

    public HonsValue sumList(HonsValue list) {
        if (list.isNil())
            return HonsValue.fromSmallInt(0);
        else if (list.isSmallInt())
            return list;
        else {
            HonsValue head = sumList(heap.fst(list));
            HonsValue rest = sumList(heap.snd(list));
            return HonsValue.applySmallIntOperation((a, b) -> a + b, head, rest);
        }
    }

    public static void main(String[] args) {
        App app = new App();
        System.out.println(app.getGreeting());

        System.out.printf("nil:             %s%n", HonsValue.nil);
        System.out.printf("symbolTag:       %s%n", HonsValue.symbolTag);
        System.out.printf("small int -17:   %s%n", HonsValue.fromSmallInt(-17));
        System.out.printf("object hash -19: %s%n", HonsValue.fromObjectHash(-19));
        System.out.println();

        HonsCell cell = new HonsCell(HonsValue.fromSmallInt(5), HonsValue.nil);
        System.out.printf("cell: %s%n", cell);

        HonsHeap heap = app.heap;
        HonsValue val = heap.cons(HonsValue.fromSmallInt(5), HonsValue.nil);
        System.out.printf("hons: %s%n", val);
        System.out.printf("      %s%n", heap.valueToString(val));

        System.out.print("again: ");
        System.out.println(heap.cons(HonsValue.fromSmallInt(5), HonsValue.nil));
        System.out.println();

        System.out.print("pair: ");
        System.out.println(heap.valueToString(heap.cons(
                HonsValue.fromSmallInt(HonsValue.SMALLINT_MIN),
                HonsValue.fromSmallInt(HonsValue.SMALLINT_MAX)
            )));

        var list = intList(heap, new int[]{1, 2, 3, 4, 5});
        System.out.print("list: ");
        System.out.println(heap.valueToString(list));
        System.out.println();

        System.out.printf("sum: %s%n", app.sumList(list));
        System.out.println();
        
        System.out.printf("symbol: %s%n", heap.valueToString(heap.makeSymbol("example")));
        System.out.println();

        app.forceCollision();

        System.out.println();
        heap.dumpHeap(System.out);
    }
}
