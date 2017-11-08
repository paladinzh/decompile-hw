package com.fyusion.sdk.viewer.internal.b.b.a;

import android.support.v4.app.FragmentTransaction;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import tmsdkobf.fs;

/* compiled from: Unknown */
class n implements Closeable {
    private final InputStream a;
    private final Charset b;
    private byte[] c;
    private int d;
    private int e;

    public n(InputStream inputStream, int i, Charset charset) {
        if (inputStream == null || charset == null) {
            throw new NullPointerException();
        } else if (i < 0) {
            throw new IllegalArgumentException("capacity <= 0");
        } else if (charset.equals(o.a)) {
            this.a = inputStream;
            this.b = charset;
            this.c = new byte[i];
        } else {
            throw new IllegalArgumentException("Unsupported encoding");
        }
    }

    public n(InputStream inputStream, Charset charset) {
        this(inputStream, FragmentTransaction.TRANSIT_EXIT_MASK, charset);
    }

    private void c() throws IOException {
        int read = this.a.read(this.c, 0, this.c.length);
        if (read != -1) {
            this.d = 0;
            this.e = read;
            return;
        }
        throw new EOFException();
    }

    public String a() throws IOException {
        synchronized (this.a) {
            if (this.c != null) {
                int i;
                if (this.d >= this.e) {
                    c();
                }
                int i2 = this.d;
                while (i2 != this.e) {
                    if (this.c[i2] != (byte) 10) {
                        i2++;
                    } else {
                        i = (i2 != this.d && this.c[i2 - 1] == fs.SIMPLE_LIST) ? i2 - 1 : i2;
                        String str = new String(this.c, this.d, i - this.d, this.b.name());
                        this.d = i2 + 1;
                        return str;
                    }
                }
                ByteArrayOutputStream anonymousClass1 = new ByteArrayOutputStream(this, (this.e - this.d) + 80) {
                    final /* synthetic */ n a;

                    public String toString() {
                        int i = (this.count > 0 && this.buf[this.count - 1] == fs.SIMPLE_LIST) ? this.count - 1 : this.count;
                        try {
                            return new String(this.buf, 0, i, this.a.b.name());
                        } catch (UnsupportedEncodingException e) {
                            throw new AssertionError(e);
                        }
                    }
                };
                loop1:
                while (true) {
                    anonymousClass1.write(this.c, this.d, this.e - this.d);
                    this.e = -1;
                    c();
                    i = this.d;
                    while (i != this.e) {
                        if (this.c[i] == (byte) 10) {
                            break loop1;
                        }
                        i++;
                    }
                }
                if (i != this.d) {
                    anonymousClass1.write(this.c, this.d, i - this.d);
                }
                this.d = i + 1;
                String byteArrayOutputStream = anonymousClass1.toString();
                return byteArrayOutputStream;
            }
            throw new IOException("LineReader is closed");
        }
    }

    public boolean b() {
        return this.e == -1;
    }

    public void close() throws IOException {
        synchronized (this.a) {
            if (this.c != null) {
                this.c = null;
                this.a.close();
            }
        }
    }
}
