package com.android.photos.data;

import android.graphics.Point;
import android.util.Pools.Pool;
import android.util.Pools.SynchronizedPool;

public class GalleryBitmapPool {
    private static final Point[] COMMON_PHOTO_ASPECT_RATIOS = new Point[]{new Point(4, 3), new Point(3, 2), new Point(16, 9)};
    private static GalleryBitmapPool sInstance = new GalleryBitmapPool(20971520);
    private int mCapacityBytes;
    private SparseArrayBitmapPool[] mPools = new SparseArrayBitmapPool[3];
    private Pool<Node> mSharedNodePool = new SynchronizedPool(128);

    private GalleryBitmapPool(int capacityBytes) {
        this.mPools[0] = new SparseArrayBitmapPool(capacityBytes / 3, this.mSharedNodePool);
        this.mPools[1] = new SparseArrayBitmapPool(capacityBytes / 3, this.mSharedNodePool);
        this.mPools[2] = new SparseArrayBitmapPool(capacityBytes / 3, this.mSharedNodePool);
        this.mCapacityBytes = capacityBytes;
    }

    public static GalleryBitmapPool getInstance() {
        return sInstance;
    }

    public void clear() {
        for (SparseArrayBitmapPool p : this.mPools) {
            p.clear();
        }
    }
}
