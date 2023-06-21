package uk.bs338.hashLisp.jproto;

public interface IHeapVisitor<V extends IValue> {
    void visitNil(V visited);
    void visitShortInt(V visited, int num);
    void visitSymbol(V visited, V val);
    void visitCons(V visited, V fst, V snd);
}
