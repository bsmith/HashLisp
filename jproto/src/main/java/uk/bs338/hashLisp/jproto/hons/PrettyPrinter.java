package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IValue;
import uk.bs338.hashLisp.jproto.ValueType;

import java.util.Optional;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class PrettyPrinter<V extends IValue> {
    private final @NotNull IHeap<V> heap;
    private final @NotNull V stringTag;

    public PrettyPrinter(@NotNull IHeap<V> heap) {
        this.heap = heap;
        stringTag = heap.makeSymbol("*string");
    }
    
    private @NotNull Optional<String> stringifyNonList(@NotNull V val) {
        if (val.getType() != ValueType.CONS_REF)
            return Optional.of(val.toString());

        try {
            var uncons = heap.uncons(val);
            if (uncons.fst().getType() == ValueType.SYMBOL_TAG)
                return Optional.of(listAsString(heap, uncons.snd()));
            if (uncons.fst().equals(stringTag))
                return Optional.of(Strings.quoteString(listAsString(heap, uncons.snd())));
        }
        catch (IllegalStateException e) {
            return Optional.of(val.toString());
        }
        
        return Optional.empty();
    }
    
    private @NotNull StringBuilder stringifyOneValue(@NotNull V val, @NotNull StringBuilder builder) {
        var common = stringifyNonList(val);
        if (common.isPresent()) {
            return builder.append(common.get());
        }

        var uncons = heap.uncons(val);
        builder.append("(");
        stringifyOneValue(uncons.fst(), builder);
        listToString(uncons.snd(), builder);
        return builder.append(")");
    }

    private void listToString(@NotNull V rest, @NotNull StringBuilder builder) {
        while (rest.getType() != ValueType.NIL) {
            var tailNonList = stringifyNonList(rest);
            if (tailNonList.isPresent()) {
                builder.append(" . ").append(tailNonList.get());
                return;
            }
            
            var uncons = heap.uncons(rest);
            rest = uncons.snd();

            builder.append(" ");
            stringifyOneValue(uncons.fst(), builder);
        }
    }
    
    public @NotNull StringBuilder valueToString(V val, @NotNull StringBuilder builder) {
        if (val == null)
            return builder.append("<null>");
        return stringifyOneValue(val, builder);
    }

    public @NotNull String valueToString(V val) {
        if (val == null)
            return "<null>";
        return valueToString(val, new StringBuilder()).toString();
    }
    
    public static <V extends IValue> @NotNull String valueToString(@NotNull IHeap<V> heap, V val) {
        if (val == null)
            return "<null>";
        return new PrettyPrinter<>(heap).valueToString(val);
    }
}
