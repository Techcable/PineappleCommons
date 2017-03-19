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
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.*;
import static net.techcable.pineapple.SimpleFormatter.*;

/**
 * A reference to a class's field, which can be used to access its value.
 * <p>
 * Provides safer, stricter, faster and easier access than a {@link Field} object.
 * However, it can be converted into a {@link Field} object by calling {@link PineappleField#getField}.
 * Unlike the core reflection API, where access checking is performed on every single invocation,
 * access checking for a PineappleField is performed only when it's created. Therefore,
 * instances of this class should usually be kept secret, and not given to untrusted code.
 * </p>
 * <p>
 * PineappleFields make a clear distinction between reference types and primitive types,
 * and won't autobox primtives into a object, or do implicit primitive conversions.
 * When autoboxing is desired, the {@link PineappleField#getBoxed}
 * </p>
 * <p>
 * PineappleFields are immutable and have no visible state.
 * PineappleFields cannot be subclassed by the user, but the implementation may decide to do so.
 * They can only be created by static factory methods in this class, or utilities in {@link Reflection}.
 * </p>
 */
@ThreadSafe
public abstract class PineappleField<T, V> {
    /* package */ final Field field;
    /* package */ final Class<T> declaringClass;
    /* package */ final Class<V> fieldType;
    /**
     * The field's modifier bits, kept inline with this wrapper object.
     */
    /* package */ final int modifiers;
    @Nullable
    /* package */ final PrimitiveType primitiveType;

    @SuppressWarnings("unchecked") // Callers shouldn't be accessing this method.
    /* package */ PineappleField(Field field) {
        this.field = checkNotNull(field, "Null field");
        checkArgument(
            field.isAccessible()
            || Modifier.isPublic(field.getModifiers())
            && Modifier.isPublic(field.getDeclaringClass().getModifiers()),
            "Field isn't accessible: %s",
            field
        );
        this.declaringClass = (Class<T>) field.getDeclaringClass();
        this.fieldType = (Class<V>) field.getType();
        this.modifiers = field.getModifiers();
        this.primitiveType = PrimitiveType.fromClass(field.getType());
        Verify.verify(this.isPrimitive() == field.getType().isPrimitive());
    }

    //
    // Getters
    //

    /**
     * Get the {@link Field} object corresponding to this field.
     *
     * @return the field object
     */
    public final Field getField() {
        return Reflection.cloneField(field); // Defensive copy
    }

    /**
     * Return the class that declared/owns this field.
     *
     * @return the class that owns this field
     */
    public final Class<T> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Return the type of this field.
     *
     * @return the type of this field
     */
    public final Class<V> getFieldType() {
        return fieldType;
    }

    //
    // Utility methods
    //

    /**
     * Return if this field's type is a primitive type.
     *
     * @return if this field is a primitive type.
     * @see #getPrimitiveType() getPrimitiveType
     */
    public final boolean isPrimitive() {
        return primitiveType != null;
    }

    /**
     * Return the primitive type of this field, or null if it isn't a primitive type.
     * <p>
     * Returns the same result as calling {@code PrimitiveType.fromClass(getFieldType())}.
     * The result of this method can also be used to check if the field is a primitive,
     * by checking if the result is null, without an explicit call to {@link #isPrimitive}.
     * </p>
     *
     * @return the primitive type of this field, or null if not a primitive
     * @see PrimitiveType
     */
    @Nullable
    public final PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    /**
     * Return if this field is a public field.
     *
     * @return if this field is a public field.
     */
    public final boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    /**
     * Return if this field is a static field.
     *
     * @return if this field is static
     */
    public final boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Return if this field is a final field.
     *
     * @return if this field is final
     */
    public final boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    //
    // Reflective access and setters
    //

    /**
     * Return the value of this _non-primitive instance field_ in the specified instance.
     * <p>
     * This method doesn't support primitive fields or static fields,
     * only _reference object_ instance fields.
     * If you need to support both primitive and object fields,
     * use {@link #getBoxed(T)} to perform the appropriate conversions automatically.
     * </p>
     *
     * @param instance the instance to get the field's value from
     * @return the current value of the field in the given instance
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if the field is static or is a primtive field
     */
    public abstract V get(T instance);

