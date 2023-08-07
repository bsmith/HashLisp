package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IEvaluator<V> {
    @NotNull V eval_one(@NotNull V val);
    
    /* If you have an array, V[], you can use Arrays.asList */
    @Contract("_ -> param1")
    @NotNull List<V> eval_multi_inplace(@NotNull List<V> vals);
}
