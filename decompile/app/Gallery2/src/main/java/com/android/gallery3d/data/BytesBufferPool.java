package com.android.gallery3d.data;

import java.util.ArrayList;

public class BytesBufferPool {
    private final int mBufferSize;
    private final ArrayList<BytesBuffer> mList;
    private final int mPoolSize;

    public static class BytesBuffer {
        public byte[] data;
        public int length;
        public int offset;

        private BytesBuffer(int capacity) {
            this.data = new byte[capacity];
        }
    }

    public BytesBufferPool(int poolSize, int bufferSize) {
        this.mList = new ArrayList(poolSize);
        this.mPoolSize = poolSize;
        this.mBufferSize = bufferSize;
    }

    public synchronized BytesBuffer get() {
        int n;
        n = this.mList.size();
        return n > 0 ? (BytesBuffer) this.mList.remove(n - 1) : new BytesBuffer(this.mBufferSize);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void recycle(BytesBuffer buffer) {
        if (buffer.data.length == this.mBufferSize) {
            if (this.mList.size() < this.mPoolSize) {
                buffer.offset = 0;
                buffer.length = 0;
                this.mList.add(buffer);
            }
        }
    }

    public synchronized void clear() {
        this.mList.clear();
    }
}
