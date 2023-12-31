package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public /*abstract*/ class Token {
    enum TokenType {
        UNKNOWN,
        HASH,
        COLON,
        DOT,
        OPEN_PARENS,
        CLOSE_PARENS,
        DIGITS,
        SYMBOL,
        STRING
    }
    
    private final TokenType type;
    private final String token;
    private final int startPos;
    private final int endPos;
    
    public Token(TokenType type, String token, int startPos, int endPos) {
        this.type = type;
        this.token = token;
        this.startPos = startPos;
        this.endPos = endPos;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public String getToken() {
        return token;
    }
    
    public int getTokenAsInt() {
        return Integer.parseInt(token);
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public String getPositionAsString() {
        return String.format("%d-%d", startPos, endPos);
    }

    @Override
    public @NotNull String toString() {
        return "Token{" +
            "type=" + type +
            ", token='" + token + '\'' +
            ", position=" + getPositionAsString() +
            '}';
    }

    /*public abstract <T> T visit(ITokenVisitor<T> visitor);*/
}
