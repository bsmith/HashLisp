package uk.bs338.hashLisp.jproto;

import java.util.Arrays;
import java.util.List;

public interface IEvaluator<V extends IValue> {
    V eval_one(V val);

    default List<V> eval_multi(List<V> vals) {
        V[] arr = (V[])vals.toArray();
        eval_multi_inplace(arr);
        return Arrays.asList(arr);
    }

    default void eval_multi_inplace(V[] vals) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = eval_one(vals[i]);
        }
    }

    V eval_hnf(V val);

    V apply(V head, V args);
}
