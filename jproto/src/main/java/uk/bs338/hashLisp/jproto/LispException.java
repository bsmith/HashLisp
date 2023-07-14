package uk.bs338.hashLisp.jproto;

public class LispException extends Exception {
    public LispException(String message) {
        super(message);
    }

    public LispException(String message, Throwable cause) {
        super(message, cause);
    }
}
