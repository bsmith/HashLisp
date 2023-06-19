package uk.bs338.hashLisp.jproto.reader;

import java.util.ArrayList;
import java.util.EnumSet;

public class CharClassifier {
    enum CharClass {
        OPEN_PARENS,
        CLOSE_PARENS,
        SYMBOL_CHAR,
        DIGIT_CHAR,
        HASH_CHAR,
        COLON_CHAR,
        DOT_CHAR,
        WHITESPACE
    }
    
    private final static String symbolChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-?!$@";
    private final static String digits = "0123456789";
    private final static String whitespace = " \t\r\n";
    
    private final static int TABLE_SIZE = 256;
    private static ArrayList<EnumSet<CharClass>> classTable = null;
    
    public CharClassifier() {
        setupTables();
    }
    
    private void setupTables() {
        if (classTable != null)
            return;
        classTable = new ArrayList<>(TABLE_SIZE);
        for (int cp = 0; cp < TABLE_SIZE; cp++)
            classTable.add(cp, EnumSet.noneOf(CharClass.class));
        
        classTable.get('(').add(CharClass.OPEN_PARENS);
        classTable.get(')').add(CharClass.CLOSE_PARENS);
        classTable.get('#').add(CharClass.HASH_CHAR);
        classTable.get(':').add(CharClass.COLON_CHAR);
        classTable.get('.').add(CharClass.DOT_CHAR);
        symbolChars.codePoints().forEach(cp -> classTable.get(cp).add(CharClass.SYMBOL_CHAR));
        digits.codePoints().forEach(cp -> classTable.get(cp).add(CharClass.DIGIT_CHAR));
        whitespace.codePoints().forEach(cp -> classTable.get(cp).add(CharClass.WHITESPACE));
    }
    
    /* XXX rewrite this so that this is the more efficient impl! */
    public EnumSet<CharClass> classifyChar(int codepoint) {
        if (codepoint < TABLE_SIZE)
            return EnumSet.copyOf(classTable.get(codepoint));
        return EnumSet.noneOf(CharClass.class);
    }
    
    public EnumSet<CharClass> classifyChar(String ch) {
        return classifyChar(ch.codePointAt(0));
    }
}