    /**
     * Return the value of this _non-primitive static field_.
     * <p>
     * This method doesn't support primitive fields or instance fields,
     * only _non-primitive_ static fields.
     * If you need to support both primitive and object fields,
     * use {@link #getStaticBoxed()} to perform the appropriate conversions automatically.
     * </p>
     *
     * @return the current value of the field
     * @throws IllegalStateException if the field is not static or is a primtive field
     */
    public abstract V getStatic();

    /**
     * Return the value of this _primitive integer instance field_ in the specified instance.
     * <p>
     * This method doesn't support wrapper objects, other primitive types, or static fields,
     * only _primitive integer_ instance fields.
     * If you need to support both primitive and object fields, or support other primitive types,
     * use {@link #getBoxed(T)} to perform the appropriate conversions automatically.
     * </p>
     *
     * @param instance the instance to get the field's value from
     * @return the current value of this integer field in the given instance
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if the field is static or isn't a primitive integer
     */
    public abstract int getInt(T instance);

    /**
     * Return the value of this _primitive integer static field_.
     * <p>
     * This method doesn't support wrapper objects, other primitive types, or instance fields,
     * only _primitive integer_ static fields.
     * If you need to support both primitive and object fields, or support other primitive types,s
     * use {@link #getStaticBoxed()} to perform the appropriate conversions automatically.
     * </p>
     *
     * @return the current value of this integer field
     * @throws IllegalStateException if the field is static or isn't a primitive integer
     */
    public abstract int getStaticInt();

    /**
     * Return the value of this instance field in the specified object,
     * automatically boxing primitives into the appropriate wrapper object.
     * <p>
     * This method supports both primitive and object fields,
     * automatically boxing primitives into their corresponding wrapper objects.
     * This method only supports instance fields, not static fields.
     * Static fields can be accessed with {@link #getStaticBoxed()}.
     * </p>
     *
     * @param instance the instance to get the field's value from
     * @return the current value of the field in the given instance
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if this field is static.
     */
    public abstract V getBoxed(T instance);

    /**
     * Return the current value of this static field,
     * automatically boxing primitives into the appropriate wrapper object.
     * <p>
     * This method supports both primitive and object fields,
     * automatically boxing primitives into their corresponding wrapper objects.
     * This method only supports static fields, not instance fields.
     * Instance fields can be accessed with {@link #getBoxed(Object)}.
     * </p>
     *
     * @return the current value of the field
     * @throws IllegalStateException if this field isn't static.
     */
    public abstract V getStaticBoxed();

    /**
     * Set the new value of this static field,
     * automatically unboxing wrapper types into the appropriate primitive.
     * <p>
     * This method supports both primitive and object fields,
     * automatically unboxing primitives into their corresponding wrapper objects.
     * This method only supports static fields, not instance fields.
     * Instance fields can be accessed with {@link #putBoxed(Object, Object)}.
     * </p>
     *
     * @param value the new value of the field
     * @throws IllegalStateException if this field isn't static, or if the field is final.
     */
    public final void putStaticBoxed(@Nullable V value) {
        checkState(!isFinal(), "Field is final!");
        forcePutStaticBoxed(value);
    }

    /**
     * Forcibly set the new value of this static field,
     * automatically unboxing wrapper types into the appropriate primitive,
     * and <i>ignoring whether or not it's a final field</i>
     * <p>
     * This method supports both primitive and object fields,
     * automatically unboxing primitives into their corresponding wrapper objects.
     * This method only supports static fields, not instance fields.
     * Instance fields can be accessed with {@link #forcePutBoxed(Object, Object)}.
     * </p>
     * <p>
     * Since the java language forbids setting final fields,
     * the JIT and compiler are free to optimize based on the assumption they never change.
     * Changes forcibly made to a final field may never be seen, depending on optimization settings.
     * </p>
     *
     * @param value the new value of the field
     * @throws IllegalStateException if this field isn't static.
     */
    public abstract void forcePutStaticBoxed(@Nullable V value);

    /**
     * Set the new value of this instance field,
     * automatically unboxing wrapper types into the appropriate primitive type.
     * <p>
     * This method supports both primitive and object fields,
     * automatically unboxing primitives into their corresponding wrapper objects.
     * This method only supports instance fields, not static fields.
     * Static fields can be accessed with {@link #forcePutStaticBoxed(Object)}.
     * </p>
     *
     * @param instance the instance of the class to set the field's value in
     * @param value the new value of the field
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if this field isn't static, or if the field is final.
     */
    public final void putBoxed(T instance, @Nullable V value) {
        checkState(!isFinal(), "Field is final!");
        forcePutBoxed(instance, value);
    }

