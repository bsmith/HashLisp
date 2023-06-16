package uk.bs338.hashLisp.jproto.reader;

public /*abstract*/ class Token {
    private final String token;
    private final int startPos;
    private final int endPos;
    
    public Token(String token, int startPos, int endPos) {
        this.token = token;
        this.startPos = startPos;
        this.endPos = endPos;
    }
    
    public String getToken() {
        return token;
    }
    
    public int getTokenAsInt() {
        return Integer.parseInt(token);
    }
    
    public String getPositionAsString() {
        return String.format("%d-%d", startPos, endPos);
    }
    
    /*public abstract <T> T visit(ITokenVisitor<T> visitor);*/
}
