package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IValue;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class PrettyPrinter<V extends IValue> {
    private final @NotNull IHeap<V> heap;

    public PrettyPrinter(@NotNull IHeap<V> heap) {
        this.heap = heap;
    }
    
    public String listToString(@NotNull V head, @NotNull V rest) {
        return listToString(head, rest, new StringBuilder()).toString();
    }

    public StringBuilder listToString(@NotNull V head, @NotNull V rest, StringBuilder builder) {
        valueToString(head, builder);
        while (!rest.isNil()) {
            if (!rest.isConsRef())
                return valueToString(rest, builder.append(" . "));

            var uncons = heap.uncons(rest);

            if (uncons.fst().isSymbolTag())
                return builder.append(" . ").append(listAsString(heap, uncons.snd()));

            builder.append(" ");
            valueToString(uncons.fst(), builder);
            rest = uncons.snd();
        }
        return builder;
    }
    
    /* XXX: this is not generic over implementations */
    public StringBuilder valueToString(@NotNull V val, StringBuilder builder) {
        if (!val.isConsRef())
            return builder.append(val);
        
        var uncons = heap.uncons(val);
        if (uncons.fst().isSymbolTag())
            return builder.append(listAsString(heap, uncons.snd()));

        builder.append("(");
        listToString(uncons.fst(), uncons.snd(), builder);
        return builder.append(")");
    }

    public String valueToString(@NotNull V val) {
        return valueToString(val, new StringBuilder()).toString();
    }
    
    public static <V extends IValue> String valueToString(@NotNull IHeap<V> heap, @NotNull V val) {
        return new PrettyPrinter(heap).valueToString(val);
    }
}