    /**
     * Set the new value of this instance field,
     * automatically unboxing wrapper types into the appropriate primitive type,
     * and <i>ignoring whether or not it's a final field</i>
     * <p>
     * This method supports both primitive and object fields,
     * automatically unboxing primitives into their corresponding wrapper objects.
     * This method only supports instance fields, not static fields.
     * Static fields can be accessed with {@link #forcePutStaticBoxed(Object)}.
     * </p>
     * <p>
     * Since the java language forbids setting final fields,
     * the JIT and compiler are free to optimize based on the assumption they never change.
     * Changes forcibly made to a final field may never be seen, depending on optimization settings.
     * </p>
     *
     * @param instance the instance of the class to set the field's value in
     * @param value the new value of the field
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if this field isn't static, or if the field is final.
     */
    public abstract void forcePutBoxed(T instance, @Nullable V value);

    /**
     * Set the new value of this _non-primitive_ instance field.
     * <p>
     * This method doesn't support primitive fields or static fields,
     * only _reference object_ instance fields.
     * If you need to support both primitive and object fields,
     * use {@link #putBoxed(Object, Object)} to perform the appropriate conversions automatically.
     * </p>
     *
     * @param instance the instance of the class to set the field's value in
     * @param value the new value of the field
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if this field isn't static, or if the field is final.
     */
    public final void put(T instance, @Nullable V value) {
        checkState(!isFinal(), "Field is final!");
        forcePut(instance, value);
    }

    /**
     * Set the new value of this _non-primitive_ instance field,
     * <i>ignoring whether or not it's a final field</i>
     * <p>
     * This method doesn't support primitive fields or static fields,
     * only _reference object_ instance fields.
     * If you need to support both primitive and object fields,
     * use {@link #forcePutBoxed(Object, Object)} to perform the appropriate conversions automatically.
     * </p>
     * <p>
     * Since the java language forbids setting final fields,
     * the JIT and compiler are free to optimize based on the assumption they never change.
     * Changes forcibly made to a final field may never be seen, depending on optimization settings.
     * </p>
     *
     * @param instance the instance of the class to set the field's value in
     * @param value the new value of the field
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if this field isn't static.
     */
    public abstract void forcePut(T instance, @Nullable V value);

    /**
     * Set the new value of this _non-primitive_ static field.
     * <p>
     * This method doesn't support primitive fields or instance fields,
     * only _reference object_ static fields.
     * If you need to support both primitive and object fields,
     * use {@link #forcePutStaticBoxed(Object)} to perform the appropriate conversions automatically.
     * </p>
     *
     * @param value the new value of the field
     * @throws IllegalStateException if this field isn't static, or if the field is final.
     */
    public final void putStatic(V value) {
        checkState(!isFinal(), "Field is final!");
        forcePutStatic(value);
    }

    /**
     * Set the new value of this _non-primitive_ static field,
     * <i>ignoring whether or not it's a final field</i>
     * <p>
     * This method doesn't support primitive fields or instance fields,
     * only _reference object_ static fields.
     * If you need to support both primitive and object fields,
     * use {@link #forcePutStaticBoxed(Object)} to perform the appropriate conversions automatically.
     * </p>
     * <p>
     * Since the java language forbids setting final fields,
     * the JIT and compiler are free to optimize based on the assumption they never change.
     * Changes forcibly made to a final field may never be seen, depending on optimization settings.
     * </p>
     *
     * @param value the new value of the field
     * @throws IllegalStateException if this field isn't static.
     */
    public abstract void forcePutStatic(V value);

    /**
     * Set the new value of this _int primitive_ instance field.
     * <p>
     * This method doesn't support boxed integer fields or static fields,
     * only _primitive integer_ instance fields.
     * If you need to support both primitive and object fields,
     * use {@link #forcePutBoxed(Object, Object)} to perform the appropriate conversions automatically.
     * </p>
     *
     * @param instance the instance of the class to set the field's value in
     * @param value the new value of the field
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if this field isn't static, or if the field is final.
     */
    public final void putInt(T instance, int value) {
        checkState(!isFinal(), "Field is final!");
        forcePutInt(instance, value);
    }

