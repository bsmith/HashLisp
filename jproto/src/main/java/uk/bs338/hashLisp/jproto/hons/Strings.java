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
            default ->
                /* NB. doesn't quote non-printable */
                IntStream.of(ch);
        };
    }

    public static @NotNull String quoteString(@NotNull String str) {
        /* Slow(?) but elegant */
        var escaped = str.codePoints().flatMap(Strings::escapeChar);
        int[] result = java.util.stream.IntStream.concat(IntStream.of('"'), IntStream.concat(escaped, IntStream.of('"'))).toArray();
        return new String(result, 0, result.length);
    }

    public static @NotNull String interpretEscapedChar(@NotNull String ch) {
        /* Java backslash sequences are \t, \b, \n, \r, \f, \', \", \\ */
        return switch (ch) {
            case "t" -> "\t";
            case "b" -> "\b";
            case "n" -> "\n";
            case "r" -> "\r";
            case "f" -> "\f";
            case "'" -> "'";
            case "\"" -> "\"";
            case "\\" -> "\\";
            default ->
                /* We're liberal here and return accept anything */
                ch;
        };
    }
    
    public static @NotNull CharSequence unescapeString(@NotNull String str) {
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        while (pos < str.length()) {
            var posFirstBackslash = str.indexOf('\\', pos);
            if (posFirstBackslash == -1)
                break;
            builder.append(str, pos, posFirstBackslash);
            var ch = interpretEscapedChar(str.substring(posFirstBackslash + 1, posFirstBackslash + 2));
            builder.append(ch);
            pos = posFirstBackslash + 2;
        }
        if (pos < str.length())
            builder.append(str.substring(pos));
        return builder.toString();
    }
}
