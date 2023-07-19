package uk.bs338.hashLisp.jproto.hons;

public class HeapValidationError extends RuntimeException {
    public HeapValidationError() {
        super("Heap validation error");
    }

    public HeapValidationError(String message) {
        super(message);
    }
}
