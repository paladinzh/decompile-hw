package com.android.photos.data;

import android.graphics.Bitmap;
import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import android.util.SparseArray;

public class SparseArrayBitmapPool {
    private int mCapacityBytes;
    private Pool<Node> mNodePool;
    private Node mPoolNodesHead = null;
    private Node mPoolNodesTail = null;
    private int mSizeBytes = 0;
    private SparseArray<Node> mStore = new SparseArray();

    protected static class Node {
        Bitmap bitmap;
        Node nextInBucket;
        Node nextInPool;
        Node prevInBucket;
        Node prevInPool;

        protected Node() {
        }
    }

    public SparseArrayBitmapPool(int capacityBytes, Pool<Node> nodePool) {
        this.mCapacityBytes = capacityBytes;
        if (nodePool == null) {
            this.mNodePool = new SimplePool(32);
        } else {
            this.mNodePool = nodePool;
        }
    }

    private void freeUpCapacity(int bytesNeeded) {
        int targetSize = this.mCapacityBytes - bytesNeeded;
        while (this.mPoolNodesTail != null && this.mSizeBytes > targetSize) {
            unlinkAndRecycleNode(this.mPoolNodesTail, true);
        }
    }

    private void unlinkAndRecycleNode(Node n, boolean recycleBitmap) {
        if (n.prevInBucket != null) {
            n.prevInBucket.nextInBucket = n.nextInBucket;
        } else {
            this.mStore.put(n.bitmap.getWidth(), n.nextInBucket);
        }
        if (n.nextInBucket != null) {
            n.nextInBucket.prevInBucket = n.prevInBucket;
        }
        if (n.prevInPool != null) {
            n.prevInPool.nextInPool = n.nextInPool;
        } else {
            this.mPoolNodesHead = n.nextInPool;
        }
        if (n.nextInPool != null) {
            n.nextInPool.prevInPool = n.prevInPool;
        } else {
            this.mPoolNodesTail = n.prevInPool;
        }
        n.nextInBucket = null;
        n.nextInPool = null;
        n.prevInBucket = null;
        n.prevInPool = null;
        this.mSizeBytes -= n.bitmap.getByteCount();
        if (recycleBitmap) {
            n.bitmap.recycle();
        }
        n.bitmap = null;
        this.mNodePool.release(n);
    }

    public synchronized void clear() {
        freeUpCapacity(this.mCapacityBytes);
    }
}
