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

import java.time.LocalDate;
import java.time.Month;

import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

@RunWith(Parameterized.class)
public class PineappleFieldTest {
    private final PineappleField field;
    private final Object instance;
    public PineappleFieldTest(PineappleField<?, ?> field, Object instance) {
        this.field = field;
        this.instance = instance;
    }

    @Test
    public void testStaticGet() {
        assumeTrue(field.isStatic());
        Object value;
        if (field.getPrimitiveType() == null) {
            assertNotNull(value = field.getStatic());
        } else {
            switch (field.getPrimitiveType()) {
                case INT:
                    value = field.getStaticInt();
                    break;
                default:
                    throw new AssertionError();
            }
        }
        assertEquals("Inconsistent results", value, field.getStaticBoxed());
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGet() {
        assumeTrue(!field.isStatic());
        Object value;
        if (field.getPrimitiveType() == null) {
            assertNotNull(value = field.get(instance));
        } else {
            switch (field.getPrimitiveType()) {
                case INT:
                    value = field.getInt(instance);
                    break;
                default:
                    throw new AssertionError();
            }
        }
        assertEquals("Inconsistent results", value, field.getBoxed(instance));
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testStaticPut() {
        assumeTrue(field.isStatic());
        assumeTrue(!field.isFinal());
        final Object oldValue, newValue;
        if (field.getPrimitiveType() == null) {
            if (String.class.isAssignableFrom(field.getFieldType())) {
                newValue = "I own u now";
            } else if (LocalDate.class.isAssignableFrom(field.getFieldType())) {
                newValue = LocalDate.of(1, 0, 0); // Jesus is the best
            } else {
                throw new AssertionError("Don't know the new value for " + field);
            }
            oldValue = field.getStatic();
            assertNotEquals("New value equals old value", newValue, oldValue);
            field.putStatic(newValue);
            assertEquals("Failed to set new value", newValue, field.getStatic());
        } else {
            switch (field.getPrimitiveType()) {
                case INT:
                    newValue = Integer.MIN_VALUE;
                    oldValue = field.getStaticInt();
                    assertNotEquals("New value equals old value", newValue, oldValue);
                    field.putStaticInt((Integer) newValue);
                    assertEquals("Failed to set new value", newValue, field.getStaticInt());
                    break;
                default:
                    throw new AssertionError();
            }
        }
        field.putStaticBoxed(oldValue); // Now switch back to the old value via putStaticBoxed
        assertEquals("Failed to revert to old value", oldValue, field.getStaticBoxed());
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testPut() {
        assumeTrue(!field.isStatic());
        assumeTrue(!field.isFinal());
        final Object oldValue, newValue;
        if (field.getPrimitiveType() == null) {
            if (String.class.isAssignableFrom(field.getFieldType())) {
                newValue = "I own u now";
            } else if (LocalDate.class.isAssignableFrom(field.getFieldType())) {
                newValue = LocalDate.of(1, 1, 1); // Jesus is the best
            } else {
                throw new AssertionError("Don't know the new value for " + field);
            }
            oldValue = field.get(instance);
            assertNotEquals("New value equals old value", newValue, oldValue);
            field.put(instance, newValue);
            assertEquals("Failed to set new value", newValue, field.get(instance));
        } else {
            switch (field.getPrimitiveType()) {
                case INT:
                    newValue = Integer.MIN_VALUE;
                    oldValue = field.getInt(instance);
                    assertNotEquals("New value equals old value", newValue, oldValue);
                    field.putInt(instance, (Integer) newValue);
                    assertEquals("Failed to set new value", newValue, field.getInt(instance));
                    break;
                default:
                    throw new AssertionError();
            }
        }
        field.putBoxed(instance, oldValue); // Now switch back to the old value via putStaticBoxed
        assertEquals("Failed to revert to old value", oldValue, field.getBoxed(instance));
    }

    @Parameterized.Parameters(name = "{0}")
    public static ImmutableList<Object[]> testData() {
        // Get every combination of int,Object, static/instance, private/public, and mutable/final
        ImmutableList.Builder<Object[]> builder = ImmutableList.builder();
        boolean[] trueFalse = new boolean[] {true,false};
        for (PrimitiveType type : new PrimitiveType[] {null,PrimitiveType.INT}) {
            for (boolean isStatic : trueFalse) {
                for (boolean isPrivate : trueFalse) {
                    for (boolean isFinal : trueFalse) {
                        StringBuilder fieldNameBuilder = new StringBuilder();
                        fieldNameBuilder.append(isPrivate ? "private" : "public");
                        if (isStatic) fieldNameBuilder.append("Static");
                        if (isFinal) fieldNameBuilder.append("Final");
                        if (type != null) {
                            String typeName = type.toString();
                            // Make the first char uppercase
                            fieldNameBuilder.append(Character.toUpperCase(typeName.charAt(0)));
                            // Append everything else lowercase
                            fieldNameBuilder.append(typeName, 1, typeName.length());
                        } else {
                            fieldNameBuilder.append("Object");
                        }
                        String fieldName = fieldNameBuilder.toString();
                        @SuppressWarnings("unchecked")
                        PineappleField field = PineappleField.create(TestFields.class, fieldName, (Class) (type != null ? type.getPrimitiveClass() : Object.class));
                        builder.add(new Object[] {field, isStatic ? null : new TestFields()});
                    }
                }
            }
        }
        return builder.build();
    }
    @SuppressWarnings("CheckStyle")
    public static class TestFields {
        private final String privateFinalObject = "do you want to build a snowman?";
        private String privateObject = "15 minutes could save you 15% or more on car insurance!";
        public final LocalDate publicFinalObject = LocalDate.of(1941, Month.DECEMBER, 7); // A date that will live in infamy
        public LocalDate publicObject = LocalDate.of(2001,Month.SEPTEMBER, 9); // For some reason I use attacks on america for all my test objects
        private static final String privateStaticFinalObject = "The hills are alive, with the sound of music.";
        private static String privateStaticObject = "Everybody clap your hands: clap, clap, clap, clap, clap, clap, clap";
        public static final String publicStaticFinalObject = "How much wood could a woodchuck chuck if a woodchuk could chuck wood?";
        public static String publicStaticObject = "About as much wood as a wouldchuck could chuck if a woodchuck could chuk wood.";
        private final int privateFinalInt = 1;
        private int privateInt = 2;
        public final int publicFinalInt = 3;
        public int publicInt = 4;
        private static final int privateStaticFinalInt = 5;
        private static int privateStaticInt = 17;
        public static final int publicStaticFinalInt = -5;
        public static int publicStaticInt = -1;
    }
}
