package com.avast.android.sdk.internal;

import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/* compiled from: Unknown */
public class e<E> extends AbstractQueue<E> implements b<E>, Serializable {
    transient d<E> a;
    transient d<E> b;
    final ReentrantLock c;
    private transient int d;
    private final int e;
    private final Condition f;
    private final Condition g;

    /* compiled from: Unknown */
    private abstract class a implements Iterator<E> {
        d<E> a;
        E b;
        final /* synthetic */ e c;
        private d<E> d;

        a(e eVar) {
            Object obj = null;
            this.c = eVar;
            ReentrantLock reentrantLock = eVar.c;
            reentrantLock.lock();
            try {
                this.a = a();
                if (this.a != null) {
                    obj = this.a.a;
                }
                this.b = obj;
            } finally {
                reentrantLock.unlock();
            }
        }

        private d<E> b(d<E> dVar) {
            while (true) {
                d<E> a = a(dVar);
                if (a == null) {
                    return null;
                }
                if (a.a != null) {
                    return a;
                }
                if (a == dVar) {
                    return a();
                }
                dVar = a;
            }
        }

        abstract d<E> a();

        abstract d<E> a(d<E> dVar);

        void b() {
            Object obj = null;
            ReentrantLock reentrantLock = this.c.c;
            reentrantLock.lock();
            try {
                this.a = b(this.a);
                if (this.a != null) {
                    obj = this.a.a;
                }
                this.b = obj;
            } finally {
                reentrantLock.unlock();
            }
        }

        public boolean hasNext() {
            return this.a != null;
        }

