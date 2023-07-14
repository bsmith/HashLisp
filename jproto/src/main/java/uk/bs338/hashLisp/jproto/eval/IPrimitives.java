package uk.bs338.hashLisp.jproto.eval;

import java.util.Optional;

public interface IPrimitives<K, V> {
    Optional<IPrimitive<V>> get(K name);
}
