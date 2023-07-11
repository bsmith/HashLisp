package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;

import static uk.bs338.hashLisp.jproto.Utilities.listAsString;

public class PrettyPrinter {
    private final @NotNull HonsHeap heap;

    public PrettyPrinter(@NotNull HonsHeap heap) {
        this.heap = heap;
    }
    
//    public String listToString(@NotNull HonsValue head, @NotNull HonsValue rest) {
//        return listToString(head, rest, "");
//    }

    public StringBuilder listToString(@NotNull HonsValue head, @NotNull HonsValue rest, StringBuilder builder) {
        valueToString(head, builder);

        if (rest.isNil())
            return builder;

        if (!rest.isObjectHash())
            return valueToString(rest, builder.append(" . "));

        var restCell = heap.getCell(rest);
        if (restCell == null)
            return valueToString(rest, builder.append(" . "));

        builder.append(" ");
        return listToString(restCell.getFst(), restCell.getSnd(), builder);
    }
    
    /* XXX: this is not generic over implementations */
    public StringBuilder valueToString(@NotNull HonsValue val, StringBuilder builder) {
        if (val.isObjectHash()) {
            var cell = heap.getCell(val);
            if (cell == null)
                return builder.append(val);
            var special = cell.getSpecial();
            if (special != null)
                return builder.append(String.format("#%d:%s", cell.getObjectHash(), special));
            if (cell.getFst().equals(HonsValue.symbolTag)) {
                String symName = listAsString(heap, cell.getSnd());
                return builder.append(symName);
            }
            builder.append("(");
            listToString(cell.getFst(), cell.getSnd(), builder);
            return builder.append(")");
        } else {
            return builder.append(val);
        }
    }

    public String valueToString(@NotNull HonsValue val) {
        return valueToString(val, new StringBuilder()).toString();
    }
    
    public static String valueToString(@NotNull HonsHeap heap, @NotNull HonsValue val) {
        return new PrettyPrinter(heap).valueToString(val);
    }
}
