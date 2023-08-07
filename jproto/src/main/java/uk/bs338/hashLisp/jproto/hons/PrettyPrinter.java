package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IMachine;
import uk.bs338.hashLisp.jproto.IValue;

import java.util.Optional;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class PrettyPrinter<V extends IValue> {
    private final @NotNull IMachine<V> machine;
    private final @NotNull V stringTag;

    public PrettyPrinter(@NotNull IMachine<V> machine) {
        this.machine = machine;
        stringTag = machine.makeSymbol("*string");
//        stringTag = heap.cons(HonsValue.symbolTag, stringAsList(heap, "*string"));
    }
    
    private @NotNull Optional<String> stringifyNonList(@NotNull V val) {
        if (!val.isConsRef())
            return Optional.of(val.toString());

        try {
            var uncons = machine.uncons(val);
            if (uncons.fst().isSymbolTag())
                return Optional.of(listAsString(machine, uncons.snd()));
            if (uncons.fst().equals(stringTag))
                return Optional.of(Strings.quoteString(listAsString(machine, uncons.snd())));
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

        var uncons = machine.uncons(val);
        builder.append("(");
        stringifyOneValue(uncons.fst(), builder);
        listToString(uncons.snd(), builder);
        return builder.append(")");
    }

    private void listToString(@NotNull V rest, @NotNull StringBuilder builder) {
        while (!rest.isNil()) {
            var tailNonList = stringifyNonList(rest);
            if (tailNonList.isPresent()) {
                builder.append(" . ").append(tailNonList.get());
                return;
            }
            
            var uncons = machine.uncons(rest);
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
    
    public static <V extends IValue> @NotNull String valueToString(@NotNull IMachine<V> machine, V val) {
        if (val == null)
            return "<null>";
        return new PrettyPrinter<>(machine).valueToString(val);
    }
}
