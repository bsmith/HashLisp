package uk.bs338.hashLisp.jproto;

public interface IValueFactory<T extends IValue> {
    T nil();

    T makeShortInt(int num);

    T symbolTag();
}
