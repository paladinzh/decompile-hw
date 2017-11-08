package android.support.v4.util;

public interface Pools$Pool<T> {
    T acquire();

    boolean release(T t);
}
