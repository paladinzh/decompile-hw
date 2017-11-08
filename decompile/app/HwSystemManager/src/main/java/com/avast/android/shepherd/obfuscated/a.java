package com.avast.android.shepherd.obfuscated;

import java.io.InputStream;
import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class a extends InputStream {
    private final ByteBuffer a;

    public a(ByteBuffer byteBuffer) {
        this.a = byteBuffer;
    }

    public synchronized int read() {
        if (!this.a.hasRemaining()) {
            return -1;
        }
        return this.a.get() & 255;
    }

    public synchronized int read(byte[] bArr, int i, int i2) {
        int remaining = this.a.remaining();
        if (remaining == 0) {
            return -1;
        }
        remaining = Math.min(i2, remaining);
        this.a.get(bArr, i, remaining);
        return remaining;
    }
}
