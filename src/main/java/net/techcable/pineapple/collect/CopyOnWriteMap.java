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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.*;

@ParametersAreNonnullByDefault
public class CopyOnWriteMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
    @SuppressWarnings("AtomicFieldUpdaterIssues") // Generic.......
    private static final AtomicReferenceFieldUpdater<CopyOnWriteMap, ImmutableMap> MAP_UPDATER = AtomicReferenceFieldUpdater.newUpdater(CopyOnWriteMap.class, ImmutableMap.class, "bakedMap");
    private final Map<K, V> map = Collections.synchronizedMap(new HashMap<K, V>());
    @Nullable
    private volatile ImmutableMap<K, V> bakedMap = null;

    @Nonnull
    private ImmutableMap<K, V> bakedMap() {
        ImmutableMap<K, V> bakedMap = this.bakedMap;
        if (bakedMap == null) {
            return bakeMap();
        } else {
            return bakedMap;
        }
    }

    private ImmutableMap<K, V> bakeMap() {
        synchronized (map) {
            return this.bakedMap = ImmutableMap.copyOf(map);
        }
    }

    @Override
    public int size() {
        return bakedMap().size();
    }

    @Override
    public boolean containsKey(Object key) {
        return bakedMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return bakedMap().containsValue(value);
    }

    @Override
    @Nullable
    public V get(Object key) {
        return bakedMap().get(key);
    }

    @Override
    @Nullable
    public V put(K key, V value) {
        checkNotNull(key, "Null key");
        checkNotNull(value, "Null value");
        synchronized (map) {
            bakedMap = null;
            return map.put(key, value);
        }
    }

    @Override
    @Nullable
    public V remove(Object key) {
        checkNotNull(key, "Null key");
        synchronized (map) {
            bakedMap = null;
            return map.remove(key);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        checkNotNull(m, "Null bakedMap");
        synchronized (map) {
            bakedMap = null;
            map.putAll(m);
        }
    }

    @Override
    public void clear() {
        synchronized (map) {
            bakedMap = ImmutableMap.of();
        }
    }

    @Override
    @Nonnull
    public ImmutableSet<K> keySet() {
        return bakedMap().keySet();
    }

    @Override
    @Nonnull
    public ImmutableCollection<V> values() {
        return bakedMap().values();
    }

    @Override
    @Nonnull
    public ImmutableSet<Entry<K, V>> entrySet() {
        return bakedMap().entrySet();
    }

    @Override
    @Nullable
    public V getOrDefault(Object key, V defaultValue) {
        return bakedMap().getOrDefault(checkNotNull(key, "Null key"), defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        checkNotNull(action, "Null action");
        ImmutableMaps.forEach(bakedMap(), action);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        checkNotNull(key, "Null key");
        checkNotNull(value, "Null value");
        Map<K, V> bakedMap = this.bakedMap;
        V oldValue;
        if (bakedMap == null || (oldValue = bakedMap.get(key)) == null) {
            return putIfAbsent0(key, value);
        } else {
            return oldValue;
        }
    }

    private V putIfAbsent0(K key, V value) {
        synchronized (map) {
            bakedMap = null;
            return map.putIfAbsent(key, value);
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        checkNotNull(key, "Null key");
        checkNotNull(value, "Null value");
        synchronized (map) {
            bakedMap = null;
            return map.remove(key, value);
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        checkNotNull(key, "Null key");
        checkNotNull(oldValue, "Null old value");
        checkNotNull(newValue, "Null new value");
        synchronized (map) {
            bakedMap = null;
            return map.replace(key, oldValue, newValue);
        }
    }

    @Override
    @Nullable
    public V replace(K key, V value) {
        checkNotNull(key, "Null key");
        checkNotNull(value, "Null value");
        synchronized (map) {
            bakedMap = null;
            return map.replace(key, value);
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        checkNotNull(function, "Null function");
        synchronized (map) {
            bakedMap = null;
            map.replaceAll(function);
        }
    }

    @Override
    @Nonnull
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        checkNotNull(key, "Null key");
        checkNotNull(mappingFunction, "Null function");
        Map<K, V> bakedMap = this.bakedMap;
        V value;
        if (bakedMap == null || (value = bakedMap.get(key)) == null) {
            return computeIfAbsent0(key, mappingFunction);
        } else {
            return value;
        }
    }

    @Nonnull
    private V computeIfAbsent0(K key, Function<? super K, ? extends V> mappingFunction) {
        synchronized (map) {
            this.bakedMap = null;
            V value = map.get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                if (value == null) throw new IllegalArgumentException("Mapping function " + mappingFunction.getClass().getTypeName() + " returned null value for key " + key);
            }
            return value;
        }
    }

    @Override
    @Nullable
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        checkNotNull(remappingFunction, "Null remapping function");
        Map<K, V> bakedMap = this.bakedMap;
        if (bakedMap == null || bakedMap.get(key) != null) {
            synchronized (map) {
                this.bakedMap = null;
                return map.computeIfPresent(key, remappingFunction);
            }
        } else {
            return null;
        }
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        checkNotNull(key, "Null key");
        checkNotNull(remappingFunction, "Null remapping function");
        synchronized (map) {
            bakedMap = null;
            return map.compute(key, remappingFunction);
        }
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        checkNotNull(key, "Null key");
        checkNotNull(value, "Null value");
        checkNotNull(remappingFunction, "Null remapping function");
        synchronized (map) {
            bakedMap = null;
            return map.merge(key, value, remappingFunction);
        }
    }
}
