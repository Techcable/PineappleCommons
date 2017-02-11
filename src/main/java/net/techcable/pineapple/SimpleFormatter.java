/**
 * The MIT License
 * Copyright (c) 2016 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.techcable.pineapple;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.annotation.MatchesPattern;
import javax.annotation.meta.TypeQualifierNickname;

import static com.google.common.base.Preconditions.*;

/**
 * Formats messages using a subset of {@link java.text.MessageFormat}.
 * <p>
 * MessageFormat takes a set of objects, and inserts their string representation into the format string at the appropriate places.
 * Any unused arguments are ignored, but insufficient arguments will trigger an error.
 * <h3>Format Strings</h3>
 * Format strings can specify to insert the next argument by using '{}'.
 * This will replace the brackets with the next argument.</p>
 * Format strings can specify to insert an argument with a certain index by using '{index}', where 'index' is the index of the argument.
 * This will <i>not</i> consume the next argument, and therefore has no effect on '{}'</p>
 * Format strings can insert a literal bracket, by escaping it with the '\' character (like '\{'.
 * Escape characters must also be prefixed, <i>or else they are ignored</i></p>
 *
 * @author Techcable
 */
public final class SimpleFormatter {
    private SimpleFormatter() {}
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Insert no arguments into the given format string
     * <p>Since there are no arguments, all this does is validate the format string.</p>
     *
     * @param format the format string
     * @return the formatted string
     */
    public static String format(@FormatPattern String format) { // This is basically just a check for a invalid format string
        checkNotNull(format, "Null format string");
        return format0(format, EMPTY_STRING_ARRAY, format.length());
    }

    /**
     * Insert the specified arguments into the given format string
     * <p>The arguments will be converted to strings using {@link Object#toString()}.
     * Null arguments are allowed, and are converted to the "null" string.</p>
     * <p>This is equivalent to calling {@link #format(String, String...)} with the objects converted to strings.</p>
     *
     * @param format the format string
     * @param args   the arguments to insert
     * @return the formatted string
     * @throws NullPointerException      if the format string is null
     * @throws NullPointerException      if the argument array is null
     * @throws IllegalArgumentException  if the format string isn't valid
     * @throws IndexOutOfBoundsException if one of the indexes isn't valid
     */
    public static String format(@FormatPattern String format, Object... args) {
        checkNotNull(format, "Null format string");
        checkNotNull(args, "Null argument array");
        if (args.length == 0) return format(format); // We don't need to work as hard when we have no arguments ;)
        int approximateSize = format.length();
        String[] argsAsStrings = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            String asString;
            if (arg == null) {
                asString = "null";
            } else if (arg instanceof Supplier) {
                asString = String.valueOf(((Supplier) arg).get());
            } else {
                asString = arg.toString();
            }
            checkNotNull(asString, "Argument at index %s returned null from toString()", i);
            approximateSize += asString.length();
            argsAsStrings[i] = asString;
        }
        return format0(format, argsAsStrings, approximateSize);
    }

    /**
     * Insert the specified argument strings into the given format string
     * <p>Calling {@link #format(String, Object...)} will produce the same result.</p>
     *
     * @param format the format string
     * @param args   the arguments to insert
     * @return the formatted string
     * @throws NullPointerException      if the format string is null
     * @throws NullPointerException      if the argument array is null
     * @throws NullPointerException      if any of the argument strings are null
     * @throws IllegalArgumentException  if the format string isn't valid
     * @throws IndexOutOfBoundsException if one of the indexes isn't valid
     */
    public static String format(@FormatPattern String format, String... args) {
        checkNotNull(format, "Null format");
        checkNotNull(args, "Null argument array");
        if (args.length == 0) return format(format); // We don't need to work as hard when we have no arguments ;)
        int approximateSize = format.length() + args.length * 20;
        return format0(format, args, approximateSize);
    }

    /**
     * Insert the specified argument strings into the given format string, specifying the approximate size of the resulting string.
     * <p>Calling {@link #format(String, String...)} will produce the same result, but may result in more allocations/copying.</p>
     *
     * @param approximateSize the approximate size of the resulting string
     * @param format the format string
     * @param args   the arguments to insert
     * @return the formatted string
     * @throws NullPointerException      if the format string is null
     * @throws NullPointerException      if the argument array is null
     * @throws NullPointerException      if any of the argument strings are null
     * @throws IllegalArgumentException  if the format string isn't valid
     * @throws IndexOutOfBoundsException if one of the indexes isn't valid
     */
    public static String format(int approximateSize, @FormatPattern String format, String... args) {
        checkNotNull(format, "Null format");
        checkNotNull(args, "Null argument array");
        checkArgument(approximateSize >= 0, "Negative approximate size %s", approximateSize);
        if (args.length == 0) return format(format); // We don't need to work as hard when we have no arguments ;)
        return format0(format, args, approximateSize);
    }

    private static String format0(@FormatPattern String format, String[] args, int approximateSize) {
        assert approximateSize >= 0 : "Negative approximate size " + approximateSize;
        char[] resultBuilder = new char[approximateSize];
        int resultSize = 0;
        int nextArg = 0;
        final int formatLength = format.length();
        for (int i = 0; i < formatLength; i++) {
            char c = format.charAt(i);
            switch (c) {
                case '{':
                    int argumentIndex;
                    if (++i >= formatLength) {
                        throw new IllegalArgumentException("Unescaped '{' at end of string: " + format);
                    }
                    c = format.charAt(i);
                    if (c == '}') {
                        argumentIndex = nextArg++;
                    } else if (c >= '0' && c <= '9') {
                        int value = 0;
                        do {
                            int digitValue = c - '0';
                            assert digitValue >= 0 : "Digit " + c + " results in negative value: " + digitValue;
                            assert digitValue <= 9 : "Digit " + c + " results in value greater than 9 " + digitValue;
                            value = value * 10 + digitValue;
                        } while ((c = format.charAt(++i)) >= '0' && c <= '9');
                        if (c != '}') throw new IllegalArgumentException("Invalid character: " + c + " at " + i);
                        argumentIndex = value;
                    } else {
                        throw new IllegalArgumentException("Invalid character: " + c + " at " + i);
                    }
                    assert argumentIndex >= 0 : "Negative argument index: " + argumentIndex;
                    if (argumentIndex >= args.length) {
                        throw new IndexOutOfBoundsException("Invalid argument index " + argumentIndex + " at character " + i);
                    }
                    String arg = args[argumentIndex];
                    assert arg != null : "Null argument at index " + i;
                    int argLength = arg.length();
                    int neededLength = resultSize + argLength + formatLength - i;
                    if (args.length < neededLength) {
                        resultBuilder = Arrays.copyOf(resultBuilder, neededLength);
                    }
                    arg.getChars(0, argLength, resultBuilder, resultSize);
                    resultSize += argLength;
                    break;
                case '%':
                    throw new IllegalArgumentException("Unescaped '%' at character " + c + " not permitted to avoid ambiguity with String.format!");
                case '\\':
                    c = format.charAt(++i); // Treat next char normally, and skip any special processing it has
                    // fallthrough the char to the default/literal handler
                default:
                    resultBuilder[resultSize++] = c;
            }
        }
        return new String(resultBuilder, 0, resultSize);
    }

    @TypeQualifierNickname
    @MatchesPattern("(?:[^{\\}%\\\\]|\\\\.|\\{\\d*\\})*")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    public @interface FormatPattern {}

}
