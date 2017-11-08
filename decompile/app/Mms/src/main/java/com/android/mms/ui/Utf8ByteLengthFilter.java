package com.android.mms.ui;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;
import com.google.android.gms.R;

public class Utf8ByteLengthFilter implements InputFilter {
    protected Context mContext;
    private final int mMaxBytes;

    public Utf8ByteLengthFilter(int _maxBytes, Context context) {
        this.mMaxBytes = _maxBytes;
        this.mContext = context;
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
        if (keepBytes >= srcByteCount) {
            return null;
        }
        i = start;
        while (i < end) {
            c = source.charAt(i);
            int charSize = c < '' ? 1 : c < 'ࠀ' ? 2 : 3;
            keepBytes -= charSize;
            if (keepBytes < 0) {
                Toast.makeText(this.mContext, this.mContext.getString(R.string.byte_length_more_than), 0).show();
                if (charSize < 3) {
                    return source.subSequence(start, i);
                }
                return i == 0 ? "" : source.subSequence(start, i - 1);
            }
            i++;
        }
        return null;
    }
}