    /**
     * Set the new value of this _int primitive_ instance field.
     * <i>ignoring whether or not it's a final field</i>
     * <p>
     * This method doesn't support boxed integer fields or static fields,
     * only _primitive integer_ instance fields.
     * If you need to support both primitive and object fields,
     * use {@link #forcePutBoxed(Object, Object)} to perform the appropriate conversions automatically.
     * </p>
     * <p>
     * Since the java language forbids setting final fields,
     * the JIT and compiler are free to optimize based on the assumption they never change.
     * Changes forcibly made to a final field may never be seen, depending on optimization settings.
     * </p>
     *
     * @param instance the instance of the class to set the field's value in
     * @param value the new value of the field
     * @throws ClassCastException if the given object isn't an instance of the declaring class
     * @throws NullPointerException if the instance is null
     * @throws IllegalStateException if this field isn't static.
     */
    public abstract void forcePutInt(T instance, int value);

    /**
     * Set the new value of this _non-primitive_ static field.
     * <p>
     * This method doesn't support boxed integer fields or instance fields,
     * only _primitive integer_ static fields.
     * If you need to support both primitive and object fields,
     * use {@link #forcePutBoxed(Object, Object)} to perform the appropriate conversions automatically.
     * </p>
     *
     * @param value the new value of the field
     * @throws IllegalStateException if this field isn't static, or if the field is final.
     */
    public final void putStaticInt(int value) {
        checkState(!isFinal(), "Field is final!");
        forcePutStaticInt(value);
    }

    /**
     * Set the new value of this _non-primitive_ static field.
     * <i>ignoring whether or not it's a final field</i>
     * <p>
     * This method doesn't support boxed integer fields or instance fields,
     * only _primitive integer_ static fields.
     * If you need to support both primitive and object fields,
     * use {@link #forcePutBoxed(Object, Object)} to perform the appropriate conversions automatically.
     * </p>
     * <p>
     * Since the java language forbids setting final fields,
     * the JIT and compiler are free to optimize based on the assumption they never change.
     * Changes forcibly made to a final field may never be seen, depending on optimization settings.
     * </p>
     *
     * @param value the new value of the field
     * @throws IllegalStateException if this field isn't static.
     */
    public abstract void forcePutStaticInt(int value);

    @Override
    public String toString() {
        return this.field.getDeclaringClass().getTypeName() + "." + this.field.getName();
    }

    //
    // Static constructors and factories
    //

