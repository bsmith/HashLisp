package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public final class Strings {
    private Strings() {
        throw new AssertionError("No Strings instances for you!");
    }
    
    /* this used to use an IntStream, but a StringBuilder seems simpler */
    private static void escapeChar(int ch, StringBuilder builder) {
        /* Java backslash sequences are \t, \b, \n, \r, \f, \', \", \\ */
        switch (ch) {
            case '\t' -> builder.append("\\t");
            case '\b' -> builder.append("\\b");
            case '\n' -> builder.append("\\n");
            case '\r' -> builder.append("\\r");
            case '\f' -> builder.append("\\f");
            case '\'' -> builder.append("\\'");
            case '"' -> builder.append("\\\"");
            case '\\' -> builder.append("\\\\");
            default -> {
                /* NB. doesn't quote 'non-printable' per se, but covers a lot of stuff */
                if (Character.isISOControl(ch) || !Character.isBmpCodePoint(ch))
                    builder.append(String.format("\\u{%x}", ch));
                else
                    builder.appendCodePoint(ch);
            }
        };
    }

    public static @NotNull String quoteString(@NotNull String str) {
        /* Slow(?) but elegant */
        StringBuilder builder = new StringBuilder(str.length() * 2 + 2);
        builder.append('"');
        str.codePoints().forEach(ch -> escapeChar(ch, builder));
        builder.append('"');
        return builder.toString();
    }
    
    /* returns the number of chars consumed */
    private static int interpretEscapeChar(@NotNull String rest, @NotNull StringBuilder builder) {
        /* Java backslash sequences are \t, \b, \n, \r, \f, \', \", \\ */
        /* We add \\u{ABCDE} where ABCDE are hex characters (with length 1 to 5) */
        if (rest.charAt(0) == 'u') {
            var closingPos = rest.indexOf('}');
            if (rest.charAt(1) != '{' || closingPos == -1)
                throw new IllegalArgumentException("malformed \\u escape");
            /* throws exception on parse error */
            int codepoint = Integer.parseInt(rest, 2, closingPos, 16);
            builder.appendCodePoint(codepoint);
            return closingPos + 1;
        }
        
        builder.append(switch (rest.charAt(0)) {
            case 't' -> "\t";
            case 'b' -> "\b";
            case 'n' -> "\n";
            case 'r' -> "\r";
            case 'f' -> "\f";
            case '\'' -> "'";
            case '"' -> "\"";
            case '\\' -> "\\";
            default ->
                /* We're liberal here and return accept anything */
                rest.charAt(0);
        });
        return 1;
    }
    
    public static @NotNull String unescapeString(@NotNull String str) {
        StringBuilder builder = new StringBuilder(str.length());
        int pos = 0;
        while (pos < str.length()) {
            var posFirstBackslash = str.indexOf('\\', pos);
            if (posFirstBackslash == -1)
                break;
            builder.append(str, pos, posFirstBackslash);
            var charsConsumed = interpretEscapeChar(str.substring(posFirstBackslash + 1), builder);
            pos = posFirstBackslash + charsConsumed + 1;
        }
        if (pos < str.length())
            builder.append(str.substring(pos));
        return builder.toString();
    }
}
