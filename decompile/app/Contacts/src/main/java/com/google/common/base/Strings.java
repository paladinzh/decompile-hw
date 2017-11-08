package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;

@GwtCompatible
public final class Strings {
    private Strings() {
    }

    public static String nullToEmpty(@Nullable String string) {
        return string == null ? "" : string;
    }

    @VisibleForTesting
    static boolean validSurrogatePairAt(CharSequence string, int index) {
        if (index < 0 || index > string.length() - 2 || !Character.isHighSurrogate(string.charAt(index))) {
            return false;
        }
        return Character.isLowSurrogate(string.charAt(index + 1));
    }
}
