package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;

public interface ITokeniserFactory {
    @NotNull Tokeniser createTokeniser(@NotNull CharSequence source);
}
