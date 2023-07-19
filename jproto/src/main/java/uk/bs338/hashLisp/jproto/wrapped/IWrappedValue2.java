package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

public interface IWrappedValue2<V extends IValue, W extends IWrappedValue2<V, W>> {
    V getValue();

    @NotNull String valueToString();

    default boolean isNil() { return getValue().isNil(); }
    default boolean isSymbolTag() { return getValue().isSymbolTag(); }
    default boolean isSmallInt() { return getValue().isSmallInt(); }
    default boolean isCons() { return getValue().isConsRef(); }
    boolean isSymbol();
    
    IWrappedCons2<V, W> asCons();
    IWrappedSymbol2<V, W> asSymbol();
    
    public static interface Boxed<V extends IValue> extends IWrappedValue2<V, Boxed<V>> {
    }
    
    public static interface Boxed2 extends IWrappedValue2<IValue, Boxed2> {
        
    }
}
