package uk.bs338.hashLisp.jproto.wrapped;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IValue;

public interface IWrappedValue2 {

    @NotNull String valueToString();

    boolean isNil();
    boolean isSymbolTag();
    boolean isSmallInt();
    boolean isCons();
    boolean isSymbol();
    
    IWrappedCons2 asCons();
    IWrappedSymbol2 asSymbol();
    
    IWrappedSymbol2 makeSymbol();
    
    public interface IGetValue<V extends IValue> extends IWrappedValue2 {
        V getValue();


        @Override
        default public boolean isNil() {
            return getValue().isNil();
        }

        @Override
        default public boolean isSymbolTag() {
            return getValue().isSymbolTag();
        }

        @Override
        default public boolean isSmallInt() {
            return getValue().isSmallInt();
        }
    }
    
//    public static interface Boxed<V extends IValue> extends IWrappedValue2<V, Boxed<V>> {
//    }
//    
//    public static interface Boxed2 extends IWrappedValue2<IValue, Boxed2> {
//        
//    }
    
//    public static interface Base<V extends IValue, W extends IWrappedValue2<V, W>> extends
//        IWrappedValue2<V, W>,
//        Boxed<V>,
//        Boxed2 {
//        
//    }
}
