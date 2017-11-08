package com.android.gallery3d.util;

import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.common.Utils;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

public class InterruptableOutputStream extends OutputStream {
    private volatile boolean mIsInterrupted = false;
    private OutputStream mOutputStream;

    public InterruptableOutputStream(OutputStream outputStream) {
        this.mOutputStream = (OutputStream) Utils.checkNotNull(outputStream);
    }

    public void write(int oneByte) throws IOException {
        if (this.mIsInterrupted) {
            throw new InterruptedIOException();
        }
        this.mOutputStream.write(oneByte);
    }

    public void write(byte[] buffer, int offset, int count) throws IOException {
        int end = offset + count;
        while (offset < end) {
            if (this.mIsInterrupted) {
                throw new InterruptedIOException();
            }
            int bytesCount = Math.min(FragmentTransaction.TRANSIT_ENTER_MASK, end - offset);
            this.mOutputStream.write(buffer, offset, bytesCount);
            offset += bytesCount;
        }
    }

    public void close() throws IOException {
        this.mOutputStream.close();
    }

    public void flush() throws IOException {
        if (this.mIsInterrupted) {
            throw new InterruptedIOException();
        }
        this.mOutputStream.flush();
    }

    public void interrupt() {
        this.mIsInterrupted = true;
    }
}
