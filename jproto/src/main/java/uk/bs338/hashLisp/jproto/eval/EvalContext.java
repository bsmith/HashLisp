package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.EnumMap;
import java.util.Map;

public class EvalContext {
    public final @NotNull HonsMachine machine;
    public final @NotNull ArgSpecCache argSpecCache;

    protected final @NotNull Map<Tag, HonsValue> tagSymbols;
    public final @NotNull ISymbolExpr blackholeTag;
    public final @NotNull ISymbolExpr lambdaTag;
    
    public EvalContext(@NotNull HonsMachine machine) {
        this.machine = machine;
        argSpecCache = new ArgSpecCache(machine);
        
        tagSymbols = new EnumMap<>(Tag.class);
        for (var tag : Tag.values())
            tagSymbols.put(tag, machine.makeSymbol(tag.getSymbolStr()));
        blackholeTag = IExpr.wrap(machine, tagSymbols.get(Tag.BLACKHOLE)).asSymbolExpr();
        lambdaTag = IExpr.wrap(machine, tagSymbols.get(Tag.LAMBDA)).asSymbolExpr();
    }
}
