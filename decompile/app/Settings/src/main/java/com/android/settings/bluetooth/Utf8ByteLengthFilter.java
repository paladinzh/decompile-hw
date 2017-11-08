package com.android.settings.bluetooth;

import android.text.InputFilter;
import android.text.Spanned;

class Utf8ByteLengthFilter implements InputFilter {
    private final int mMaxBytes;

    Utf8ByteLengthFilter(int maxBytes) {
        this.mMaxBytes = maxBytes;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int i;
        int srcByteCount = 0;
        for (i = start; i < end; i++) {
            char c = source.charAt(i);
            int i2 = c < '' ? 1 : c < 'ࠀ' ? 2 : 3;
            srcByteCount += i2;
        }
        int destLen = dest.length();
        int destByteCount = 0;
        i = 0;
        while (i < destLen) {
            if (i < dstart || i >= dend) {
                c = dest.charAt(i);
                i2 = c < '' ? 1 : c < 'ࠀ' ? 2 : 3;
                destByteCount += i2;
            }
            i++;
        }
        int keepBytes = this.mMaxBytes - destByteCount;
        if (keepBytes <= 0) {
            return "";
        }
        if (keepBytes >= srcByteCount) {
            return null;
        }
        for (i = start; i < end; i++) {
            c = source.charAt(i);
            i2 = c < '' ? 1 : c < 'ࠀ' ? 2 : 3;
            keepBytes -= i2;
            if (keepBytes < 0) {
                return source.subSequence(start, i);
            }
        }
        return null;
    }
}
