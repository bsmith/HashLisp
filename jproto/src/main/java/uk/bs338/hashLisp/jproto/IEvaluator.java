package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface IEvaluator<V extends IValue> {
    @NotNull V eval_one(@NotNull V val);

    default @NotNull List<V> eval_multi(@NotNull List<V> vals) {
        ArrayList<V> out = new ArrayList<>(vals.size());
        for (V val : vals) {
            out.add(eval_one(val));
        }
        return out;
    }

    default void eval_multi_inplace(@NotNull V @NotNull [] vals) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = eval_one(vals[i]);
        }
    }
}
