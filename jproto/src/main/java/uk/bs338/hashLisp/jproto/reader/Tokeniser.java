package uk.bs338.hashLisp.jproto.reader;

import uk.bs338.hashLisp.jproto.reader.CharClassifier.CharClass;
import uk.bs338.hashLisp.jproto.reader.Token.TokenType;

import java.util.EnumSet;
import java.util.Iterator;

public class Tokeniser implements Iterator<Token> {
    private final CharClassifier charClassifier;
    private final CharSequence source;
    /* the offset in characters into source */
    private int curOffset;
    /* the position in codepoints into source */
    private int startPos;
    
    public Tokeniser(CharSequence source, CharClassifier charClassifier) {
        this.charClassifier = charClassifier;
        this.source = source;
        this.curOffset = 0;
        this.startPos = 0;
    }
    
    public String getRemaining() {
        return source.subSequence(curOffset, source.length()).toString();
    }
    
    public int getStartPos() {
        return this.startPos;
    }
    
    public boolean isAtEnd() {
        return curOffset >= source.length();
    }
    
    public int getFirstCharAsCodepoint() {
        return Character.codePointAt(source, this.curOffset);
    }
    
    public EnumSet<CharClass> classifyFirstChar() {
        return charClassifier.classifyChar(getFirstCharAsCodepoint());
    }
    
    public void advancePosition() {
        int codepointLen = Character.charCount(getFirstCharAsCodepoint());
        curOffset += codepointLen;
        startPos++;
    }
    
    public void eatClass(CharClass charClass) {
        while (!isAtEnd() && classifyFirstChar().contains(charClass)) {
            advancePosition();
        }
    }

    public void eatClasses(EnumSet<CharClass> eatThem) {
        while (!isAtEnd()) {
            var charClass = classifyFirstChar();
            charClass.retainAll(eatThem);
            if (!charClass.isEmpty())
                advancePosition();
            else
                break;
        }
    }
    
    public void eatWhitespace() {
        eatClass(CharClass.WHITESPACE);
    }
    
    @Override
    public boolean hasNext() {
        eatWhitespace();
        return !isAtEnd();
    }

    @Override
    public Token next() {
        eatWhitespace();
        
        int tokenStartPos = startPos;
        int tokenStartOffset = curOffset;
        var charClass = classifyFirstChar();
        var type = TokenType.UNKNOWN;
        
        if (charClass.contains(CharClass.HASH_CHAR)) {
            type = TokenType.HASH;
            advancePosition();
        }
        else if (charClass.contains(CharClass.COLON_CHAR)) {
            type = TokenType.COLON;
            advancePosition();
        }
        else if (charClass.contains(CharClass.OPEN_PARENS)) {
            type = TokenType.OPEN_PARENS;
            advancePosition();
        }
        else if (charClass.contains(CharClass.CLOSE_PARENS)) {
            type = TokenType.CLOSE_PARENS;
            advancePosition();
        }
        else if (charClass.contains(CharClass.DIGIT_CHAR)) {
            type = TokenType.DIGITS;
            eatClass(CharClass.DIGIT_CHAR);
            /* must be followed by whitespace or parens or end */
            var permittedNext = EnumSet.of(CharClass.WHITESPACE, CharClass.OPEN_PARENS, CharClass.CLOSE_PARENS);
            if (!isAtEnd()) {
                var nextCharClass = classifyFirstChar();
                nextCharClass.retainAll(permittedNext);
                if (nextCharClass.isEmpty()) {
                    /* eat until we find a permitted character */
                    System.out.println(nextCharClass);
                    type = TokenType.UNKNOWN;
                    eatClasses(EnumSet.complementOf(permittedNext));
                }
            }
        }
        else if (charClass.contains(CharClass.SYMBOL_CHAR)) {
            type = TokenType.SYMBOL;
            eatClass(CharClass.SYMBOL_CHAR);
        }
        else
            advancePosition();
        
        int tokenEndPos = startPos;
        String tokenStr = source.subSequence(tokenStartOffset, curOffset).toString();
        return new Token(type, tokenStr, tokenStartPos, tokenEndPos);
    }
}
