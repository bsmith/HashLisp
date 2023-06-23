package uk.bs338.hashLisp.jproto;

public interface IValue {
    boolean isNil();
    boolean isSymbolTag();
    boolean isSmallInt();
    boolean isConsRef();
    int toSmallInt();
}
