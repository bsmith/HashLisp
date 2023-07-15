package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IValue;

import java.util.Arrays;
import java.util.stream.IntStream;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class PrettyPrinter<V extends IValue> {
    private final @NotNull IHeap<V> heap;
    private final @NotNull V stringTag;

    public PrettyPrinter(@NotNull IHeap<V> heap) {
        this.heap = heap;
        stringTag = heap.makeSymbol("*string");
    }
    
    public @NotNull String quoteString(@NotNull String str) {
        /* Slow(?) but elegant */
        var escaped = str.codePoints().flatMap(ch -> {
            if (ch == '"')
                return IntStream.of('\\', '"');
            else if (ch == '\\')
                return IntStream.of('\\', '\\');
            else
                return IntStream.of(ch);
        });
        int[] result = IntStream.concat(IntStream.of('"'), IntStream.concat(escaped, IntStream.of('"'))).toArray();
        return new String(result, 0, result.length);
    }
    
    public @NotNull String listToString(@NotNull V head, @NotNull V rest) {
        return listToString(head, rest, new StringBuilder()).toString();
    }

    public @NotNull StringBuilder listToString(@NotNull V head, @NotNull V rest, @NotNull StringBuilder builder) {
        valueToString(head, builder);
        while (!rest.isNil()) {
            if (!rest.isConsRef())
                return valueToString(rest, builder.append(" . "));

            var uncons = heap.uncons(rest);

            if (uncons.fst().isSymbolTag())
                return builder.append(" . ").append(listAsString(heap, uncons.snd()));
            if (uncons.fst().equals(stringTag))
                return builder.append(" . ").append(quoteString(listAsString(heap, uncons.snd())));

            builder.append(" ");
            valueToString(uncons.fst(), builder);
            rest = uncons.snd();
        }
        return builder;
    }
    
    public @NotNull StringBuilder valueToString(@NotNull V val, @NotNull StringBuilder builder) {
        if (!val.isConsRef())
            return builder.append(val);
        
        var uncons = heap.uncons(val);
        if (uncons.fst().isSymbolTag())
            return builder.append(listAsString(heap, uncons.snd()));
        if (uncons.fst().equals(stringTag))
            return builder.append(quoteString(listAsString(heap, uncons.snd())));

        builder.append("(");
        listToString(uncons.fst(), uncons.snd(), builder);
        return builder.append(")");
    }

    public @NotNull String valueToString(@NotNull V val) {
        return valueToString(val, new StringBuilder()).toString();
    }
    
    public static <V extends IValue> @NotNull String valueToString(@NotNull IHeap<V> heap, @NotNull V val) {
        return new PrettyPrinter<>(heap).valueToString(val);
    }
}
