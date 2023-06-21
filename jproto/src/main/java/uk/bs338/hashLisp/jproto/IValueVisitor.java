package uk.bs338.hashLisp.jproto;

public interface IValueVisitor<V extends IValue> {
    void visitNil(V visited);
    void visitSymbolTag(V visited);
    void visitShortInt(V visited, int num);
    void visitConsRef(V val);
}
