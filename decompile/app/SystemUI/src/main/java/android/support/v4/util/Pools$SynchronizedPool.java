package android.support.v4.util;

public class Pools$SynchronizedPool<T> extends Pools$SimplePool<T> {
    private final Object mLock = new Object();

    public Pools$SynchronizedPool(int maxPoolSize) {
        super(maxPoolSize);
    }

    public T acquire() {
        T acquire;
        synchronized (this.mLock) {
            acquire = super.acquire();
        }
        return acquire;
    }

    public boolean release(T element) {
        boolean release;
        synchronized (this.mLock) {
            release = super.release(element);
        }
        return release;
    }
}
