package com.google.android.gms.wearable.internal;

import com.google.android.gms.wearable.ChannelIOException;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public final class zzo extends InputStream {
    private final InputStream zzbab;
    private volatile zzl zzbac;

    private int zzkF(int i) throws ChannelIOException {
        if (i == -1) {
            zzl zzl = this.zzbac;
            if (zzl != null) {
                throw new ChannelIOException("Channel closed unexpectedly before stream was finished", zzl.zzaZS, zzl.zzaZT);
            }
        }
        return i;
    }

    public int available() throws IOException {
        return this.zzbab.available();
    }

    public void close() throws IOException {
        this.zzbab.close();
    }

    public void mark(int readlimit) {
        this.zzbab.mark(readlimit);
    }

    public boolean markSupported() {
        return this.zzbab.markSupported();
    }

    public int read() throws IOException {
        return zzkF(this.zzbab.read());
    }

    public int read(byte[] buffer) throws IOException {
        return zzkF(this.zzbab.read(buffer));
    }

    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        return zzkF(this.zzbab.read(buffer, byteOffset, byteCount));
    }

    public void reset() throws IOException {
        this.zzbab.reset();
    }

    public long skip(long byteCount) throws IOException {
        return this.zzbab.skip(byteCount);
    }
}
