package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.IValue;

public interface IExprVisitor<V extends IValue, R> {
    /* nil and small int */
    R visitConstant(V visited);
    R visitSymbol(V visited);
    R visitLambda(V visited, V argSpec, V body);
    R visitApply(V visited, V head, V args);
}
