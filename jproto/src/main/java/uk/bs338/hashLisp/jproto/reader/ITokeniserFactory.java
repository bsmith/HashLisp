package uk.bs338.hashLisp.jproto.reader;

public interface ITokeniserFactory {
    Tokeniser createTokeniser(CharSequence source);
}
