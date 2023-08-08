package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;
import uk.bs338.hashLisp.jproto.ValueType;

public interface IWrappedValue {

    @NotNull String valueToString();

    @NotNull ValueType getType();
    boolean isSymbol();
    
    IWrappedCons asCons();
    IWrappedSymbol asSymbol();
    
    interface IGetValue<V extends IValue> extends IWrappedValue {
        V getValue();
        
        @Override
        default @NotNull ValueType getType() {
            return getValue().getType();
        }
    }
}
