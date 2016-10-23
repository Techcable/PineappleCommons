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

import javax.annotation.Nullable;

import com.google.common.primitives.Primitives;

import static com.google.common.base.Preconditions.*;

/**
 * An enumeration of all the primitive types in the java language.
 */
public enum PrimitiveType {
    BYTE(byte.class, Byte.class),
    CHAR(char.class, Character.class),
    SHORT(short.class, Short.class),
    INT(int.class, Integer.class),
    LONG(long.class, Long.class),
    FLOAT(float.class, Float.class),
    DOUBLE(double.class, Double.class),
    BOOLEAN(boolean.class, Boolean.class),
    VOID(void.class, Void.class);

    private final Class<?> primitiveClass, referenceClass;
    /* private */ PrimitiveType(Class<?> primitiveClass, Class<?> referenceClass) {
        this.primitiveClass = checkNotNull(primitiveClass);
        this.referenceClass = checkNotNull(referenceClass);
    }

    /**
     * Return this primitive's class
     * 
     * @return this primitive's class
     */
    public Class<?> getPrimitiveClass() {
        return primitiveClass;
    }

    /**
     * Return this primitive's reference class, that it is boxed into.
     * 
     * @return this primitive's reference class
     */
    public Class<?> getReferenceClass() {
        return referenceClass;
    }

    /**
     * Return if the primitive is a number.
     */
    public boolean isNumeric() {
        return Number.class.isAssignableFrom(referenceClass);
    }

    //
    // Lookup methods
    //

    /**
     * Return the primitive type of the specified class, or null if it isn't primitive.
     */
    @Nullable
    public static PrimitiveType fromClass(Class<?> primitiveClass) {
        String name = primitiveClass.getName(); 
        switch (name.charAt(0)) {
            case 'b':
                // It's either boolean or byte
                if (primitiveClass == boolean.class) {
                    return PrimitiveType.BOOLEAN;
                } else if (primitiveClass == byte.class) {
                    return PrimitiveType.BYTE;
                }
                break;
            case 'c':
                if (primitiveClass == char.class) {
                    return PrimitiveType.CHAR;
                }
                break;
            case 'd':
                if (primitiveClass == double.class) {
                    return PrimitiveType.DOUBLE;
                }
                break;
            case 'f':
                if (primitiveClass == float.class) {
                    return PrimitiveType.FLOAT;
                }
                break;
            case 'i':
                if (primitiveClass == int.class) {
                    return PrimitiveType.INT;
                }
                break;
            case 'l':
                if (primitiveClass == long.class) {
                    return PrimitiveType.LONG;
                }
                break;
            case 's':
                if (primitiveClass == short.class) {
                    return PrimitiveType.SHORT;
                }
                break;
            case 'v':
                if (primitiveClass == void.class) {
                    return PrimitiveType.VOID;
                }
                break;
        }
        assert !primitiveClass.isPrimitive();
        return null;
    }

    /**
     * Return the PrimitiveType with the specified boxed type, or null if it's not a boxed primitive.
     *
     * @return the primitive type of the specified boxed class, or null if none.
     */
    @Nullable
    public static PrimitiveType fromBoxedClass(Class<?> boxedClass) {
        String name = boxedClass.getName();
        /*
         * Switch on the first char after 'java.lang.', which has length 10.
         * Therefore the char will be at index 10, so check if name can fit it.
         * Then switch on the char, then check if the boxed class is equal.
         */
        if (name.length() > 10) {
            switch (name.charAt(10)) {
                case 'B':
                    if (boxedClass == Boolean.class) {
                        return PrimitiveType.BOOLEAN;
                    } else if (boxedClass == Byte.class) {
                        return PrimitiveType.BYTE;
                    }
                    break;
                case 'C':
                    if (boxedClass == Character.class) {
                        return PrimitiveType.CHAR;
                    }
                    break;
                case 'D':
                    if (boxedClass == Double.class) {
                        return PrimitiveType.DOUBLE;
                    }
                    break;
                case 'F':
                    if (boxedClass == Float.class) {
                        return PrimitiveType.FLOAT;
                    }
                    break;
                case 'I':
                    if (boxedClass == Integer.class) {
                        return PrimitiveType.INT;
                    }
                    break;
                case 'L':
                    if (boxedClass == Long.class) {
                        return PrimitiveType.LONG;
                    }
                    break;
                case 'S':
                    if (boxedClass == Short.class) {
                        return PrimitiveType.SHORT;
                    }
                    break;
                case 'V':
                    if (boxedClass == Void.class) {
                        return PrimitiveType.VOID;
                    }
                    break;
            }
        }
        assert !Primitives.wrap(boxedClass).isPrimitive();
        return null;
    }
}
