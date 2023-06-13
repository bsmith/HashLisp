package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public interface IPrimitive {
    HonsValue apply(IHeap heap, HonsValue args);
}
