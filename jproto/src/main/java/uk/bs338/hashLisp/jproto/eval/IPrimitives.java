package uk.bs338.hashLisp.jproto.eval;

public interface IPrimitives<K, V> {
    IPrimitive<V> get(K name);
}
