package uk.bs338.hashLisp.jproto.eval;

import uk.bs338.hashLisp.jproto.LispException;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

public class EvalException extends LispException {
    private String currentlyEvaluating;
    private String primitive;

    public EvalException(String message) {
        super(message);
    }

    public EvalException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCurrentlyEvaluating() {
        return currentlyEvaluating;
    }

    public void setCurrentlyEvaluating(String currentlyEvaluating) {
        this.currentlyEvaluating = currentlyEvaluating;
    }
    
    public String getPrimitive() {
        return primitive;
    }

    public void setPrimitive(String primitive) {
        this.primitive = primitive;
    }

    @Override
    public String toString() {
        String msg = super.toString();
        if (currentlyEvaluating != null)
            msg += " (currently evaluating: %s)".formatted(currentlyEvaluating);
        if (primitive != null)
            msg += " (primitive: %s)".formatted(primitive);
        return msg;
    }
}
