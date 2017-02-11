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
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.*;

/**
 * A PineappleField that accesses it's underlying field with reflection.
 */
/* package */ final class ReflectivePineappleField<T, V> extends PineappleField<T, V> {
    /* package */ ReflectivePineappleField(Field field) {
        super(field);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(T instance) {
        checkState(!this.isPrimitive(), "Field is primitive!");
        checkState(!this.isStatic(), "Field is static!");
        /*
         * Get the field's value optimistically, then catch any errors.
         * Then, convert the errors into cleaner ones for the caller.
         * We still have to check that the field isn't a primitive or static field,
         * since the reflection API is very lenient and would otherwise allow this to happen.
         */
        try {
            return (V) super.field.get(instance);
        } catch (NullPointerException e) {
            checkNotNull(instance, "Null instance"); // Check for null instance
            throw e; // Something else wen't wrong
        } catch (IllegalArgumentException e) {
            this.getDeclaringClass().cast(instance); // Check the cast
            throw e; // Something else went wrong
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Unexpected error accessing field: " + this,
                e
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getStatic() {
        checkState(!this.isPrimitive(), "Field is primitive!");
        /*
         * Get the field's value optimistically, then catch any errors.
         * Then, convert the errors into cleaner ones for the caller.
         * We still have to check that the field isn't a primitive field,
         * since the reflection API is very lenient and would otherwise allow this.
         */
        try {
            return (V) super.field.get(null);
        } catch (NullPointerException e) {
            /*
             * Maybe we were are actually an instance field.
             * Check that we are a static field and throw an error if not.
             */

            checkState(this.isStatic(), "Field is not a static field!");
            throw e; // Unexpected NPE
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Unexpected error accessing field: " + this,
                e
            );
        }
    }

    @Override
    public int getInt(T instance) {
        checkState(
            this.primitiveType == PrimitiveType.INT,
            "Field isn't a primitive int!"
        );
        checkState(!this.isStatic(), "Field is static!");
        /*
         * Get the field's value optimistically, then catch any errors.
         * Then, convert the errors into cleaner ones for the caller.
         * We still have to check that the field is an int field,
         * since the reflection API is very lenient and would otherwise allow implicit conversions.
         */
        try {
            return super.field.getInt(instance);
        } catch (NullPointerException e) {
            checkNotNull(instance, "Null instance"); // Check for null instance
            throw e; // Unexpected NPE
        } catch (IllegalArgumentException e) {
            this.getDeclaringClass().cast(instance); // Check the cast
            throw e; // Something else went wrong
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Unexpected error accessing field: " + this,
                e
            );
        }
    }

    @Override
    public int getStaticInt() {
        checkState(
            this.getPrimitiveType() == PrimitiveType.INT,
            "Field isn't a primitive int!"
        );
        /*
         * Get the field's value optimistically, then catch any errors.
         * Then, convert the errors into cleaner ones for the caller.
         * We still have to check that the field is an int field,
         * since the reflection API is very lenient,
         * and would otherwise allow implicit widening conversions.
         */
        try {
            return super.field.getInt(null);
        } catch (NullPointerException e) {
            /*
             * Maybe we were are actually an instance field.
             * Check that we are a static field and throw an error if not.
             */
            checkState(this.isStatic(), "Field is not a static field!");
            throw e; // Unexpected NPE
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Unexpected error accessing field: " + this,
                e
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getBoxed(T instance) {
        checkState(!this.isStatic(), "Field is static!");
        /*
         * Get the field's value optimistically, then catch any errors.
         * Then, convert the errors into cleaner ones for the caller.
         * We still have to check that the field is a static field,
         * since the reflection API would otherwise allow a null instances.
         * However, we do want to allow the implicit autoboxing here,
         * since that's what this method is supposed to do.
         */
        try {
            return (V) super.field.get(null);
        }  catch (NullPointerException e) {
            checkNotNull(instance, "Null instance"); // Check for null instance
            throw e; // Unexpected NPE
        } catch (IllegalArgumentException e) {
            this.getDeclaringClass().cast(instance); // Check the cast
            throw e; // Something else went wrong
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Unexpected error accessing field: " + this,
                e
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getStaticBoxed() {
        /*
         * Get the field's value optimistically, then catch any errors.
         * Then, convert the errors into cleaner ones for the caller.
         * We _do not_ have to check that the field is a static field,
         * since the reflection API is going to error if it is an instance field.
         * We also don't need to check for primitives,
         * since we _want_ to allow the implicit autoboxing here.
         */
        try {
            return (V) super.field.get(null);
        } catch (NullPointerException e) {
            /*
             * Maybe we were are actually an instance field.
             * Check that we are a static field and throw an error if not.
             */
            checkState(this.isStatic(), "Field is not a static field!");
            throw e; // Unexpected NPE
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Unexpected error accessing field: " + this,
                e
            );
        }
    }

    @Override
    public void putStaticBoxed(@Nullable V value) {
        try {
            super.field.set(null, value);
        } catch (NullPointerException e) {
            /*
             * Maybe we were are actually an instance field.
             * Check that we are a static field and throw an error if not.
             */
            checkState(this.isStatic(), "Field is not a static field!");
            throw e; // Unexpected NPE
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Unexpected error accessing field: " + this,
                    e
            );
        }
    }

    @Override
    public void putBoxed(T instance, @Nullable V value) {
        try {
            super.field.set(instance, value);
        } catch (NullPointerException e) {
            /*
             * Maybe we were are actually an instance field.
             * Check that we are a static field and throw an error if not.
             */
            checkState(this.isStatic(), "Field is not a static field!");
            throw e; // Unexpected NPE
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Unexpected error accessing field: " + this,
                    e
            );
        }
    }

    @Override
    public void put(T instance, @Nullable V value) {

    }

    @Override
    public void putStatic(@Nullable V value) {

    }

    @Override
    public void putInt(T instance, int value) {

    }

    @Override
    public void putStaticInt(int value) {

    }
}
