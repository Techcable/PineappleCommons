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
package net.techcable.pineapple.reflection;

import lombok.*;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.base.Verify;
import com.google.common.primitives.Primitives;

import static com.google.common.base.Preconditions.*;

@Getter
public class FastField<T, V> {
    private final Field field;
    private final Class<T> declaringType;
    private final Class<V> fieldType;

    private FastField(Field field, Class<T> declaringType, Class<V> fieldType) {
        this.field = checkNotNull(field, "Null field");
        this.declaringType = checkNotNull(declaringType, "Null declaring type");
        this.fieldType = checkNotNull(fieldType, "Null field type");
        field.setAccessible(true);
        checkArgument(fieldType.isAssignableFrom(Primitives.wrap(field.getType())));
        checkArgument(declaringType == field.getDeclaringClass());
    }

    private static final Unsafe UNSAFE;
    static {
        Unsafe u;
        try {
            Class.forName("sun.misc.Unsafe");
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            u = (Unsafe) field.get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalAccessException e) {
            u = null;
        }
        UNSAFE = u;
    }

    public static <T> FastField<T, ?> create(String name, Class<T> declaringType) {
        return create(name, declaringType, Object.class);
    }

    public static <T, V> FastField<T, V> create(String name, Class<T> declaringType, Class<V> fieldType) {
        Field field;
        try {
            field = checkNotNull(declaringType, "Null declaring type").getDeclaredField(checkNotNull(name, "Null fieldName"));
        } catch (NoSuchFieldException e) {
            return null;
        }
        checkArgument(checkNotNull(fieldType, "Null field type").isAssignableFrom(Primitives.wrap(field.getType())), "Field type %s doesn't equal expected field type %s", fieldType);
        if (!fieldType.isPrimitive() && UNSAFE != null) {
            return new UnsafeFastField<>(field, declaringType, fieldType);
        } else {
            return new FastField<>(field, declaringType, fieldType);
        }
    }

    @SuppressWarnings("unchecked") // The caller will check ;)
    public V get(T instance) {
        try {
            return (V) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Field " + field + " should've been set accessible!");
        }
    }

    public static class UnsafeFastField<T, V> extends FastField<T, V> {

        private long fieldOffset;
        private UnsafeFastField(Field field, Class<T> declaringType, Class<V> fieldType) {
            super(field, declaringType, fieldType);
            checkArgument(!field.getType().isPrimitive(), "Field is a primitive type %s", field.getType());
            checkArgument(!Modifier.isStatic(field.getType().getModifiers()), "Static field %s", field);
            fieldOffset = UNSAFE.objectFieldOffset(field);
            Verify.verify(fieldOffset >= 0);
        }

        @Override
        @SuppressWarnings("unchecked")
        public V get(T instance) {
            getDeclaringType().cast(checkNotNull(instance, "Null instance"));
            return (V) UNSAFE.getObject(instance, fieldOffset);
        }
    }
}
