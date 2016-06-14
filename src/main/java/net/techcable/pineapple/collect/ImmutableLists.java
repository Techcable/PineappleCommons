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
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.techcable.pineapple.reflection.Reflection;

import static com.google.common.base.Preconditions.*;

@ParametersAreNonnullByDefault
public class ImmutableLists {
    @Nonnull
    public static <T, U> ImmutableList<U> transform(ImmutableList<T> list, Function<T, U> transformer) {
        ImmutableList.Builder<U> resultBuilder = builder(checkNotNull(list, "Null list").size());
        for (int i = 0; i < list.size(); i++) {
            T oldElement = list.get(i);
            U newElement = checkNotNull(transformer, "Null transformer").apply(oldElement);
            if (newElement == null) throw new NullPointerException("Transformer  " + transformer.getClass().getTypeName() + " returned null.");
            resultBuilder.add(newElement);
        }
        return resultBuilder.build();
    }

    //
    // Reflection and dark magic
    //

    private static final MethodHandle BUILDER_CONSTRUCTOR = Reflection.getConstructor(ImmutableList.Builder.class, int.class);

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> ImmutableList.Builder<T> builder(int size) {
        checkArgument(size >= 0, "Negative size %s", size);
        return BUILDER_CONSTRUCTOR != null ? (ImmutableList.Builder<T>) BUILDER_CONSTRUCTOR.invokeExact(size) : ImmutableList.builder();
    }
}
