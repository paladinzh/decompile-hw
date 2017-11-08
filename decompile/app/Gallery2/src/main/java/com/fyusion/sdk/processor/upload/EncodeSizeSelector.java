package com.fyusion.sdk.processor.upload;

import com.fyusion.sdk.common.ext.Size;
import com.fyusion.sdk.common.ext.j;

/* compiled from: Unknown */
public interface EncodeSizeSelector {
    public static final EncodeSizeSelector DEFAULT = new a();

    /* compiled from: Unknown */
    public static class a implements EncodeSizeSelector {
        public Size select(int i) {
            return i < j.b.height ? j.d : j.c;
        }
    }

    Size select(int i);
}
