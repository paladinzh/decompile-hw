package com.fyusion.sdk.common.ext.util.exif;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/* compiled from: Unknown */
class b extends FilterInputStream {
    static final /* synthetic */ boolean a;
    private int b = 0;
    private final byte[] c = new byte[8];
    private final ByteBuffer d = ByteBuffer.wrap(this.c);

    static {
        boolean z = false;
        if (!b.class.desiredAssertionStatus()) {
            z = true;
        }
        a = z;
    }

    protected b(InputStream inputStream) {
        super(inputStream);
    }

    public int a() {
        return this.b;
    }

    public String a(int i, Charset charset) throws IOException {
        byte[] bArr = new byte[i];
        a(bArr);
        return new String(bArr, charset);
    }

    public void a(long j) throws IOException {
        if (skip(j) != j) {
            throw new EOFException();
        }
    }

    public void a(ByteOrder byteOrder) {
        this.d.order(byteOrder);
    }

    public void a(byte[] bArr) throws IOException {
        a(bArr, 0, bArr.length);
    }

    public void a(byte[] bArr, int i, int i2) throws IOException {
        if (read(bArr, i, i2) != i2) {
            throw new EOFException();
        }
    }

    public ByteOrder b() {
        return this.d.order();
    }

    public void b(long j) throws IOException {
        Object obj = null;
        long j2 = j - ((long) this.b);
        if (!a) {
            if (j2 >= 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        a(j2);
    }

    public short c() throws IOException {
        a(this.c, 0, 2);
        this.d.rewind();
        return this.d.getShort();
    }

    public int d() throws IOException {
        return c() & 65535;
    }

    public int e() throws IOException {
        a(this.c, 0, 4);
        this.d.rewind();
        return this.d.getInt();
    }

    public long f() throws IOException {
        return ((long) e()) & 4294967295L;
    }

    public int read() throws IOException {
        int i = 0;
        int read = this.in.read();
        int i2 = this.b;
        if (read >= 0) {
            i = 1;
        }
        this.b = i + i2;
        return read;
    }

    public int read(byte[] bArr) throws IOException {
        int i = 0;
        int read = this.in.read(bArr);
        int i2 = this.b;
        if (read >= 0) {
            i = read;
        }
        this.b = i + i2;
        return read;
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        int read = this.in.read(bArr, i, i2);
        int i4 = this.b;
        if (read >= 0) {
            i3 = read;
        }
        this.b = i3 + i4;
        return read;
    }

    public long skip(long j) throws IOException {
        long skip = this.in.skip(j);
        this.b = (int) (((long) this.b) + skip);
        return skip;
    }
}
