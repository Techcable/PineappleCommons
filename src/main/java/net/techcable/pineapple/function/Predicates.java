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
package net.techcable.pineapple.function;

import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import net.techcable.pineapple.NonnullByDefault;
import net.techcable.pineapple.collect.ImmutableLists;

import static com.google.common.base.Preconditions.*;

@NonnullByDefault
public final class Predicates {
    private Predicates() {
    }

    public static <T> Predicate<T> alwaysTrue() {
        return ObjectPredicates.ALWAYS_TRUE.narrowType();
    }

    public static <T> Predicate<T> alwaysFalse() {
        return ObjectPredicates.ALWAYS_FALSE.narrowType();
    }

    public static <T> Predicate<T> isNonNull() {
        return ObjectPredicates.IS_NULL.narrowType();
    }

    public static <T> Predicate<T> isNull() {
        return ObjectPredicates.IS_NULL.narrowType();
    }

    public static <T> Predicate<T> and(Predicate<? super T> firstPredicate, Predicate<? super T> secondPredicate) {
        return and(ImmutableList.<Predicate<? super T>>of(firstPredicate, secondPredicate));
    }


    public static <T> Predicate<T> and(
            Predicate<? super T> firstPredicate,
            Predicate<? super T> secondPredicate,
            Predicate<? super T> thirdPredicate
    ) {
        return and(ImmutableList.<Predicate<? super T>>of(firstPredicate, secondPredicate, thirdPredicate));
    }


    public static <T> Predicate<T> and(
            Predicate<? super T> firstPredicate,
            Predicate<? super T> secondPredicate,
            Predicate<? super T> thirdPredicate,
            Predicate<? super T> fourthPredicate
    ) {
        return and(ImmutableList.<Predicate<? super T>>of(
                firstPredicate,
                secondPredicate,
                thirdPredicate,
                fourthPredicate
        ));
    }


    public static <T> Predicate<T> and(
            Predicate<? super T> firstPredicate,
            Predicate<? super T> secondPredicate,
            Predicate<? super T> thirdPredicate,
            Predicate<? super T> fourthPredicate,
            Predicate<? super T> fifthPredicate
    ) {
        return and(ImmutableList.<Predicate<? super T>>of(
                firstPredicate,
                secondPredicate,
                thirdPredicate,
                fourthPredicate,
                fifthPredicate
        ));
    }

    public static <T> Predicate<T> and(ImmutableList<Predicate<? super T>> predicates) {
        return new AndPredicate<T>(predicates);
    }


    public static <T> Predicate<T> or(Predicate<? super T> firstPredicate, Predicate<? super T> secondPredicate) {
        return or(ImmutableList.<Predicate<? super T>>of(firstPredicate, secondPredicate));
    }


    public static <T> Predicate<T> or(
            Predicate<? super T> firstPredicate,
            Predicate<? super T> secondPredicate,
            Predicate<? super T> thirdPredicate
    ) {
        return or(ImmutableList.<Predicate<? super T>>of(firstPredicate, secondPredicate, thirdPredicate));
    }


    public static <T> Predicate<T> or(
            Predicate<? super T> firstPredicate,
            Predicate<? super T> secondPredicate,
            Predicate<? super T> thirdPredicate,
            Predicate<? super T> fourthPredicate
    ) {
        return or(ImmutableList.<Predicate<? super T>>of(
                firstPredicate,
                secondPredicate,
                thirdPredicate,
                fourthPredicate
        ));
    }


    public static <T> Predicate<T> or(
            Predicate<? super T> firstPredicate,
            Predicate<? super T> secondPredicate,
            Predicate<? super T> thirdPredicate,
            Predicate<? super T> fourthPredicate,
            Predicate<? super T> fifthPredicate
    ) {
        return or(ImmutableList.<Predicate<? super T>>of(
                firstPredicate,
                secondPredicate,
                thirdPredicate,
                fourthPredicate,
                fifthPredicate
        ));
    }

