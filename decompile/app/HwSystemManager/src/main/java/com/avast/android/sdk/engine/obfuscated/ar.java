package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.ProgressObserver;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public class ar extends InputStream {
    private InputStream a;
    private long b;
    private long c = 0;
    private ProgressObserver d;

    public ar(InputStream inputStream, long j, ProgressObserver progressObserver) {
        this.a = inputStream;
        this.b = j;
        this.d = progressObserver;
    }

    public int available() throws IOException {
        return this.a.available();
    }

    public void close() throws IOException {
        this.a.close();
    }

    public void mark(int i) {
        this.a.mark(i);
    }

    public boolean markSupported() {
        return this.a.markSupported();
    }

    public int read() throws IOException {
        int read = this.a.read();
        if (!(this.d == null || read == -1)) {
            if ((this.b <= 0 ? 1 : null) == null) {
                ProgressObserver progressObserver = this.d;
                long j = this.c + 1;
                this.c = j;
                progressObserver.onProgressChanged(j, this.b);
            }
        }
        return read;
    }

    public int read(byte[] bArr) throws IOException {
        int read = this.a.read(bArr);
        if (!(this.d == null || read == -1)) {
            if ((this.b <= 0 ? 1 : null) == null) {
                this.c += (long) read;
                this.d.onProgressChanged(this.c, this.b);
            }
        }
        return read;
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        int read = this.a.read(bArr, i, i2);
        if (!(this.d == null || read == -1)) {
            if ((this.b <= 0 ? 1 : null) == null) {
                this.c += (long) read;
                this.d.onProgressChanged(this.c, this.b);
            }
        }
        return read;
    }

    public synchronized void reset() throws IOException {
        this.a.reset();
    }

    public long skip(long j) throws IOException {
        return this.a.skip(j);
    }
}
