package uk.bs338.hashLisp.jproto.eval;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.bs338.hashLisp.jproto.ValueType;
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
    private final @NotNull HonsValue origArgSpec;
    private List<HonsValue> argNames;
    private @Nullable HonsValue slurpyName;

    public ArgSpec(@NotNull HonsMachine machine, @NotNull HonsValue argSpec) throws EvalException {
        this.machine = machine;
        this.origArgSpec = argSpec;
        
        parseArgSpec(argSpec);
    }

    public @NotNull HonsValue getOrigArgSpec() {
        return origArgSpec;
    }

    private void parseArgSpec(HonsValue argSpec) throws EvalException {
        argNames = new ArrayList<>();
        slurpyName = null;

        var curSpec = argSpec;
        while (curSpec.getType() != ValueType.NIL) {
            /* XXX Would a visitor make sense here? Or an iterator? */
            if (machine.isSymbol(curSpec)) {
                slurpyName = curSpec;
                break;
            } else if (curSpec.getType() == ValueType.CONS_REF) {
                var uncons = machine.uncons(curSpec);
                if (!machine.isSymbol(uncons.fst())) {
                    throw new EvalException("Found non-symbol " + uncons.fst() + " in argSpec: " + machine.valueToString(argSpec));
                }
                argNames.add(uncons.fst());
                curSpec = uncons.snd();
            } else {
                throw new EvalException("Cannot parse argSpec at: " + machine.valueToString(curSpec));
            }
        }
    }

    public @NotNull Assignments match(@NotNull HonsValue args) {
        var assignmentsMap = new HashMap<HonsValue, HonsValue>();
        var curArg = args;
        /* XXX would an iterator make sense here? */
        for (var argName : argNames) {
            HonsValue value = HonsValue.nil;
            if (curArg.getType() == ValueType.CONS_REF) {
                var uncons = machine.uncons(curArg);
                value = uncons.fst();
                curArg = uncons.snd();
            } else {
                curArg = HonsValue.nil;
            }
            assignmentsMap.put(argName, value);
        }
        if (slurpyName != null)
            assignmentsMap.put(slurpyName, curArg);
        return new Assignments(machine, assignmentsMap);
    }

    @Override
    public @NotNull String toString() {
        return "ArgSpec{" +
            "argNames=" + argNames.stream().map(machine::valueToString).toList() +
            ", slurpyName=" + machine.valueToString(slurpyName) +
            '}';
    }
    
    public @NotNull Set<HonsValue> getBoundVariables() {
        Set<HonsValue> names = new HashSet<>(argNames.size() + 1);

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
            var oldName = machine.symbolName(old);
            if (machine.fst(oldName).toSmallInt() == '$')
                continue;
            
            var newName = machine.symbolName(old);
            for (int idx = prefix.length() - 1; idx >= 0; idx--) {
                newName = machine.cons(HonsValue.fromSmallInt(prefix.charAt(idx)), newName);
            }
            oldNameToNewName.put(old, machine.makeSymbol(newName));
        }
        
        return new Assignments(machine, oldNameToNewName);
    }
}
