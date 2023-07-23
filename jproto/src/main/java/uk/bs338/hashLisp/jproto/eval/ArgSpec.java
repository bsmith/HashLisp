package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.ExprFactory;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.expr.IExprFactory;
import uk.bs338.hashLisp.jproto.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArgSpec {
    private final @NotNull IExprFactory exprFactory;
    private final @NotNull IExpr origArgSpec;
    private List<ISymbolExpr> argNames;
    private ISymbolExpr slurpyName;

    public ArgSpec(@NotNull IExprFactory exprFactory, @NotNull IExpr argSpec) throws EvalException {
        this.exprFactory = exprFactory;
        this.origArgSpec = argSpec;
        
        parseArgSpec(argSpec);
    }

    public @NotNull IExpr getOrigArgSpec() {
        return origArgSpec;
    }

    public List<ISymbolExpr> getArgNames() {
        return argNames;
    }

    public ISymbolExpr getSlurpyName() {
        return slurpyName;
    }
    
    public List<ISymbolExpr> getBoundNames() {
        var names = new ArrayList<>(getArgNames());
        if (slurpyName != null)
            names.add(slurpyName);
        return names;
    }

    private void parseArgSpec(IExpr argSpec) throws EvalException {
        argNames = new ArrayList<>();
        slurpyName = null;

        var curSpec = argSpec;
        while (!curSpec.isNil()) {
            /* XXX Would a visitor make sense here? Or an iterator? */
            if (curSpec.isSymbol()) {
                slurpyName = curSpec.asSymbol();
                break;
            } else if (curSpec.isCons()) {
                var cons = curSpec.asCons();
                if (!cons.fst().isSymbol()) {
                    throw new EvalException("Found non-symbol " + cons.fst() + " in argSpec: " + argSpec.valueToString());
                }
                argNames.add(cons.fst().asSymbol());
                curSpec = cons.snd();
            } else {
                throw new EvalException("Cannot parse argSpec at: " + curSpec.valueToString());
            }
        }
    }

    public @NotNull Assignments match(@NotNull IExpr args) {
        var assignmentsMap = new HashMap<HonsValue, HonsValue>();
        var curArg = args;
        /* XXX would an iterator make sense here? */
        for (var argName : argNames) {
            HonsValue value = HonsValue.nil;
            if (curArg.isCons()) {
                var cons = curArg.asCons();
                value = cons.fst().getValue();
                curArg = cons.snd();
            } else {
                curArg = exprFactory.nil();
            }
            assignmentsMap.put(argName.getValue(), value);
        }
        if (slurpyName != null)
            assignmentsMap.put(slurpyName.getValue(), curArg.getValue());
        return new Assignments(exprFactory, assignmentsMap);
    }

    @Override
    public String toString() {
        return "ArgSpec{" +
            "argNames=" + argNames.stream().map(IExpr::valueToString).toList() +
            ", slurpyName=" + slurpyName.valueToString() +
            '}';
    }
}
