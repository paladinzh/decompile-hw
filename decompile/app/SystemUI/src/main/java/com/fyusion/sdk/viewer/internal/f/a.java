package com.fyusion.sdk.viewer.internal.f;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public final class a extends FilterInputStream {
    private final long a;
    private int b;

    a(InputStream inputStream, long j) {
        super(inputStream);
        this.a = j;
    }

    private int a(int i) throws IOException {
        Object obj = null;
        if (i < 0) {
            if (this.a - ((long) this.b) <= 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new IOException("Failed to read all expected data, expected: " + this.a + ", but read: " + this.b);
            }
        }
        this.b += i;
        return i;
    }

    public static InputStream a(InputStream inputStream, long j) {
        return new a(inputStream, j);
    }

    public synchronized int available() throws IOException {
        return (int) Math.max(this.a - ((long) this.b), (long) this.in.available());
    }

    public synchronized int read() throws IOException {
        return a(super.read());
    }

    public int read(byte[] bArr) throws IOException {
        return read(bArr, 0, bArr.length);
    }

    public synchronized int read(byte[] bArr, int i, int i2) throws IOException {
        return a(super.read(bArr, i, i2));
    }
}
