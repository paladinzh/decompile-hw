package org.apache.commons.jexl2.internal;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator implements Iterator<Object> {
    private final Object array;
    private int pos;
    private final int size;

    public ArrayIterator(Object arr) {
        if (arr == null) {
            this.array = null;
            this.pos = 0;
            this.size = 0;
        } else if (arr.getClass().isArray()) {
            this.array = arr;
            this.pos = 0;
            this.size = Array.getLength(this.array);
        } else {
            throw new IllegalArgumentException(arr.getClass() + " is not an array");
        }
    }

    public Object next() {
        if (this.pos >= this.size) {
            throw new NoSuchElementException("No more elements: " + this.pos + " / " + this.size);
        }
        Object obj = this.array;
        int i = this.pos;
        this.pos = i + 1;
        return Array.get(obj, i);
    }

    public boolean hasNext() {
        return this.pos < this.size;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
