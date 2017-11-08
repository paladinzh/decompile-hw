package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible(emulated = true)
public abstract class FluentIterable<E> implements Iterable<E> {
    private final Iterable<E> iterable = this;

    protected FluentIterable() {
    }

    public String toString() {
        return Iterables.toString(this.iterable);
    }
}