        public E next() {
            if (this.a != null) {
                this.d = this.a;
                E e = this.b;
                b();
                return e;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            d dVar = this.d;
            if (dVar != null) {
                this.d = null;
                ReentrantLock reentrantLock = this.c.c;
                reentrantLock.lock();
                try {
                    if (dVar.a != null) {
                        this.c.a(dVar);
                    }
                    reentrantLock.unlock();
                } catch (Throwable th) {
                    reentrantLock.unlock();
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /* compiled from: Unknown */
    private class b extends a {
        final /* synthetic */ e d;

        private b(e eVar) {
            this.d = eVar;
            super(eVar);
        }

        d<E> a() {
            return this.d.b;
        }

        d<E> a(d<E> dVar) {
            return dVar.b;
        }
    }

    /* compiled from: Unknown */
    private class c extends a {
        final /* synthetic */ e d;

        private c(e eVar) {
            this.d = eVar;
            super(eVar);
        }

        d<E> a() {
            return this.d.a;
        }

        d<E> a(d<E> dVar) {
            return dVar.c;
        }
    }

    /* compiled from: Unknown */
    static final class d<E> {
        E a;
        d<E> b;
        d<E> c;

        d(E e) {
            this.a = e;
        }
    }

    public e() {
        this(SpaceConst.SCANNER_TYPE_ALL);
    }

    public e(int i) {
        this.c = new ReentrantLock();
        this.f = this.c.newCondition();
        this.g = this.c.newCondition();
        if (i > 0) {
            this.e = i;
            return;
        }
        throw new IllegalArgumentException();
    }

    private E b() {
        d dVar = this.a;
        if (dVar == null) {
            return null;
        }
        d dVar2 = dVar.c;
        E e = dVar.a;
        dVar.a = null;
        dVar.c = dVar;
        this.a = dVar2;
        if (dVar2 != null) {
            dVar2.b = null;
        } else {
            this.b = null;
        }
        this.d--;
        this.g.signal();
        return e;
    }

    private boolean b(d<E> dVar) {
        if (this.d >= this.e) {
            return false;
        }
        d dVar2 = this.a;
        dVar.c = dVar2;
        this.a = dVar;
        if (this.b != null) {
            dVar2.b = dVar;
        } else {
            this.b = dVar;
        }
        this.d++;
        this.f.signal();
        return true;
    }

    private E c() {
        d dVar = this.b;
        if (dVar == null) {
            return null;
        }
        d dVar2 = dVar.b;
        E e = dVar.a;
        dVar.a = null;
        dVar.b = dVar;
        this.b = dVar2;
        if (dVar2 != null) {
            dVar2.c = null;
        } else {
            this.a = null;
        }
        this.d--;
        this.g.signal();
        return e;
    }

    private boolean c(d<E> dVar) {
        if (this.d >= this.e) {
            return false;
        }
        d dVar2 = this.b;
        dVar.b = dVar2;
        this.b = dVar;
        if (this.a != null) {
            dVar2.c = dVar;
        } else {
            this.a = dVar;
        }
        this.d++;
        this.f.signal();
        return true;
    }

    public E a() throws InterruptedException {
        E b;
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        while (true) {
            try {
                b = b();
                if (b != null) {
                    break;
                }
                this.f.await();
            } finally {
                reentrantLock.unlock();
            }
        }
        return b;
    }

    public E a(long j, TimeUnit timeUnit) throws InterruptedException {
        long toNanos = timeUnit.toNanos(j);
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lockInterruptibly();
        while (true) {
            long j2 = toNanos;
            E b = b();
            if (b != null) {
                reentrantLock.unlock();
                return b;
            }
            if ((j2 > 0 ? 1 : null) == null) {
                break;
            }
            try {
                toNanos = this.f.awaitNanos(j2);
            } finally {
                reentrantLock.unlock();
            }
        }
        return null;
    }

    void a(d<E> dVar) {
        d dVar2 = dVar.b;
        d dVar3 = dVar.c;
        if (dVar2 == null) {
            b();
        } else if (dVar3 != null) {
            dVar2.c = dVar3;
            dVar3.b = dVar2;
            dVar.a = null;
            this.d--;
            this.g.signal();
        } else {
            c();
        }
    }

    public void a(E e) throws InterruptedException {
        if (e != null) {
            d dVar = new d(e);
            ReentrantLock reentrantLock = this.c;
            reentrantLock.lock();
            while (!c(dVar)) {
                try {
                    this.g.await();
                } finally {
                    reentrantLock.unlock();
                }
            }
            return;
        }
        throw new NullPointerException();
    }

    public boolean a(E e, long j, TimeUnit timeUnit) throws InterruptedException {
        if (e != null) {
            d dVar = new d(e);
            long toNanos = timeUnit.toNanos(j);
            ReentrantLock reentrantLock = this.c;
            reentrantLock.lockInterruptibly();
            while (!c(dVar)) {
                if (!(toNanos > 0)) {
                    return false;
                }
                try {
                    toNanos = this.g.awaitNanos(toNanos);
                } finally {
                    reentrantLock.unlock();
                }
            }
            reentrantLock.unlock();
            return true;
        }
        throw new NullPointerException();
    }

    public boolean add(E e) {
        addLast(e);
        return true;
    }

    public void addFirst(E e) {
        if (!offerFirst(e)) {
            throw new IllegalStateException("Deque full");
        }
    }

    public void addLast(E e) {
        if (!offerLast(e)) {
            throw new IllegalStateException("Deque full");
        }
    }

    public void clear() {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            d dVar = this.a;
            while (dVar != null) {
                dVar.a = null;
                d dVar2 = dVar.c;
                dVar.b = null;
                dVar.c = null;
                dVar = dVar2;
            }
            this.b = null;
            this.a = null;
            this.d = 0;
            this.g.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    public boolean contains(Object obj) {
        if (obj == null) {
            return false;
        }
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            for (d dVar = this.a; dVar != null; dVar = dVar.c) {
                if (obj.equals(dVar.a)) {
                    return true;
                }
            }
            reentrantLock.unlock();
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }

    public Iterator<E> descendingIterator() {
        return new b();
    }

    public int drainTo(Collection<? super E> collection) {
        return drainTo(collection, SpaceConst.SCANNER_TYPE_ALL);
    }

    public int drainTo(Collection<? super E> collection, int i) {
        int i2 = 0;
        if (collection == null) {
            throw new NullPointerException();
        } else if (collection == this) {
            throw new IllegalArgumentException();
        } else if (i <= 0) {
            return 0;
        } else {
            ReentrantLock reentrantLock = this.c;
            reentrantLock.lock();
            try {
                int min = Math.min(i, this.d);
                while (i2 < min) {
                    collection.add(this.a.a);
                    b();
                    i2++;
                }
                return min;
            } finally {
                reentrantLock.unlock();
            }
        }
    }

    public E element() {
        return getFirst();
    }

    public E getFirst() {
        E peekFirst = peekFirst();
        if (peekFirst != null) {
            return peekFirst;
        }
        throw new NoSuchElementException();
    }

    public E getLast() {
        E peekLast = peekLast();
        if (peekLast != null) {
            return peekLast;
        }
        throw new NoSuchElementException();
    }

    public Iterator<E> iterator() {
        return new c();
    }

    public boolean offer(E e) {
        return offerLast(e);
    }

    public boolean offer(E e, long j, TimeUnit timeUnit) throws InterruptedException {
        return a(e, j, timeUnit);
    }

    public boolean offerFirst(E e) {
        if (e != null) {
            d dVar = new d(e);
            ReentrantLock reentrantLock = this.c;
            reentrantLock.lock();
            try {
                boolean b = b(dVar);
                return b;
            } finally {
                reentrantLock.unlock();
            }
        } else {
            throw new NullPointerException();
        }
    }

    public boolean offerLast(E e) {
        if (e != null) {
            d dVar = new d(e);
            ReentrantLock reentrantLock = this.c;
            reentrantLock.lock();
            try {
                boolean c = c(dVar);
                return c;
            } finally {
                reentrantLock.unlock();
            }
        } else {
            throw new NullPointerException();
        }
    }

    public E peek() {
        return peekFirst();
    }

    public E peekFirst() {
        E e = null;
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            if (this.a != null) {
                e = this.a.a;
            }
            reentrantLock.unlock();
            return e;
        } catch (Throwable th) {
            reentrantLock.unlock();
        }
    }

    public E peekLast() {
        E e = null;
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            if (this.b != null) {
                e = this.b.a;
            }
            reentrantLock.unlock();
            return e;
        } catch (Throwable th) {
            reentrantLock.unlock();
        }
    }

    public E poll() {
        return pollFirst();
    }

    public E poll(long j, TimeUnit timeUnit) throws InterruptedException {
        return a(j, timeUnit);
    }

    public E pollFirst() {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            E b = b();
            return b;
        } finally {
            reentrantLock.unlock();
        }
    }

    public E pollLast() {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            E c = c();
            return c;
        } finally {
            reentrantLock.unlock();
        }
    }

    public E pop() {
        return removeFirst();
    }

    public void push(E e) {
        addFirst(e);
    }

    public void put(E e) throws InterruptedException {
        a((Object) e);
    }

    public int remainingCapacity() {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            int i = this.e - this.d;
            return i;
        } finally {
            reentrantLock.unlock();
        }
    }

