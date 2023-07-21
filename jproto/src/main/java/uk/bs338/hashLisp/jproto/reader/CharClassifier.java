package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        LINE_COMMENT_CHAR,
        STRING_QUOTE_CHAR,
        STRING_ESCAPE_CHAR,
        END_OF_LINE_CHARS,
        MINUS_SIGN,
        WHITESPACE
    }
    
    private final static String symbolChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-?!$@*";
    private final static String digits = "0123456789";
    private final static String whitespace = " \t\r\n";
    
    private final static int TABLE_SIZE = 256;
    private static @Nullable ArrayList<EnumSet<CharClass>> classTable;
    
    public CharClassifier() {
        setupTables();
    }
    
    private static void setupTables() {
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
        classTable.get(';').add(CharClass.LINE_COMMENT_CHAR);
        classTable.get('"').add(CharClass.STRING_QUOTE_CHAR);
        classTable.get('\\').add(CharClass.STRING_ESCAPE_CHAR);
        classTable.get('\n').add(CharClass.END_OF_LINE_CHARS);
        classTable.get('\r').add(CharClass.END_OF_LINE_CHARS);
        classTable.get('-').add(CharClass.MINUS_SIGN);
        symbolChars.codePoints().forEach(cp -> classTable.get(cp).add(CharClass.SYMBOL_CHAR));
        digits.codePoints().forEach(cp -> classTable.get(cp).add(CharClass.DIGIT_CHAR));
        whitespace.codePoints().forEach(cp -> classTable.get(cp).add(CharClass.WHITESPACE));
    }
    
    public EnumSet<CharClass> classifyChar(int codepoint) {
        assert classTable != null;
        if (codepoint < TABLE_SIZE)
            return EnumSet.copyOf(classTable.get(codepoint));
        return EnumSet.noneOf(CharClass.class);
    }
    
    public EnumSet<CharClass> classifyChar(@NotNull String ch) {
        return classifyChar(ch.codePointAt(0));
    }
}
