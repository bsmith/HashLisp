package uk.bs338.hashLisp.jproto;

public record ConsPair<V extends IValue> (
    V fst,
    V snd
) {
    static public <V extends IValue> ConsPair<V> of(V fst, V snd) {
        return new ConsPair<>(fst, snd);
    }
}
