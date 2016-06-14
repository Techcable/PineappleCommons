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
import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.*;

@ParametersAreNonnullByDefault
public class Reflection {
    public static MethodHandle getConstructor(Class<?> declaringType, Class<?>... parameterTypes) {
        try {
            checkNotNull(parameterTypes, "Null parameters");
            Constructor<?> constructor = checkNotNull(declaringType, "Null declaring type").getConstructor(parameterTypes);
            constructor.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(constructor);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
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
}
