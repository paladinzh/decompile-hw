package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.javax.annotation.CheckReturnValue;
import com.google.javax.annotation.Nullable;
import java.util.Iterator;

@GwtCompatible(emulated = true)
public abstract class FluentIterable<E> implements Iterable<E> {
    private final Iterable<E> iterable;

    protected FluentIterable() {
        this.iterable = this;
    }

    FluentIterable(Iterable<E> iterable) {
        this.iterable = (Iterable) Preconditions.checkNotNull(iterable);
    }

    public static <E> FluentIterable<E> from(final Iterable<E> iterable) {
        if (iterable instanceof FluentIterable) {
            return (FluentIterable) iterable;
        }
        return new FluentIterable<E>(iterable) {
            public Iterator<E> iterator() {
                return iterable.iterator();
            }
        };
    }

    @Deprecated
    public static <E> FluentIterable<E> from(FluentIterable<E> iterable) {
        return (FluentIterable) Preconditions.checkNotNull(iterable);
    }

    @Beta
    public static <E> FluentIterable<E> of(E[] elements) {
        return from(Lists.newArrayList((Object[]) elements));
    }

    public String toString() {
        return Iterables.toString(this.iterable);
    }

    public final int size() {
        return Iterables.size(this.iterable);
    }

    public final boolean contains(@Nullable Object element) {
        return Iterables.contains(this.iterable, element);
    }

    @CheckReturnValue
    public final FluentIterable<E> filter(Predicate<? super E> predicate) {
        return from(Iterables.filter(this.iterable, (Predicate) predicate));
    }

    @CheckReturnValue
    @GwtIncompatible("Class.isInstance")
    public final <T> FluentIterable<T> filter(Class<T> type) {
        return from(Iterables.filter(this.iterable, (Class) type));
    }

    public final <T> FluentIterable<T> transform(Function<? super E, T> function) {
        return from(Iterables.transform(this.iterable, function));
    }

    @CheckReturnValue
    public final FluentIterable<E> skip(int numberToSkip) {
        return from(Iterables.skip(this.iterable, numberToSkip));
    }

    public final boolean isEmpty() {
        return !this.iterable.iterator().hasNext();
    }

    @GwtIncompatible("Array.newArray(Class, int)")
    public final E[] toArray(Class<E> type) {
        return Iterables.toArray(this.iterable, type);
    }

    @Beta
    public final String join(Joiner joiner) {
        return joiner.join((Iterable) this);
    }

    public final E get(int position) {
        return Iterables.get(this.iterable, position);
    }
}
