package uk.bs338.hashLisp.jproto;

public interface IValue {
    boolean isNil();
    boolean isSymbolTag();
    boolean isShortInt();
    int toShortInt();
}
