package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;
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
    
    public static @NotNull ITokeniserFactory getFactory(CharClassifier charClassifier) {
        return str -> new Tokeniser(str, charClassifier);
    }
    
    public @NotNull String getRemaining() {
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

    public void eatExceptClasses(@NotNull EnumSet<CharClass> stopAt) {
        while (!isAtEnd()) {
            var charClass = classifyFirstChar();
            charClass.retainAll(stopAt);
            if (charClass.isEmpty())
                advancePosition();
            else
                break;
        }
    }
    
    /* Eat both whitespace and comments */
    public void eatWhitespace() {
        while (!isAtEnd()) {
            eatClass(CharClass.WHITESPACE);
            if (!isAtEnd() && classifyFirstChar().contains(CharClass.LINE_COMMENT_CHAR)) {
                /* consume the LINE_COMMENT_CHAR */
                advancePosition();
                /* consume characters that don't end a line */
                eatExceptClasses(EnumSet.of(CharClass.END_OF_LINE_CHARS));
                /* eat more whitespace! */
                /* we're assuming that WHITESPACE includes END_OF_LINE_CHARS */
            }
            else {
                break;
            }
        }
    }
    
    @Override
    public boolean hasNext() {
        eatWhitespace();
        return !isAtEnd();
    }

    @Override
    public @NotNull Token next() {
        eatWhitespace();
        
        int tokenStartPos = startPos;
        int tokenStartOffset = curOffset;
        String tokenStr = null;
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
        else if (charClass.contains(CharClass.DOT_CHAR)) {
            type = TokenType.DOT;
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
        else if (charClass.contains(CharClass.STRING_QUOTE_CHAR)) {
            advancePosition();
            var specialChars = EnumSet.of(CharClass.STRING_ESCAPE_CHAR, CharClass.STRING_QUOTE_CHAR);
            int stringStartOffset = curOffset;
            while (!isAtEnd()) {
                eatExceptClasses(specialChars);
                var nextCharClass = classifyFirstChar();
                if (nextCharClass.contains(CharClass.STRING_ESCAPE_CHAR)) {
                    advancePosition(); /* eat the backslash */
                    advancePosition(); /* eat the char escaped */
                    /* continue */
                }
                else if (nextCharClass.contains(CharClass.STRING_QUOTE_CHAR)) {
                    /* end of string, break the loop */
                    if (!isAtEnd() && classifyFirstChar().contains(CharClass.STRING_QUOTE_CHAR)) {
                        type = TokenType.STRING;
                        /* the tokenStr is the string between the " characters */
                        tokenStr = source.subSequence(stringStartOffset, curOffset).toString();
                        advancePosition();
                    }
                    break;
                }
                else {
                    /* we stopped eating for some reason, break the loop */
                    /* this leaves type as UNKNOWN */
                    break;
                }
            }
        }
        else if (charClass.contains(CharClass.DIGIT_CHAR) || charClass.contains(CharClass.MINUS_SIGN)) {
            type = TokenType.DIGITS;
            advancePosition(); /* eat the first digit or minus sign */
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
                    eatExceptClasses(permittedNext);
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
        if (tokenStr == null) 
            tokenStr = source.subSequence(tokenStartOffset, curOffset).toString();
        return new Token(type, tokenStr, tokenStartPos, tokenEndPos);
    }
}