    /**
     * Get the pineapple field corresponding to the specified field object.
     *
     * @param field the field to get the pineapple field for
     * @return a pineapple field corresponding to the specified field object
     * @throws NullPointerException if the field is null
     * @throws SecurityException if the caller doesn't have access to the specified field.
     */
    public static PineappleField<?, ?> fromField(Field field) {
        Objects.requireNonNull(field, "Null field");
        field = Reflection.cloneField(field); // Defensive copy
        if (!Modifier.isPublic(field.getModifiers())) {
            /*
             * The field isn't public, so they're basically trying to suppress access checks.
             * Ensure that they're permitted to do that, by checking for the permission.
             * We don't care whether or not field.isAccessible(), since we wan't to check for permission again,
             * and we want to implicitly access private fields, without an explicit call to setAccessible.
             */
            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                securityManager.checkPermission(Reflection.SUPPRESS_ACCESS_CHECKS_PERMISSION);
            }
        }
        field.setAccessible(true);
        final boolean isStatic = Modifier.isStatic(field.getModifiers());
        final Class<?> fieldType = field.getType();
        if (Reflection.UNSAFE != null) {
            PrimitiveType primitiveType = PrimitiveType.fromClass(fieldType);
            if (primitiveType != null) {
                switch (primitiveType) {
                    case INT:
                        if (isStatic) {
                            return new UnsafeStaticIntegerField(field);
                        } else {
                            return new UnsafeInstanceIntegerField<>(field);
                        }
                    default:
                        break; // Fallback to reflection
                }
            } else {
                // The primitive type is null, so it must be a reference field.
                return isStatic ? new UnsafeStaticReferenceField<>(field)
                    : new UnsafeInstanceReferenceField<>(field);
            }
        }
        return new ReflectivePineappleField<>(field);
    }

    /**
     * Get the pineapple field named {@code name} in the specified class.
     *
     * @param declaringType the type that declared the field
     * @param name  the name of the field
     * @param <T> the type of the declaring class
     * @return the field with the specified name and class
     * @throws NullPointerException     if type or name is null
     * @throws IllegalArgumentException if a field with the given name doesn't exist
     * @throws SecurityException if the caller doesn't have access to the specified field.
     */
    @SuppressWarnings("unchecked")
    public static <T> PineappleField<T, ?> create(Class<? extends T> declaringType, String name) {
        try {
            return (PineappleField<T, ?>) fromField(declaringType.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No field named " + name + " in " + declaringType);
        }

    }

    /**
     * Get the pineapple field named {@code name} with the type {@code fieldType} in the specified class.
     *
     * @param declaringType the type to get the field from
     * @param name  the name of the field
     * @param fieldType the type of the field's value
     * @param <T> the type of the declaring class
     * @param <V> the type of the field
     * @return the field with the specified name and class
     * @throws NullPointerException     if type or name is null
     * @throws IllegalArgumentException if a field with the given name and type doesn't exist
     * @throws SecurityException if the caller doesn't have access to the specified field.
     */
    @SuppressWarnings("unchecked")
    public static <T, V> PineappleField<T, V> create(Class<? extends T> declaringType, String name, Class<V> fieldType) {
        Field field;
        try {
            field = checkNotNull(declaringType, "Null declaring type").getDeclaredField(checkNotNull(name, "Null fieldName"));
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(format("Can't find field named '{}' in {}", name, declaringType.getTypeName()));
        }
        checkArgument(checkNotNull(fieldType, "Null field type").isAssignableFrom(field.getType()), "Field type %s doesn't equal expected field type %s", fieldType);
        return (PineappleField<T, V>) fromField(field);
    }


    /**
     * Get the only field in the class with the given type
     * <p>Throws an exception if no fields are found, or if multiple fields are found.</p>
     *
     * @param clazz the type to find the field in
     * @param fieldType the type of the field
     * @param <T> the type of the declaring class
     * @param <V> the type of the field
     * @return the only field
     * @throws IllegalArgumentException if there is more than one field with the given type
     * @throws IllegalArgumentException if not found
     * @throws NullPointerException if any args are null
     */
    public static <T, V> PineappleField<T, V> findFieldWithType(Class<T> clazz, Class<V> fieldType) {
        ImmutableList<PineappleField<T, V>> fields = findFieldsWithType(clazz, fieldType);
        switch (fields.size()) {
            case 1:
                return fields.get(0);
            case 0:
                throw new IllegalArgumentException("Field in " + clazz + " not found with type " + fieldType);
            default:
                StringBuilder builder = new StringBuilder("Multiple fields found in ");
                builder.append(clazz);
                builder.append(" with type ");
                builder.append(fieldType);
                builder.append(": [");
                for (int i = 0; i < fields.size(); i++) {
                    PineappleField<T, V> field = fields.get(i);
                    builder.append(field);
                    if (i + 1 < fields.size()) { // Has more
                        builder.append(", ");
                    }
                }
                builder.append("] ");
                throw new IllegalArgumentException(builder.toString());
        }
    }

    /**
     * Get the only field in the class with the given type
     * @param clazz the type to find the field in
     * @param fieldType the type of the field
     * @param <T> the type of the declaring class
     * @param <V> the type of the field
     * @throws IllegalArgumentException if there is more than one field with the given type
     * @throws IllegalArgumentException if not found
     * @throws NullPointerException if any args are null
     * @return all fields in the class with the given type
     */
    @SuppressWarnings("unchecked")
    public static <T, V> ImmutableList<PineappleField<T, V>> findFieldsWithType(Class<T> clazz, Class<V> fieldType) {
        checkNotNull(clazz, "Null class");
        checkNotNull(fieldType, "Null type");
        ImmutableList.Builder<PineappleField<T, V>> builder = ImmutableList.builder();
        for (Field field : clazz.getDeclaredFields()) {
            if (Reflection.isLenientlyAssignableFrom(fieldType, field.getType())) {
                builder.add((PineappleField<T, V>) fromField(field));
            }
        }
        return builder.build();
    }
}
