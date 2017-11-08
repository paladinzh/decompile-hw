package android.support.v4.util;

public class Pools$SimplePool<T> implements Pools$Pool<T> {
    private final Object[] mPool;
    private int mPoolSize;

    public Pools$SimplePool(int maxPoolSize) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("The max pool size must be > 0");
        }
        this.mPool = new Object[maxPoolSize];
    }

    public T acquire() {
        if (this.mPoolSize <= 0) {
            return null;
        }
        int lastPooledIndex = this.mPoolSize - 1;
        T instance = this.mPool[lastPooledIndex];
        this.mPool[lastPooledIndex] = null;
        this.mPoolSize--;
        return instance;
    }

    public boolean release(T instance) {
        if (isInPool(instance)) {
            throw new IllegalStateException("Already in the pool!");
        } else if (this.mPoolSize >= this.mPool.length) {
            return false;
        } else {
            this.mPool[this.mPoolSize] = instance;
            this.mPoolSize++;
            return true;
        }
    }

    private boolean isInPool(T instance) {
        for (int i = 0; i < this.mPoolSize; i++) {
            if (this.mPool[i] == instance) {
                return true;
            }
        }
        return false;
    }
}
