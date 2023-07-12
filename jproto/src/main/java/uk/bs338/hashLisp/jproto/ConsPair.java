package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record ConsPair<V extends IValue> (
    V fst,
    V snd
) {
    static public <V extends IValue> @NotNull ConsPair<V> of(V fst, V snd) {
        return new ConsPair<>(fst, snd);
    }
    
    public <T extends IValue> @NotNull ConsPair<T> fmap(@NotNull Function<V, T> func) {
        return new ConsPair<>(func.apply(fst), func.apply(snd));
    }
}
