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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.security.AccessController;
import java.security.Permission;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.techcable.pineapple.SneakyThrow;
import static com.google.common.base.Preconditions.*;

@SuppressWarnings("restriction") // Let us use sun.misc.Unsafe
@ParametersAreNonnullByDefault
public final class Reflection {
    private Reflection() {}

    @Nullable
    public static MethodHandle getMethod(Class<?> declaringType, String name, Class<?>... parameterTypes) {
        try {
            checkNotNull(declaringType, "Null declaring type");
            checkNotNull(name, "Null name");
            checkNotNull(parameterTypes, "Null parameter types");
            Method reflectionMethod = declaringType.getMethod(name, parameterTypes);
            reflectionMethod.setAccessible(true);
            return MethodHandles.lookup().unreflect(reflectionMethod);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw SneakyThrow.sneakyThrow(e);
        }
    }

    @Nullable
    public static MethodHandle getConstructor(Class<?> declaringType, Class<?>... parameterTypes) {
        try {
            checkNotNull(parameterTypes, "Null parameters");
            Constructor<?> constructor = checkNotNull(declaringType, "Null declaring type").getConstructor(parameterTypes);
            constructor.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(constructor);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw SneakyThrow.sneakyThrow(e);
        }
    }

    public static <T> Class<? extends T> getClass(String name, Class<T> superclass) {
        Class<?> raw = getClass(name);
        return raw == null ? null : raw.asSubclass(checkNotNull(superclass, "Null superclass"));
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(checkNotNull(name, "Null name"));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    private static final MethodHandle FIELD_COPY_METHOD = getMethod(Field.class, "copy");
    /**
     * A field object's `root` field, indicating it's parent field object.
     * <p>
     * In java 8, you can only copy the parent field object in a tree.
     * Therefore, in order to copy the field object, we have to find the parent object and clone that.
     * We can't use a PineappleField here, since their API needs this class and can't use it until we're initialized.
     * Since they can't use us,
     * Therefore, we have to resort to the regular field reflection API, and use them instead.
     * </p>
     */
    @Nullable
    private static final Field FIELD_ROOT_FIELD;
    static {
        Field rootField;
        try {
            rootField = Field.class.getDeclaredField("root");
            rootField.setAccessible(true);
            if (Field.class != rootField.getType()) {
                rootField = null;
            }
        } catch (NoSuchFieldException e) {
            rootField = null;
        }
        FIELD_ROOT_FIELD = rootField;
    }
    private static Field getRootField(Field field) {
        try {
            return (Field) FIELD_ROOT_FIELD.get(field);
        } catch (IllegalAccessException e) {
            throw SneakyThrow.sneakyThrow(e); // Shouldn't happen
        }
    }
    
    /**
     * Create an identical clone of the specified field object, with an independent access flag.
     * <p>
     * The returned field will be accessible only if the specified field is accessible.
     * However, the accessibility flag of the returned field will be independent from the original,
     * so modifications to the original won't be reflected in the new field, and vice-versa.
     * </p>
     *
     * @param field the field to clone
     * @return a clone of the given field
     * @throws RuntimeException if unable to clone the field for whatever reason
     */
    public static Field cloneField(Field field) {
        checkNotNull(field, "Null field");
        if (FIELD_COPY_METHOD != null) {
            if (FIELD_ROOT_FIELD != null) {
                /*
                 * Make sure we're at the 'root' field.
                 * In java 8, copying fields that aren't root is forbidden.
                 */
                Field rootField;
                while ((rootField = getRootField(field)) != null) {
                    field = rootField;
                }
            }
            try {
                return (Field) FIELD_COPY_METHOD.invokeExact(field);
            } catch (Throwable t) {
                throw new RuntimeException(
                    "Unexpected error copying field " + field
                );
            }
        } else {
            // Our own private copy of the class's declared fields
            final Field[] declaredFields = field.getDeclaringClass().getDeclaredFields();
            for (Field declaredField: declaredFields) {
                if (declaredField.equals(field)) {
                    return field;
                }
            }
            throw new RuntimeException("Unable to clone field ("
                + field.getType().getSimpleName()
                + " "
                + field.getDeclaringClass().getTypeName()
                + "."
                + field.getName()
                + ") : Field not found in "
                + field.getDeclaringClass().getSimpleName()
                + ".getDeclaredFields()"
            );
        }
    }

    /**
     * The permsision to supress access checks in reflection.
     *
     * This permission is requried to access private methods and fields.
     * It is also used to guard access to {@link #aquireUnsafe},
     * since that can also be used to bypass security/access checks.s
     */
    /* package */ static final Permission SUPPRESS_ACCESS_CHECKS_PERMISSION = new ReflectPermission("suppressAccessChecks");
    @Nullable
    /* package */ static final sun.misc.Unsafe UNSAFE;
    static {
        sun.misc.Unsafe unsafe;
        try {
            Class<sun.misc.Unsafe> unsafeClass = sun.misc.Unsafe.class;
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (sun.misc.Unsafe) field.get(null);
        } catch (NoClassDefFoundError | NoSuchFieldException | SecurityException | IllegalAccessException e) {
            unsafe = null; // Can't find it :(
        }
        UNSAFE = unsafe;
    }
    
    /**
     * Aquire the instance of {@link sun.misc.Unsafe}, or null if it can't be found.
     * <p>
     * The returned Unsafe object should be carefully protected,
     * since it can be used to read and write data at arbitrary memory addresses.
     * It must never be passed to untrusted code, or the safety of the JVM will be destroyed.
     * The caller should keep a reference to the result of this method in a private constant.
     * This method requires a security/permissions check, and impairs JIT optimization,
     * so it should only be used in initialization code.
     * </p>
     *
     * @throws SecurityException if access to sun.misc.Unsafe is forbidden
     */
    @Nullable
    public static sun.misc.Unsafe aquireUnsafe() {
        AccessController.checkPermission(SUPPRESS_ACCESS_CHECKS_PERMISSION);
        return UNSAFE;
    }
}
