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

import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

/**
 * A set of sanity/validity checks that lazily evaluate arguments (using {@link Supplier}).
 * Uses the message format of {@link SimpleFormatter}.
 * Can be used as a replacement/supplement to {@link Preconditions}.
 */
@NonnullByDefault
public final class LazyPreconditions {
    private LazyPreconditions() {
    }

    public static void checkState(boolean expression) {
        Preconditions.checkState(expression);
    }

    public static void checkState(boolean expression, String message) {
        Preconditions.checkState(expression, message);
    }

    public static void checkState(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalStateException(messageSupplier.get());
        }
    }

    public static void checkState(boolean expression, @SimpleFormatter.FormatPattern String format, @Nullable Object arg) {
        checkState(expression, () -> SimpleFormatter.format(format, arg));
    }

    public static void checkState(boolean expression, @SimpleFormatter.FormatPattern String format, Supplier<?> argumentSupplier) {
        checkState(expression, () -> {
            Supplier<?> supplier = checkNotNull(argumentSupplier, "Null supplier");
            Object arg = supplier.get();
            String argString = checkNotNull(arg, "Null argument returned by supplier").toString();
            return SimpleFormatter.format(format, argString);
        });
    }

    public static void checkState(boolean expression, @SimpleFormatter.FormatPattern String format, Supplier<?>... argumentSuppliers) {
        checkState(expression, () -> {
            Preconditions.checkNotNull(argumentSuppliers, "Null argument suppliers");
            String[] args = new String[argumentSuppliers.length];
            for (int index = 0; index < argumentSuppliers.length; index++) {
                Supplier<?> supplier = argumentSuppliers[index];
                checkNotNull(supplier, "Null supplier at index {}", supplier);
                final Object result = supplier.get();
                checkNotNull(result, "Supplier at index {} returned null result", format);
                args[index] = result.toString();
            }
            return SimpleFormatter.format(format, args);
        });
    }

    public static void checkState(boolean expression, @SimpleFormatter.FormatPattern String format, Object... args) {
        checkState(expression, () -> SimpleFormatter.format(format, args));
    }

    public static void checkArgument(boolean expression) {
        Preconditions.checkArgument(expression);
    }

    public static void checkArgument(boolean expression, String message) {
        Preconditions.checkArgument(expression, message);
    }

    public static void checkArgument(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
    }

    public static void checkArgument(boolean expression, @SimpleFormatter.FormatPattern String format, @Nullable Object arg) {
        checkArgument(expression, () -> SimpleFormatter.format(format, arg));
    }

    public static void checkArgument(boolean expression, @SimpleFormatter.FormatPattern String format, Supplier<?> argumentSupplier) {
        checkArgument(expression, () -> {
            Supplier<?> supplier = checkNotNull(argumentSupplier, "Null supplier");
            Object arg = supplier.get();
            String argString = checkNotNull(arg, "Null argument returned by supplier").toString();
            return SimpleFormatter.format(format, argString);
        });
    }

    public static void checkArgument(boolean expression, @SimpleFormatter.FormatPattern String format, Supplier<?>... argumentSuppliers) {
        checkArgument(expression, () -> {
            Preconditions.checkNotNull(argumentSuppliers, "Null argument suppliers");
            String[] args = new String[argumentSuppliers.length];
            for (int index = 0; index < argumentSuppliers.length; index++) {
                Supplier<?> supplier = argumentSuppliers[index];
                checkNotNull(supplier, "Null supplier at index {}", supplier);
                final Object result = supplier.get();
                checkNotNull(result, "Supplier at index {} returned null result", format);
                args[index] = result.toString();
            }
            return SimpleFormatter.format(format, args);
        });
    }

    public static void checkArgument(boolean expression, @SimpleFormatter.FormatPattern String format, Object... args) {
        checkArgument(expression, () -> SimpleFormatter.format(format, args));
    }

    public static <T> T checkNotNull(@Nullable T obj, int i) {
        if (obj == null) {
            throw new NullPointerException(Integer.toString(i));
        } else {
            return obj;
        }
    }

    @Nonnull
    public static <T> T checkNotNull(@Nullable T obj, @SimpleFormatter.FormatPattern String format, int argument) {
        if (obj == null) {
            throw new NullPointerException(SimpleFormatter.format(format, argument));
        } else {
            return obj;
        }
    }

    @Nonnull
    public static <T> T checkNotNull(@Nullable T obj, @SimpleFormatter.FormatPattern String format, Object argument) {
        if (obj == null) {
            throw new NullPointerException(SimpleFormatter.format(format, argument));
        } else {
            return obj;
        }
    }

    @Nonnull
    public static <T> T checkNotNull(@Nullable T obj, Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new NullPointerException(checkNotNull(messageSupplier, "Null supplier").get());
        } else {
            return obj;
        }
    }

    @Nonnull
    public static <T> T checkNotNull(@Nullable T obj, String errorMessage) {
        return Objects.requireNonNull(obj, errorMessage);
    }
}
