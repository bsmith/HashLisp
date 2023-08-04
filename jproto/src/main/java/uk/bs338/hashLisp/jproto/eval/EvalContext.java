package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.EnumMap;
import java.util.Map;

public class EvalContext {
    public final @NotNull HonsHeap heap;
    public final @NotNull ArgSpecCache argSpecCache;

    protected final @NotNull Map<Tag, HonsValue> tagSymbols;
    public final @NotNull HonsValue blackholeTag;
    public final @NotNull HonsValue lambdaTag;
    
    public EvalContext(@NotNull HonsHeap heap) {
        this.heap = heap;
        argSpecCache = new ArgSpecCache(heap);
        
        tagSymbols = new EnumMap<>(Tag.class);
        for (var tag : Tag.values())
            tagSymbols.put(tag, heap.makeSymbol(tag.getSymbolStr()));
        blackholeTag = tagSymbols.get(Tag.BLACKHOLE);
        lambdaTag = tagSymbols.get(Tag.LAMBDA);
    }
}
