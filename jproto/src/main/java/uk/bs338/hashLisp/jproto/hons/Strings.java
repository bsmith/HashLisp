package uk.bs338.hashLisp.jproto.hons;

import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public final class Strings {
    private Strings() {
        throw new AssertionError("No Strings instances for you!");
    }
    
    private static @NotNull IntStream escapeChar(int ch) {
        /* Java backslash sequences are \t, \b, \n, \r, \f, \', \", \\ */
        return switch (ch) {
            case '\t' -> IntStream.of('\\', 't');
            case '\b' -> IntStream.of('\\', 'b');
            case '\n' -> IntStream.of('\\', 'n');
            case '\r' -> IntStream.of('\\', 'r');
            case '\f' -> IntStream.of('\\', 'f');
            case '\'' -> IntStream.of('\\', '\'');
            case '"' -> IntStream.of('\\', '"');
            case '\\' -> IntStream.of('\\', '\\');
            default -> {
                /* NB. doesn't quote 'non-printable' per se, but covers a lot of stuff */
                if (Character.isISOControl(ch) || !Character.isBmpCodePoint(ch))
                    yield String.format("\\u{%x}", ch).codePoints();
                yield IntStream.of(ch);
            }
        };
    }

    public static @NotNull String quoteString(@NotNull String str) {
        /* Slow(?) but elegant */
        var escaped = str.codePoints().flatMap(Strings::escapeChar);
        int[] result = java.util.stream.IntStream.concat(IntStream.of('"'), IntStream.concat(escaped, IntStream.of('"'))).toArray();
        return new String(result, 0, result.length);
    }

    private record InterpretedEscapeChar(String interpreted, int charsConsumed) { }

    private static @NotNull InterpretedEscapeChar interpretEscapeChar(@NotNull String ch) {
        /* Java backslash sequences are \t, \b, \n, \r, \f, \', \", \\ */
        /* We add \\u{ABCDE} where ABCDE are hex characters (with length 1 to 5) */
        if (ch.charAt(0) == 'u') {
            assert ch.charAt(1) == '{';
            var closingPos = ch.indexOf('}');
            assert closingPos != -1;
            /* throws exception on parse error */
            int codepoint = Integer.parseInt(ch, 2, closingPos, 16);
            return new InterpretedEscapeChar(new String(new int[]{codepoint}, 0, 1), closingPos + 1);
        }
        
        var singleChar = switch (ch.charAt(0)) {
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
                ch;
        };
        return new InterpretedEscapeChar(singleChar, 1);
    }
    
    public static @NotNull CharSequence unescapeString(@NotNull String str) {
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        while (pos < str.length()) {
            var posFirstBackslash = str.indexOf('\\', pos);
            if (posFirstBackslash == -1)
                break;
            builder.append(str, pos, posFirstBackslash);
            var result = interpretEscapeChar(str.substring(posFirstBackslash + 1));
            builder.append(result.interpreted);
            pos = posFirstBackslash + result.charsConsumed + 1;
        }
        if (pos < str.length())
            builder.append(str.substring(pos));
        return builder.toString();
    }
}
