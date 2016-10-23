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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import sun.misc.Unsafe;
import static com.google.common.base.Preconditions.*;
import static net.techcable.pineapple.reflection.Reflection.UNSAFE;

/**
 * Abstract type for all fields implemented using {@link sun.misc.Unsafe}, with default implementations.
 * <p>
 * Subclasses need to override any methods that could possibly work for their type.
 * All types must implement either `getBoxed()` or `getBoxedStatic()`,
 * depending on if they're a static field type.
 * </p>
 */
@SuppressWarnings("restriction")
/* package */ abstract class UnsafePineappleField<T, V> extends PineappleField<T, V> {
    /* package */ final long fieldOffset;

    /* package */ UnsafePineappleField(Field field) {
        this(
            field,
            Modifier.isStatic(field.getModifiers())
                ? UNSAFE.staticFieldOffset(field)
                : UNSAFE.objectFieldOffset(field)
        );
    }

    private UnsafePineappleField(Field field, long fieldOffset) {
        super(field);
        checkArgument(fieldOffset != Unsafe.INVALID_FIELD_OFFSET, "Invalid field offset: " + fieldOffset);
        this.fieldOffset = fieldOffset;

    }

    @Override
    public V get(T instance) {
        checkState(!this.isPrimitive(), "Field is primitive!");
        checkState(!this.isStatic(), "Field is static!");
        /*
         * It's not a primitive field, and it's not a static field.
         * They should override this method if it could possibly ever be successful.
         */
        throw new UnsupportedOperationException(
            "Type "
            + getClass()
            + " didn't implement get(), even though it's a non-primitive instance field!"
        );
    }

    @Override
    public V getStatic() {
        checkState(!this.isPrimitive(), "Field is primitive!");
        checkState(this.isStatic(), "Field is not static!");
        /*
         * It's not a primitive field, and it's not a static field.
         * They should override this method if it could possibly ever be successful.
         */
        throw new UnsupportedOperationException(
            "Type "
            + getClass()
            + " didn't implement getStatic(), even though it's a non-primitive static field!"
        );
    }

    @Override
    public int getInt(T instance) {
        checkState(this.primitiveType == PrimitiveType.INT, "Field isn't a primitive integer!");
        checkState(!this.isStatic(), "Field is static!");
        /*
         * It's not a primitive field, and it's not a static field.
         * They should override this method if it could possibly ever be successful.
         */
        throw new UnsupportedOperationException(
            "Type "
            + getClass()
            + " didn't implement getInt(), even though it's a primitive integer instance field!"
        );
    }

    @Override
    public int getStaticInt() {
        checkState(this.primitiveType == PrimitiveType.INT, "Field isn't a primitive integer!");
        checkState(this.isStatic(), "Field is not static!");
        /*
         * It's not a primitive field, and it's not a static field.
         * They should override this method if it could possibly ever be successful.
         */
        throw new UnsupportedOperationException(
            "Type "
            + getClass()
            + " didn't implement getStaticInt(), even though it's a primitive integer static field!"
        );
    }

    @Override
    public V getBoxed(T instance) {
        checkState(!this.isStatic(), "Field is static!");
        throw new UnsupportedOperationException(
            "Type "
            + getClass()
            + " didn't implement getBoxed(), even though it's an instance field!"
        );
    }

    @Override
    public V getStaticBoxed() {
        checkState(this.isStatic(), "Field is not static!");
        throw new UnsupportedOperationException(
            "Type "
            + getClass()
            + " didn't implement getBoxed(), even though it's a static field!"
        );
    }
}
