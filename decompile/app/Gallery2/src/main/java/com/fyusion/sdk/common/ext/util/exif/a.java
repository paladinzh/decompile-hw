package com.fyusion.sdk.common.ext.util.exif;

import java.io.InputStream;
import java.nio.ByteBuffer;

/* compiled from: Unknown */
class a extends InputStream {
    private ByteBuffer a;

    public a(ByteBuffer byteBuffer) {
        this.a = byteBuffer;
    }

    public int read() {
        return this.a.hasRemaining() ? this.a.get() & 255 : -1;
    }

    public int read(byte[] bArr, int i, int i2) {
        if (!this.a.hasRemaining()) {
            return -1;
        }
        int min = Math.min(i2, this.a.remaining());
        this.a.get(bArr, i, min);
        return min;
    }
}
