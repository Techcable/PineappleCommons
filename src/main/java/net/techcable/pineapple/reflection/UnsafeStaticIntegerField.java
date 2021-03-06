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
/* package */ class UnsafeStaticIntegerField extends UnsafePineappleField<Void, Integer> {
    /* package */ UnsafeStaticIntegerField(Field field) {
        super(field);
        Verify.verify(field.getType() == int.class);
        Verify.verify(Modifier.isStatic(field.getModifiers()));
    }

    @Override
    public int getStaticInt() {
        /*
         * Since we're a static field, we pass a null instance.
         * The fieldOffset is actually an absolute memory location, not a offset.
         */
        return UNSAFE.getInt(this.fieldBase, this.fieldOffset);
    }

    @Override
    public Integer getStaticBoxed() {
        return this.getStaticInt();
    }

    @Override
    public void forcePutStaticInt(int value) {
        UNSAFE.putInt(this.fieldBase, this.fieldOffset, value);
    }

    @Override
    public void forcePutStaticBoxed(@Nullable Integer value) {
        this.forcePutStaticInt(value);
    }
}
