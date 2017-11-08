package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool.Job;

public interface ThumbnailLoaderListener {
    void onComplete(Object obj);

    void onPreviewLoad(Object obj, Bitmap bitmap);

    Future<Bitmap> submit(Job<Bitmap> job, FutureListener<Bitmap> futureListener, int i);
}
