package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.List;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true, serializable = true)
final class SingletonImmutableList<E> extends ImmutableList<E> {
    final transient E element;

    SingletonImmutableList(E element) {
        this.element = Preconditions.checkNotNull(element);
    }

    public E get(int index) {
        Preconditions.checkElementIndex(index, 1);
        return this.element;
    }

    public int indexOf(@Nullable Object object) {
        return this.element.equals(object) ? 0 : -1;
    }

    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(this.element);
    }

    public int lastIndexOf(@Nullable Object object) {
        return indexOf(object);
    }

    public int size() {
        return 1;
    }

    public ImmutableList<E> subList(int fromIndex, int toIndex) {
        Preconditions.checkPositionIndexes(fromIndex, toIndex, 1);
        return fromIndex == toIndex ? ImmutableList.of() : this;
    }

    public ImmutableList<E> reverse() {
        return this;
    }

    public boolean contains(@Nullable Object object) {
        return this.element.equals(object);
    }

    public boolean equals(@Nullable Object object) {
        boolean z = false;
        if (object == this) {
            return true;
        }
        if (!(object instanceof List)) {
            return false;
        }
        List<?> that = (List) object;
        if (that.size() == 1) {
            z = this.element.equals(that.get(0));
        }
        return z;
    }

    public int hashCode() {
        return this.element.hashCode() + 31;
    }

    public String toString() {
        String elementToString = this.element.toString();
        return new StringBuilder(elementToString.length() + 2).append('[').append(elementToString).append(']').toString();
    }

    public boolean isEmpty() {
        return false;
    }

    boolean isPartialView() {
        return false;
    }

    int copyIntoArray(Object[] dst, int offset) {
        dst[offset] = this.element;
        return offset + 1;
    }
}
