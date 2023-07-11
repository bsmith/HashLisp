package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class PrettyPrinter {
    private final @NotNull HonsHeap heap;

    public PrettyPrinter(@NotNull HonsHeap heap) {
        this.heap = heap;
    }
    
//    public String listToString(@NotNull HonsValue head, @NotNull HonsValue rest) {
//        return listToString(head, rest, "");
//    }

    public <V extends HonsValue> StringBuilder listToString(@NotNull V head, @NotNull HonsValue rest, StringBuilder builder) {
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
    public StringBuilder valueToString(@NotNull HonsValue val, StringBuilder builder) {
        if (!val.isConsRef())
            return builder.append(val);
        
        var uncons = heap.uncons(val);
        if (uncons.fst().isSymbolTag())
            return builder.append(listAsString(heap, uncons.snd()));

        builder.append("(");
        listToString(uncons.fst(), uncons.snd(), builder);
        return builder.append(")");
    }

    public <V extends HonsValue> String valueToString(@NotNull V val) {
        return valueToString(val, new StringBuilder()).toString();
    }
    
    public static <V extends HonsValue> String valueToString(@NotNull HonsHeap heap, @NotNull V val) {
        return new PrettyPrinter(heap).valueToString(val);
    }
}