    public E remove() {
        return removeFirst();
    }

    public boolean remove(Object obj) {
        return removeFirstOccurrence(obj);
    }

    public E removeFirst() {
        E pollFirst = pollFirst();
        if (pollFirst != null) {
            return pollFirst;
        }
        throw new NoSuchElementException();
    }

    public boolean removeFirstOccurrence(Object obj) {
        if (obj == null) {
            return false;
        }
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            for (d dVar = this.a; dVar != null; dVar = dVar.c) {
                if (obj.equals(dVar.a)) {
                    a(dVar);
                    return true;
                }
            }
            reentrantLock.unlock();
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }

    public E removeLast() {
        E pollLast = pollLast();
        if (pollLast != null) {
            return pollLast;
        }
        throw new NoSuchElementException();
    }

    public boolean removeLastOccurrence(Object obj) {
        if (obj == null) {
            return false;
        }
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            for (d dVar = this.b; dVar != null; dVar = dVar.b) {
                if (obj.equals(dVar.a)) {
                    a(dVar);
                    return true;
                }
            }
            reentrantLock.unlock();
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }

    public int size() {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            int i = this.d;
            return i;
        } finally {
            reentrantLock.unlock();
        }
    }

    public E take() throws InterruptedException {
        return a();
    }

    public Object[] toArray() {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            Object[] objArr = new Object[this.d];
            int i = 0;
            d dVar = this.a;
            while (dVar != null) {
                int i2 = i + 1;
                objArr[i] = dVar.a;
                dVar = dVar.c;
                i = i2;
            }
            return objArr;
        } finally {
            reentrantLock.unlock();
        }
    }

    public <T> T[] toArray(T[] tArr) {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            if (tArr.length < this.d) {
                tArr = (Object[]) Array.newInstance(tArr.getClass().getComponentType(), this.d);
            }
            int i = 0;
            d dVar = this.a;
            while (dVar != null) {
                int i2 = i + 1;
                tArr[i] = dVar.a;
                dVar = dVar.c;
                i = i2;
            }
            if (tArr.length > i) {
                tArr[i] = null;
            }
            reentrantLock.unlock();
            return tArr;
        } catch (Throwable th) {
            reentrantLock.unlock();
        }
    }

    public String toString() {
        ReentrantLock reentrantLock = this.c;
        reentrantLock.lock();
        try {
            d dVar = this.a;
            String stringBuilder;
            if (dVar != null) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append('[');
                while (true) {
                    d dVar2 = dVar;
                    Object obj = dVar2.a;
                    if (obj == this) {
                        obj = "(this Collection)";
                    }
                    stringBuilder2.append(obj);
                    dVar = dVar2.c;
                    if (dVar == null) {
                        break;
                    }
                    stringBuilder2.append(',').append(' ');
                }
                stringBuilder = stringBuilder2.append(']').toString();
                return stringBuilder;
            }
            stringBuilder = "[]";
            reentrantLock.unlock();
            return stringBuilder;
        } finally {
            reentrantLock.unlock();
        }
    }
}
