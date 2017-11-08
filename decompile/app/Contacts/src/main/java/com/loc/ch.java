package com.loc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/* compiled from: BinaryRandomAccessFile */
public class ch {
    private ByteArrayInputStream a;
    private long b;
    private boolean c = false;
    private RandomAccessFile d = null;
    private boolean e = false;
    private final byte[] f = new byte[8];
    private a g;
    private String h = null;

    /* compiled from: BinaryRandomAccessFile */
    public static class a {
        public boolean a = true;
        public boolean b = true;
    }

    public ch(File file, a aVar) throws IOException, FileNotFoundException, OutOfMemoryError {
        if (aVar != null) {
            if (!aVar.a) {
                this.d = new RandomAccessFile(file, "r");
                this.c = true;
            } else {
                byte[] a = cw.a(file);
                this.a = new ByteArrayInputStream(a);
                this.b = (long) a.length;
                this.c = false;
                this.h = file.getAbsolutePath();
            }
            this.g = aVar;
        }
    }

    private void h() throws IOException {
        if (this.e) {
            throw new IOException("file closed");
        }
    }

    public void a(long j) throws IOException {
        Object obj = null;
        if (j >= 0) {
            obj = 1;
        }
        if (obj == null) {
            throw new IOException("offset < 0: " + j);
        }
        h();
        if (this.c) {
            this.d.seek(j);
            return;
        }
        this.a.reset();
        this.a.skip(j);
    }

    public boolean a() {
        return this.g != null ? this.g.a : false;
    }

    public void b() throws IOException {
        synchronized (this) {
            if (this.c) {
                if (this.d != null) {
                    this.d.close();
                    this.d = null;
                }
            } else if (this.a != null) {
                this.a.close();
                this.a = null;
            }
            this.e = true;
        }
    }

    public final long c() throws IOException {
        h();
        if (this.c) {
            return this.d.readLong();
        }
        this.a.read(this.f);
        return cw.b(this.f);
    }

    public final int d() throws IOException {
        h();
        if (this.c) {
            return this.d.readUnsignedShort();
        }
        this.a.read(this.f, 0, 2);
        return cw.c(this.f);
    }

    public final int e() throws IOException {
        h();
        if (this.c) {
            return this.d.readInt();
        }
        this.a.read(this.f, 0, 4);
        return cw.d(this.f);
    }

    public final int f() throws IOException {
        h();
        return !this.c ? this.a.read() : this.d.readUnsignedByte();
    }

    protected void finalize() throws Throwable {
        b();
        super.finalize();
    }

    public long g() throws IOException {
        if (!this.e) {
            return !this.c ? this.b : this.d.length();
        } else {
            throw new IOException("file closed");
        }
    }
}
