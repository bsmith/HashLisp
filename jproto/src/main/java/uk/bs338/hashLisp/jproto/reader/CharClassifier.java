package uk.bs338.hashLisp.jproto.reader;

import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.EnumSet;

public class CharClassifier {
    private final static String symbolChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-?!$@";
    private final static String digits = "0123456789";
    private final static String whitespace = " \t\r\n";
    
    enum CharClass {
        OPEN_PARENS,
        CLOSE_PARENS,
        SYMBOL_CHAR,
        DIGIT_CHAR,
        HASH_CHAR,
        WHITESPACE;
    }

    EnumSet<CharClass> classifyChar(String ch)  {
        var set = EnumSet.noneOf(CharClass.class);
        if (ch.startsWith("("))
            set.add(CharClass.OPEN_PARENS);
        if (ch.startsWith(")"))
            set.add(CharClass.CLOSE_PARENS);
        if (symbolChars.contains(ch.substring(0, 1)))
            set.add(CharClass.SYMBOL_CHAR);
        if (digits.contains(ch.substring(0, 1)))
            set.add(CharClass.DIGIT_CHAR);
        if (ch.startsWith("#"))
            set.add(CharClass.HASH_CHAR);
        if (whitespace.contains(ch.substring(0, 1)))
            set.add(CharClass.WHITESPACE);
        return set;
    }
}
