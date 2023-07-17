package uk.bs338.hashLisp.jproto.reader;

import org.jetbrains.annotations.NotNull;
import uk.bs338.hashLisp.jproto.IHeap;
import uk.bs338.hashLisp.jproto.IReader;
import uk.bs338.hashLisp.jproto.hons.HonsValue;
import uk.bs338.hashLisp.jproto.hons.Strings;
import uk.bs338.hashLisp.jproto.reader.Token.TokenType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static uk.bs338.hashLisp.jproto.Utilities.*;

public class Reader implements IReader<HonsValue> {
    public record ReadError(@NotNull String reason, Token token) {
    }
    
    private final IHeap<HonsValue> heap;
    private final ITokeniserFactory tokeniserFactory;
    private @NotNull List<ReadError> errors;
    private HonsValue stringSym = null;

    public Reader(IHeap<HonsValue> heap, ITokeniserFactory tokeniserFactory) {
        this.heap = heap;
        this.tokeniserFactory = tokeniserFactory;
        this.errors = new ArrayList<>();
    }
    
    private @NotNull HonsValue getStringSym() {
        if (stringSym == null)
            stringSym = heap.makeSymbol("*string");
        return stringSym;
    }
    
    private void addError(@NotNull String reason, Token token) {
        errors.add(new ReadError(reason, token));
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
            addError("Failed to parse after HASH: ", token);
            return Optional.empty();
        } else if (token.getType() == TokenType.STRING) {
            try {
                var string = Strings.unescapeString(token.getToken());
                return Optional.of(heap.cons(getStringSym(), stringAsList(heap, string)));
            }
            catch (Exception e) {
                addError("Failed to parse STRING token due to exception: " + e, token);
                return Optional.empty();
            }
        } else if (token.getType() == TokenType.OPEN_PARENS) {
            var rv = readListAfterOpenParens(tokeniser);
            if (rv.isEmpty())
                addError("Failed to parse list started at: ", token);
            return rv;
        } else {
            addError("Failed to parse at: ", token);
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
                    addError("Failed parsing after dot: ", token);
                    return snd;
                }
                if (listContents.isEmpty()) {
                    addError("Dot appeared at start of list: ", token);
                    return Optional.empty();
                }
                listContents.add(snd.get());
                Token token2 = tokeniser.next();
                if (token2.getType() != TokenType.CLOSE_PARENS) {
                    addError("Not a closing parenthesis in list after dot: ", token2);
                    return Optional.empty();
                }
                return Optional.of(makeListWithDot(heap, listContents.toArray(new HonsValue[]{})));
            }
            else {
                Optional<HonsValue> interpretation = interpretToken(tokeniser, token);
                if (interpretation.isEmpty()) {
                    addError("Stopped parsing list at token: ", token);
                    return interpretation;
                }
                listContents.add(interpretation.get());
            }
        }

        addError("Ran out of tokens inside list!", null);
        return Optional.empty();
    }

    private @NotNull Optional<HonsValue> readOneValue(@NotNull Iterator<Token> tokeniser) {
        if (!tokeniser.hasNext())
            return Optional.empty();
        
        Token token = tokeniser.next();
        
        return interpretToken(tokeniser, token);
    }
    
    protected <T> T collectErrors(@NotNull List<ReadError> errors, @NotNull Supplier<T> supplier) {
        var oldErrors = this.errors;
        this.errors = errors;
        T retval;
        try {
            retval = supplier.get();
        }
        finally {
            this.errors = oldErrors;
        }
        return retval;
    }
    
    public @NotNull ReadResult<HonsValue> read(@NotNull String str) {
        Tokeniser tokeniser = tokeniserFactory.createTokeniser(str);

        var errors = new ArrayList<ReadError>();
        var value = collectErrors(errors, () -> {
            var retval = readOneValue(tokeniser);
            /* if we read something, eat any whitespace after it */
            if (retval.isPresent())
                tokeniser.eatWhitespace();
            return retval;
        });
        
        if (value.isPresent()) {
            return ReadResult.successfulRead(tokeniser.getRemaining(), value.get());
        }
        else {
            /* notice we return str, not remaining, because we ate tokens but couldn't digest them */
            StringBuilder message = new StringBuilder();
            for (var error : errors) {
                message.append(error.reason);
                if (error.token != null) {
                    message.append(error.token.getPositionAsString());
                    message.append("\n");
                    
                    var beforeStartPos = str.substring(0, error.token.getStartPos());
                    var betweenPos = str.substring(error.token.getStartPos(), error.token.getEndPos());
                    var afterEndPos = str.substring(error.token.getEndPos());
                    message.append("  |").append(beforeStartPos).append(">*>").append(betweenPos).append("<*<").append(afterEndPos).append("|");
                }
                message.append("\n");
            }
            return ReadResult.failedRead(str, message.toString());
        }
    }
}