    public static <T> Predicate<T> or(ImmutableList<Predicate<? super T>> predicates) {
        return new OrPredicate<T>(predicates);
    }

    public static <T> Predicate<? super T> instanceOf(Class<T> type) {
        return checkNotNull(type, "Null type")::isInstance;
    }

    //
    // Implementations
    //

    private static final class AndPredicate<T> implements Predicate<T> {
        private final ImmutableList<Predicate<? super T>> predicates;

        private AndPredicate(ImmutableList<Predicate<? super T>> predicates) {
            if (checkNotNull(predicates, "Null predicates").size() < 2) {
                throw new IllegalArgumentException("Only " + predicates.size() + " predicates given!");
            }
            this.predicates = predicates;
        }

        @Override
        public boolean test(T o) {
            for (int i = 0; i < predicates.size(); i++) {
                Predicate<? super T> predicate = predicates.get(i);
                if (!predicate.test(o)) return false;
            }
            return true;
        }

        @Override
        public Predicate<T> and(Predicate<? super T> other) {
            ImmutableList.Builder<Predicate<? super T>> builder = ImmutableLists.builder(predicates.size() + 1);
            builder.addAll(predicates);
            builder.add(other);
            return new AndPredicate<T>(builder.build());
        }
    }

    private static final class OrPredicate<T> implements Predicate<T> {
        private final ImmutableList<Predicate<? super T>> predicates;

        private OrPredicate(ImmutableList<Predicate<? super T>> predicates) {
            if (checkNotNull(predicates, "Null predicates").size() < 2) {
                throw new IllegalArgumentException("Only " + predicates.size() + " predicates given!");
            }
            this.predicates = predicates;
        }

        @Override
        public Predicate<T> or(Predicate<? super T> other) {
            ImmutableList.Builder<Predicate<? super T>> builder = ImmutableLists.builder(predicates.size() + 1);
            builder.addAll(predicates);
            builder.add(other);
            return new OrPredicate<T>(builder.build());
        }

        @Override
        public boolean test(T o) {
            for (int i = 0; i < predicates.size(); i++) {
                Predicate<? super T> predicate = predicates.get(i);
                if (predicate.test(o)) return true;
            }
            return false;
        }

        @Override
        public Predicate<T> and(Predicate<? super T> other) {
            ImmutableList.Builder<Predicate<? super T>> builder = ImmutableLists.builder(predicates.size() + 1);
            builder.addAll(predicates);
            builder.add(other);
            return new AndPredicate<T>(builder.build());
        }
    }

    private enum ObjectPredicates implements Predicate {
        ALWAYS_TRUE {
            @Override
            public boolean test(Object o) {
                return true;
            }

            @Override
            public Predicate negate() {
                return ALWAYS_TRUE;
            }

            @Override
            public Predicate and(Predicate other) {
                return checkNotNull(other, "Null other");
            }

            @Override
            public Predicate or(Predicate other) {
                checkNotNull(other, "Null other");
                return this; // true || other == true
            }
        },
        ALWAYS_FALSE {
            @Override
            public boolean test(Object o) {
                return false;
            }

            @Override
            public Predicate negate() {
                return ALWAYS_TRUE;
            }

            @Override
            public Predicate and(Predicate other) {
                checkNotNull(other, "Null other");
                return this; // false && other == false
            }

            @Override
            public Predicate or(Predicate other) {
                return checkNotNull(other, "Null other");
            }


        },
        IS_NON_NULL {
            @Override
            public Predicate negate() {
                return IS_NULL;
            }

            @Override
            public boolean test(Object o) {
                return o != null;
            }
        },
        IS_NULL {
            @Override
            public Predicate negate() {
                return IS_NON_NULL;
            }

            @Override
            public boolean test(Object o) {
                return o == null;
            }
        };

        @SuppressWarnings("unchecked")
        public <T> Predicate<T> narrowType() {
            return this;
        }
    }
}
