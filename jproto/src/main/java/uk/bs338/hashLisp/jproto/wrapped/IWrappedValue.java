package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;
import uk.bs338.hashLisp.jproto.ValueType;

public interface IWrappedValue {

    @NotNull String valueToString();

    boolean isNil();
    boolean isSymbolTag();
    boolean isSmallInt();
    boolean isCons();
    boolean isSymbol();
    
    IWrappedCons asCons();
    IWrappedSymbol asSymbol();
    
    IWrappedSymbol makeSymbol();
    
    interface IGetValue<V extends IValue> extends IWrappedValue {
        V getValue();
        
        @Override
        default boolean isNil() {
            return getValue().getType() == ValueType.NIL;
        }

        @Override
        default boolean isSymbolTag() {
            return getValue().getType() == ValueType.SYMBOL_TAG;
        }

        @Override
        default boolean isSmallInt() {
            return getValue().getType() == ValueType.SMALL_INT;
        }
    }
}
