package org.apache.commons.jexl2.internal;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ArrayListWrapper extends AbstractList<Object> {
    private final Object array;

    public ArrayListWrapper(Object anArray) {
        if (anArray.getClass().isArray()) {
            this.array = anArray;
            return;
        }
        throw new IllegalArgumentException(anArray.getClass() + " is not an array");
    }

    public Object get(int index) {
        return Array.get(this.array, index);
    }

    public Object set(int index, Object element) {
        Object old = get(index);
        Array.set(this.array, index, element);
        return old;
    }

    public int size() {
        return Array.getLength(this.array);
    }

    public Object[] toArray() {
        int size = size();
        Object[] a = new Object[size];
        for (int i = 0; i < size; i++) {
            a[i] = get(i);
        }
        return a;
    }

    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            System.arraycopy(a, a.length, (Object[]) ((Object[]) Array.newInstance(a.getClass().getComponentType(), size)), 0, a.length);
        }
        for (int i = 0; i < size; i++) {
            a[i] = get(i);
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    public int indexOf(Object o) {
        int size = size();
        int i;
        if (o != null) {
            for (i = 0; i < size; i++) {
                if (o.equals(get(i))) {
                    return i;
                }
            }
        } else {
            for (i = 0; i < size; i++) {
                if (get(i) == null) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public boolean isEmpty() {
        return super.isEmpty();
    }

    public Iterator<Object> iterator() {
        return super.iterator();
    }

    public boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    public int lastIndexOf(Object o) {
        return super.lastIndexOf(o);
    }

    public ListIterator<Object> listIterator() {
        return super.listIterator();
    }

    public ListIterator<Object> listIterator(int index) {
        return super.listIterator(index);
    }

    public List<Object> subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean addAll(Collection<? extends Object> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean addAll(int index, Collection<? extends Object> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void add(int index, Object element) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Object remove(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
