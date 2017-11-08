package android.support.v7.content.res;

import java.lang.reflect.Array;

final class GrowingArrayUtils {
    static final /* synthetic */ boolean -assertionsDisabled = (!GrowingArrayUtils.class.desiredAssertionStatus());

    public static <T> T[] append(T[] array, int currentSize, T element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            T[] newArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static int[] append(int[] array, int currentSize, int element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            int[] newArray = new int[growSize(currentSize)];
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }

    private GrowingArrayUtils() {
    }
}
