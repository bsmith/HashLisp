package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.IValue;

public interface IPrimitive<T extends IValue>  {
    T apply(T args) throws Exception;
}
