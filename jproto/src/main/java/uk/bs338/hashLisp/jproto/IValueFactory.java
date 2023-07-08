package uk.bs338.hashLisp.jproto;

public interface IValueFactory<T extends IValue> {
    T nil();

    T makeSmallInt(int num);

    T symbolTag();
}
