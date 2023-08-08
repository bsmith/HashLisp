package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.expr.ExprType;
import uk.bs338.hashLisp.jproto.expr.IExpr;
import uk.bs338.hashLisp.jproto.expr.ISymbolExpr;
import uk.bs338.hashLisp.jproto.hons.HonsMachine;
import uk.bs338.hashLisp.jproto.hons.HonsValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArgSpec {
    private final @NotNull HonsMachine machine;
    private final @NotNull IExpr origArgSpec;
    private List<ISymbolExpr> argNames;
    private ISymbolExpr slurpyName;

    public ArgSpec(@NotNull HonsMachine machine, @NotNull IExpr argSpec) throws EvalException {
        this.machine = machine;
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
        while (curSpec.getType() != ExprType.NIL) {
            /* XXX Would a visitor make sense here? Or an iterator? */
            if (curSpec.getType() == ExprType.SYMBOL) {
                slurpyName = curSpec.asSymbolExpr();
                break;
            } else if (curSpec.getType() == ExprType.CONS) {
                var cons = curSpec.asConsExpr();
                if (cons.fst().getType() != ExprType.SYMBOL) {
                    throw new EvalException("Found non-symbol " + cons.fst() + " in argSpec: " + argSpec.valueToString());
                }
                argNames.add(cons.fst().asSymbolExpr());
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
            if (curArg.getType() == ExprType.CONS) {
                var cons = curArg.asConsExpr();
                value = cons.fst().getValue();
                curArg = cons.snd();
            } else {
                curArg = IExpr.nil(machine);
            }
            assignmentsMap.put(argName.getValue(), value);
        }
        if (slurpyName != null)
            assignmentsMap.put(slurpyName.getValue(), curArg.getValue());
        return new Assignments(machine, assignmentsMap);
    }

    @Override
    public String toString() {
        return "ArgSpec{" +
            "argNames=" + argNames.stream().map(IExpr::valueToString).toList() +
            ", slurpyName=" + slurpyName.valueToString() +
            '}';
    }
    
    public @NotNull Set<ISymbolExpr> getBoundVariables() {
        Set<ISymbolExpr> names = new HashSet<>(argNames.size() + 1);

        if (slurpyName != null)
            names.add(slurpyName);

        names.addAll(argNames);

        return names;
    }
    
    public @NotNull Assignments alphaConversion(int uniqNumber) {
        Map<HonsValue, HonsValue> oldNameToNewName = new HashMap<>(argNames.size() + 1);
        String prefix = "$%x$".formatted(uniqNumber);
        
        var boundVariables = this.getBoundVariables();
        
        for (var old : boundVariables) {
            var oldName = machine.symbolName(old.getValue());
            if (machine.fst(oldName).toSmallInt() == '$')
                continue;
            
            var newName = machine.symbolName(old.getValue());
            for (int idx = prefix.length() - 1; idx >= 0; idx--) {
                newName = machine.cons(HonsValue.fromSmallInt(prefix.charAt(idx)), newName);
            }
            oldNameToNewName.put(old.getValue(), machine.makeSymbol(newName));
        }
        
        return new Assignments(machine, oldNameToNewName);
    }
}
