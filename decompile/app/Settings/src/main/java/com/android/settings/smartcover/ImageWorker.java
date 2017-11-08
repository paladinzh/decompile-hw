package com.android.settings.smartcover;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.android.settings.smartcover.ImageCache.ImageCacheParams;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressLint({"NewApi"})
public abstract class ImageWorker {
    private static volatile Executor sDualExecutor = Executors.newFixedThreadPool(2);
    private boolean mExitTasksEarly = false;
    private boolean mFadeInBitmap = false;
    private ImageCache mImageCache;
    private ImageCacheParams mImageCacheParams;
    protected int mImageHeight;
    protected int mImageWidth;
    private Bitmap mLoadingBitmap;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    protected Resources mResources;

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            this.bitmapWorkerTaskReference = new WeakReference(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return (BitmapWorkerTask) this.bitmapWorkerTaskReference.get();
        }
    }

    private class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
        private Object data;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            this.imageViewReference = new WeakReference(imageView);
        }

        protected Bitmap doInBackground(Object... params) {
            this.data = params[0];
            String dataString = String.valueOf(this.data);
            Bitmap bitmap = null;
            synchronized (ImageWorker.this.mPauseWorkLock) {
                while (ImageWorker.this.mPauseWork && !isCancelled()) {
                    try {
                        ImageWorker.this.mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!(ImageWorker.this.mImageCache == null || isCancelled() || getAttachedImageView() == null || ImageWorker.this.mExitTasksEarly)) {
                bitmap = ImageWorker.this.mImageCache.getBitmapFromDiskCache(dataString);
                if (!(bitmap == null || (bitmap.getHeight() == ImageWorker.this.mImageHeight && bitmap.getWidth() == ImageWorker.this.mImageWidth))) {
                    bitmap = null;
                    ImageWorker.this.mImageCache.removeBitmapFromDiskCache(dataString);
                }
            }
            if (!(bitmap != null || isCancelled() || getAttachedImageView() == null || ImageWorker.this.mExitTasksEarly)) {
                bitmap = ImageWorker.this.processBitmap(params[0]);
            }
            if (!(bitmap == null || ImageWorker.this.mImageCache == null)) {
                ImageWorker.this.mImageCache.addBitmapToCache(dataString, bitmap);
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || ImageWorker.this.mExitTasksEarly) {
                bitmap = null;
            }
            ImageView imageView = getAttachedImageView();
            if (bitmap != null && imageView != null) {
                ImageWorker.this.setImageBitmap(imageView, bitmap);
            }
        }

        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            synchronized (ImageWorker.this.mPauseWorkLock) {
                ImageWorker.this.mPauseWorkLock.notifyAll();
            }
        }

        private ImageView getAttachedImageView() {
            ImageView imageView = (ImageView) this.imageViewReference.get();
            if (this == ImageWorker.getBitmapWorkerTask(imageView)) {
                return imageView;
            }
            return null;
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {
        protected CacheAsyncTask() {
        }

        protected Void doInBackground(Object... params) {
            switch (((Integer) params[0]).intValue()) {
                case 0:
                    ImageWorker.this.clearCacheInternal();
                    break;
                case 1:
                    ImageWorker.this.initDiskCacheInternal();
                    break;
                case 2:
                    ImageWorker.this.flushCacheInternal();
                    break;
                case 3:
                    ImageWorker.this.closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected abstract Bitmap processBitmap(Object obj);

    protected ImageWorker(Context context) {
        this.mResources = context.getResources();
    }

    public void clearMemoryCache() {
        if (this.mImageCache != null) {
            this.mImageCache.removeBitmapFromCache();
        }
    }

    public void loadImage(Object data, ImageView imageView) {
        if (data != null && imageView != null) {
            Bitmap bitmap = null;
            if (this.mImageCache != null) {
                bitmap = this.mImageCache.getBitmapFromMemCache(String.valueOf(data));
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else if (cancelPotentialWork(data, imageView)) {
                BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                imageView.setImageDrawable(new AsyncDrawable(this.mResources, this.mLoadingBitmap, task));
                task.executeOnExecutor(sDualExecutor, new Object[]{data});
            }
        }
    }

    public void setLoadingImage(int resId) {
        this.mLoadingBitmap = BitmapFactory.decodeResource(this.mResources, resId);
    }

    public void addImageCache(FragmentManager fragmentManager, ImageCacheParams cacheParams) {
        this.mImageCacheParams = cacheParams;
        if (fragmentManager != null) {
            setImageCache(ImageCache.findOrCreateCache(fragmentManager, this.mImageCacheParams));
        } else {
            setImageCache(new ImageCache(this.mImageCacheParams));
        }
        new CacheAsyncTask().execute(new Object[]{Integer.valueOf(1)});
    }

    public void setImageCache(ImageCache imageCache) {
        this.mImageCache = imageCache;
    }

    public void setImageFadeIn(boolean fadeIn) {
        this.mFadeInBitmap = fadeIn;
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData != null && bitmapData.equals(data)) {
                return false;
            }
            bitmapWorkerTask.cancel(true);
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable) drawable).getBitmapWorkerTask();
            }
        }
        return null;
    }

    private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (this.mFadeInBitmap) {
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(17170445), new BitmapDrawable(this.mResources, bitmap)});
            imageView.setBackgroundDrawable(new BitmapDrawable(this.mResources, this.mLoadingBitmap));
            imageView.setImageDrawable(td);
            td.startTransition(200);
            return;
        }
        imageView.setImageBitmap(bitmap);
    }

    protected void initDiskCacheInternal() {
        if (this.mImageCache != null) {
            this.mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (this.mImageCache != null) {
            this.mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (this.mImageCache != null) {
            this.mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (this.mImageCache != null) {
            this.mImageCache.close();
            this.mImageCache = null;
        }
    }
}
