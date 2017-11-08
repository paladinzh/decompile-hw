package org.apache.commons.jexl2.internal;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<T> implements Iterator<T> {
    private final Enumeration<T> enumeration;

    public EnumerationIterator(Enumeration<T> enumer) {
        this.enumeration = enumer;
    }

    public T next() {
        return this.enumeration.nextElement();
    }

    public boolean hasNext() {
        return this.enumeration.hasMoreElements();
    }

    public void remove() {
    }
}
