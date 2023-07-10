package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.hons.HonsHeap;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.reader.Token.TokenType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class Reader {
    private final HonsHeap heap;
    private final ITokeniserFactory tokeniserFactory;
    private @NotNull List<String> errors;

    public Reader(HonsHeap heap, ITokeniserFactory tokeniserFactory) {
        this.heap = heap;
        this.tokeniserFactory = tokeniserFactory;
        this.errors = new ArrayList<>();
    }
    
    private @NotNull Optional<HonsValue> interpretToken(@NotNull Iterator<Token> tokeniser, @NotNull Token token) {
        if (token.getType() == TokenType.DIGITS) {
            return Optional.of(HonsValue.fromSmallInt(token.getTokenAsInt()));
        } else if (token.getType() == TokenType.SYMBOL) {
            return Optional.of(heap.makeSymbol(token.getToken()));
        } else if (token.getType() == TokenType.HASH) {
            Token token2 = tokeniser.next();
            if (token2.getType() == TokenType.DIGITS && token2.getTokenAsInt() == 0) {
                return Optional.of(HonsValue.nil);
            }
            errors.add("Failed to parse after HASH: " + token);
            return Optional.empty();
        } else if (token.getType() == TokenType.STRING) {
            return Optional.of(stringAsList(heap, token.getToken()));
        } else if (token.getType() == TokenType.OPEN_PARENS) {
            var rv = readListAfterOpenParens(tokeniser);
            if (rv.isEmpty())
                errors.add("Failed to parse list started at: " + token);
            return rv;
        } else {
            errors.add("Failed to parse at: " + token);
            return Optional.empty();
        }
    }

    private @NotNull Optional<HonsValue> readListAfterOpenParens(@NotNull Iterator<Token> tokeniser) {
        ArrayList<HonsValue> listContents = new ArrayList<>();
        
        while (tokeniser.hasNext()) {
            Token token = tokeniser.next();
            
            if (token.getType() == TokenType.CLOSE_PARENS) {
                if (listContents.isEmpty())
                    return Optional.of(HonsValue.nil);
                return Optional.of(makeList(heap, listContents.toArray(new HonsValue[]{})));
            } else if (token.getType() == TokenType.DOT) {
                Optional<HonsValue> snd = readOneValue(tokeniser);
                if (snd.isEmpty()) {
                    errors.add("Failed parsing after dot: " + token);
                    return snd;
                }
                if (listContents.isEmpty()) {
                    errors.add("Dot appeared at start of list: " + token);
                    return Optional.empty();
                }
                listContents.add(snd.get());
                Token token2 = tokeniser.next();
                if (token2.getType() != TokenType.CLOSE_PARENS) {
                    errors.add("Not a closing parenthesis in list after dot: " + token2);
                    return Optional.empty();
                }
                return Optional.of(makeListWithDot(heap, listContents.toArray(new HonsValue[]{})));
            }
            else {
                Optional<HonsValue> interpretation = interpretToken(tokeniser, token);
                if (interpretation.isEmpty()) {
                    errors.add("Stopped parsing list at token: " + token);
                    return interpretation;
                }
                listContents.add(interpretation.get());
            }
        }
        
        errors.add("Ran out of tokens inside list!");
        return Optional.empty();
    }

    private Optional<HonsValue> readOneValue(@NotNull Iterator<Token> tokeniser) {
        if (!tokeniser.hasNext())
            return Optional.empty();
        
        Token token = tokeniser.next();
        
        return interpretToken(tokeniser, token);
    }
    
    public @NotNull ReadResult read(@NotNull String str) {
        Tokeniser tokeniser = tokeniserFactory.createTokeniser(str);

        /* XXX something nicer */
        var oldErrors = this.errors;
        this.errors = new ArrayList<>();
        var value = readOneValue(tokeniser);
        /* if we read something, eat any whitespace after it */
        if (value.isPresent())
            tokeniser.eatWhitespace();
        var errors = this.errors;
        this.errors = oldErrors;
        
        if (value.isPresent()) {
            return ReadResult.successfulRead(tokeniser.getRemaining(), value.get());
        }
        else {
            /* notice we return str, not remaining, because we ate tokens but couldn't digest them */
            StringBuilder message = new StringBuilder();
            message.append("Failed to readOneValue\n");
            for (var error : errors) {
                message.append(error);
                message.append("\n");
            }
            return ReadResult.failedRead(str, message.toString() );
        }
    }
}
