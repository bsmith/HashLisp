package uk.bs338.hashLisp.jproto;

import org.jetbrains.annotations.NotNull;

public interface ISymbols<V> {
    @NotNull V makeSymbol(@NotNull V name);

    @NotNull V makeSymbol(@NotNull String name);

    boolean isSymbol(@NotNull V symbol);

    @NotNull V symbolName(@NotNull V symbol);

    @NotNull String symbolNameAsString(@NotNull V symbol);
}
