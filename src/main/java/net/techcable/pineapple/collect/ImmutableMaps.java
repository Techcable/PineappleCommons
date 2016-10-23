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

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;


import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.techcable.pineapple.SneakyThrow;
import net.techcable.pineapple.reflection.PineappleField;
import net.techcable.pineapple.reflection.Reflection;
import static com.google.common.base.Preconditions.*;

@ParametersAreNonnullByDefault
public class ImmutableMaps {

    public static <K1, K2, V> ImmutableMap<K2, V> transformKeys(ImmutableMap<K1, V> original, Function<K1, K2> keyTransformer) {
        return transform(original, keyTransformer, Function.identity());
    }

    @Nonnull
    public static <K1, K2, V1, V2> ImmutableMap<K2, V2> transform(ImmutableMap<K1, V1> original, Function<K1, K2> keyTransformer, Function<V1, V2> valueTransformer) {
        ImmutableMap.Builder<K2, V2> resultBuilder = builder(checkNotNull(original, "Null map").size());
        forEach(original, (originalKey, originalValue) -> {
            K2 newKey = checkNotNull(keyTransformer, "Null key transformer").apply(originalKey);
            V2 newValue = checkNotNull(valueTransformer, "Null value transformer").apply(originalValue);
            resultBuilder.put(newKey, newValue);
        });
        return resultBuilder.build();
    }

    //
    // Dark Magic
    //

    @SuppressWarnings("rawtypes")
    private static final Class<? extends ImmutableMap> REGULAR_IMMUTABLE_MAP_CLASS = Reflection.getClass("com.google.common.collect.RegularImmutableMap", ImmutableMap.class);
    private static final MethodHandle BUILDER_CONSTRUCTOR = Reflection.getConstructor(ImmutableMap.Builder.class, int.class);
    @SuppressWarnings("rawtypes")
    private static final PineappleField<ImmutableMap, Map.Entry[]> ENTRIES_ARRAY_FIELD = REGULAR_IMMUTABLE_MAP_CLASS != null ? PineappleField.create(REGULAR_IMMUTABLE_MAP_CLASS, "entries", Map.Entry[].class) : null;

    @Nonnull
    public static <K, V> ImmutableMap.Builder<K, V> builder(int initialCapacity) {
        checkArgument(initialCapacity >= 0, "Negative initial capacity %s");
        if (BUILDER_CONSTRUCTOR != null) {
            try {
                return (ImmutableMap.Builder<K, V>) BUILDER_CONSTRUCTOR.invokeExact(initialCapacity);
            } catch (Throwable t) {
                throw SneakyThrow.sneakyThrow(t);
            }
        } else {
            return ImmutableMap.builder();
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void forEach(ImmutableMap<K, V> map, BiConsumer<? super K, ? super V> action) {
        checkNotNull(map, "Null map");
        if (ENTRIES_ARRAY_FIELD != null && ENTRIES_ARRAY_FIELD.getDeclaringClass().isInstance(map)) {
            for (Map.Entry<K, V> entry : ENTRIES_ARRAY_FIELD.get(map)) {
                K key = entry.getKey();
                V value = entry.getValue();
                checkNotNull(action, "Null action").accept(key, value);
            }
        } else {
            ImmutableList<Map.Entry<K, V>> entryList = map.entrySet().asList(); // Since they don't support forEach this is the fastest way to iterate
            for (int i = 0; i < entryList.size(); i++) {
                Map.Entry<K, V> entry = entryList.get(i);
                action.accept(entry.getKey(), entry.getValue());
            }
        }
    }
}
