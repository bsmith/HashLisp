package uk.bs338.hashLisp.jproto;

import java.util.Objects;

public final class Pair<V> {
    public final V fst;
    public final V snd;
    
    private Pair(V fst, V snd) {
        this.fst = fst;
        this.snd = snd;
    }
    
    public static <V> Pair<V> of(V fst, V snd) {
        return new Pair<>(fst, snd);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?> pair = (Pair<?>) o;
        return Objects.equals(fst, pair.fst) && Objects.equals(snd, pair.snd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fst, snd);
    }

    @Override
    public String toString() {
        return "Pair{" + fst + ", " + snd + "}";
    }
}
