package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IValue;

import java.util.Optional;
import java.util.stream.IntStream;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class PrettyPrinter<V extends IValue> {
    private final @NotNull IHeap<V> heap;
    private final @NotNull V stringTag;

    public PrettyPrinter(@NotNull IHeap<V> heap) {
        this.heap = heap;
        stringTag = heap.makeSymbol("*string");
    }

    private @NotNull IntStream quoteChar(@NotNull int ch) {
        /* Java backslash sequences are \t, \b, \n, \r, \f, \', \", \\ */
        return switch (ch) {
            case '\t' -> IntStream.of('\\', 't');
            case '\b' -> IntStream.of('\\', 'b');
            case '\n' -> IntStream.of('\\', 'n');
            case '\r' -> IntStream.of('\\', 'r');
            case '\f' -> IntStream.of('\\', 'f');
            case '\'' -> IntStream.of('\\', '\'');
            case '"' -> IntStream.of('\\', '"');
            case '\\' -> IntStream.of('\\', '\\');
            default ->
                /* NB. doesn't quote non-printable */
                IntStream.of(ch);
        };
    }
    
    public @NotNull String quoteString(@NotNull String str) {
        /* Slow(?) but elegant */
        var escaped = str.codePoints().flatMap(this::quoteChar);
        int[] result = IntStream.concat(IntStream.of('"'), IntStream.concat(escaped, IntStream.of('"'))).toArray();
        return new String(result, 0, result.length);
    }
    
    private @NotNull Optional<String> commonNonList(@NotNull V val) {
        if (!val.isConsRef())
            return Optional.of(val.toString());

        var uncons = heap.uncons(val);
        if (uncons.fst().isSymbolTag())
            return Optional.of(listAsString(heap, uncons.snd()));
        if (uncons.fst().equals(stringTag))
            return Optional.of(quoteString(listAsString(heap, uncons.snd())));
        
        return Optional.empty();
    }

    private @NotNull StringBuilder listToString(@NotNull V head, @NotNull V rest, @NotNull StringBuilder builder) {
        valueToString(head, builder);
        while (!rest.isNil()) {
            var common = commonNonList(rest);
            if (common.isPresent()) {
                return builder.append(" . ").append(common.get());
            }

            var uncons = heap.uncons(rest);
            builder.append(" ");
            valueToString(uncons.fst(), builder);
            rest = uncons.snd();
        }
        return builder;
    }
    
    public @NotNull StringBuilder valueToString(@NotNull V val, @NotNull StringBuilder builder) {
        var common = commonNonList(val);
        if (common.isPresent()) {
            return builder.append(common.get());
        }
        
        var uncons = heap.uncons(val);
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
