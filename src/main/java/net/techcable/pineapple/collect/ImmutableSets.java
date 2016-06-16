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
package net.techcable.pineapple.collect;

import lombok.*;

import java.lang.invoke.MethodHandle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.techcable.pineapple.reflection.Reflection;

import static com.google.common.base.Preconditions.*;

@ParametersAreNonnullByDefault
public class ImmutableSets {
    @Nonnull
    public static <T, U> ImmutableSet<U> transform(Set<T> set, Function<T, U> transformer) {
        ImmutableSet.Builder<U> resultBuilder = builder(checkNotNull(set, "Null list").size());
        set.forEach((oldElement) -> {
            U newElement = checkNotNull(transformer, "Null transformer").apply(oldElement);
            if (newElement == null) throw new NullPointerException("Transformer  " + transformer.getClass().getTypeName() + " returned null.");
            resultBuilder.add(newElement);
        });
        return resultBuilder.build();
    }

    @Nonnull
    public static <T, U> ImmutableSet<U> transform(ImmutableSet<T> set, Function<T, U> transformer) {
        ImmutableSet.Builder<U> resultBuilder = builder(checkNotNull(set, "Null set").size());
        ImmutableList<T> list = set.asList();
        for (int i = 0; i < list.size(); i++) {
            T oldElement = list.get(i);
            U newElement = checkNotNull(transformer, "Null transformer").apply(oldElement);
            if (newElement == null) throw new NullPointerException("Transformer  " + transformer.getClass().getTypeName() + " returned null.");
            resultBuilder.add(newElement);
        }
        return resultBuilder.build();
    }

    public static <T> void forEach(ImmutableSet<T> set, Consumer<T> consumer) {
        ImmutableList<T> list = checkNotNull(set, "Null set").asList();
        for (int i = 0; i < list.size(); i++) {
            consumer.accept(list.get(i));
        }
    }

    //
    // Reflection and dark magic
    //

    private static final MethodHandle BUILDER_CONSTRUCTOR = Reflection.getConstructor(ImmutableSet.Builder.class, int.class);

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> ImmutableSet.Builder<T> builder(int size) {
        checkArgument(size >= 0, "Negative size %s", size);
        return BUILDER_CONSTRUCTOR != null ? (ImmutableSet.Builder<T>) BUILDER_CONSTRUCTOR.invokeExact(size) : ImmutableSet.builder();
    }
}
