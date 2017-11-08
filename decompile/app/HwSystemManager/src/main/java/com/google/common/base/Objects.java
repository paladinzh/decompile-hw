package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.javax.annotation.CheckReturnValue;
import com.google.javax.annotation.Nullable;
import java.util.Arrays;

@GwtCompatible
public final class Objects {
    private Objects() {
    }

    @CheckReturnValue
    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        if (a != b) {
            return a != null ? a.equals(b) : false;
        } else {
            return true;
        }
    }

    public static int hashCode(@Nullable Object... objects) {
        return Arrays.hashCode(objects);
    }
}
