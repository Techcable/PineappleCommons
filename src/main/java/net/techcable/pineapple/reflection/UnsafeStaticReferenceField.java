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
import javax.annotation.Nullable;

import com.google.common.base.Verify;

import static net.techcable.pineapple.reflection.Reflection.*;

@SuppressWarnings("restriction")
/* package */ final class UnsafeStaticReferenceField<V> extends UnsafePineappleField<Void, V> {
    /* package */ UnsafeStaticReferenceField(Field field) {
        super(field);
        Verify.verify(!field.getType().isPrimitive());
        Verify.verify(Modifier.isStatic(field.getModifiers()));
    }

    @Override
    @SuppressWarnings("unchecked") // I do solemly swear not to crash the VM
    public V getStatic() {
        /*
         * It's perfectly safe that we don't check the type of the return object.
         * Because of erasure, the return type of this method is technically 'Object'.
         * The caller will have a automatic cast inserted by the compiler,
         * which will catch any issues here.
         *
         * Since we're a static field, we pass a null instance.
         * The fieldOffset is actually an absolute memory location, not a offset.
         */
        return (V) UNSAFE.getObject(this.fieldBase, this.fieldOffset);
    }

    @Override
    public V getStaticBoxed() {
        return this.getStatic();
    }


    @Override
    public void forcePutStatic(@Nullable V value) {
        this.fieldType.cast(value);
        UNSAFE.putObject(this.fieldBase, this.fieldOffset, value);
    }

    @Override
    public void forcePutStaticBoxed(@Nullable V value) {
        this.forcePutStatic(value);
    }
}
