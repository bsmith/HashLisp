package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

public interface IValueFactory<T extends IValue> {
    @NotNull T nil();

    @NotNull T makeSmallInt(int num);

    @NotNull T symbolTag();
}
