package com.android.gallery3d.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AbsPhotoView;
import com.android.gallery3d.ui.AbsPhotoView.Size;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageViewAdapter;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool;
import com.huawei.gallery.app.AbsPhotoPage.Model;

public class SinglePhotoDataAdapter extends TileImageViewAdapter implements Model {
    private BitmapScreenNail mBitmapScreenNail;
    private Handler mHandler;
    private boolean mHasFullImage;
    private MediaItem mItem;
    private FutureListener<BitmapRegionDecoder> mLargeListener = new FutureListener<BitmapRegionDecoder>() {
        public void onFutureDone(Future<BitmapRegionDecoder> future) {
            BitmapRegionDecoder decoder = (BitmapRegionDecoder) future.get();
            if (decoder == null) {
                SinglePhotoDataAdapter.this.mHasFullImage = false;
                SinglePhotoDataAdapter.this.mTask = SinglePhotoDataAdapter.this.mThreadPool.submit(SinglePhotoDataAdapter.this.mItem.requestImage(1), SinglePhotoDataAdapter.this.mThumbListener);
                return;
            }
            int width = decoder.getWidth();
            int height = decoder.getHeight();
            Options options = new Options();
            options.inSampleSize = BitmapUtils.computeSampleSize(1024.0f / ((float) Math.max(width, height)));
            SinglePhotoDataAdapter.this.mHandler.sendMessage(SinglePhotoDataAdapter.this.mHandler.obtainMessage(1, new ImageBundle(decoder, decoder.decodeRegion(new Rect(0, 0, width, height), options))));
        }
    };
    private int mLoadingState = 0;
    private AbsPhotoView mPhotoView;
    private Future<?> mTask;
    private ThreadPool mThreadPool;
    private FutureListener<Bitmap> mThumbListener = new FutureListener<Bitmap>() {
        public void onFutureDone(Future<Bitmap> future) {
            SinglePhotoDataAdapter.this.mHandler.sendMessage(SinglePhotoDataAdapter.this.mHandler.obtainMessage(1, future));
        }
    };

    private static class ImageBundle {
        public final Bitmap backupImage;
        public final BitmapRegionDecoder decoder;

        public ImageBundle(BitmapRegionDecoder decoder, Bitmap backupImage) {
            this.decoder = decoder;
            this.backupImage = backupImage;
        }
    }

    public SinglePhotoDataAdapter(GalleryContext context, GLRoot glRoot, AbsPhotoView view, MediaItem item) {
        boolean z;
        this.mItem = (MediaItem) Utils.checkNotNull(item);
        if ((item.getSupportedOperations() & 64) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasFullImage = z;
        this.mPhotoView = (AbsPhotoView) Utils.checkNotNull(view);
        this.mHandler = new SynchronizedHandler(glRoot) {
            public void handleMessage(Message message) {
                boolean z = true;
                if (message.what != 1) {
                    z = false;
                }
                Utils.assertTrue(z);
                if (SinglePhotoDataAdapter.this.mHasFullImage) {
                    SinglePhotoDataAdapter.this.onDecodeLargeComplete((ImageBundle) message.obj);
                } else {
                    SinglePhotoDataAdapter.this.onDecodeThumbComplete((Future) message.obj);
                }
            }
        };
        this.mThreadPool = context.getThreadPool();
    }

    public boolean isEmpty() {
        return false;
    }

    private void setScreenNail(Bitmap bitmap, int width, int height) {
        this.mBitmapScreenNail = new BitmapScreenNail(bitmap);
        setScreenNail(this.mBitmapScreenNail, width, height);
    }

    private void onDecodeLargeComplete(ImageBundle bundle) {
        try {
            this.mLoadingState = 1;
            setScreenNail(bundle.backupImage, bundle.decoder.getWidth(), bundle.decoder.getHeight());
            setRegionDecoder(bundle.decoder);
            setFilePath(this.mItem.getFilePath());
            this.mPhotoView.notifyImageChange(0);
        } catch (Throwable t) {
            GalleryLog.w("SinglePhotoDataAdapter", "fail to decode large." + t.getMessage());
            this.mLoadingState = 2;
        }
    }

    private void onDecodeThumbComplete(Future<Bitmap> future) {
        try {
            Bitmap backup = (Bitmap) future.get();
            if (backup == null) {
                this.mLoadingState = 2;
                return;
            }
            this.mLoadingState = 1;
            setScreenNail(backup, backup.getWidth(), backup.getHeight());
            this.mPhotoView.notifyImageChange(0);
        } catch (Throwable t) {
            GalleryLog.w("SinglePhotoDataAdapter", "fail to decode thumb." + t.getMessage());
        }
    }

    public void resume() {
        if (this.mTask != null) {
            return;
        }
        if (this.mHasFullImage) {
            this.mTask = this.mThreadPool.submit(this.mItem.requestLargeImage(), this.mLargeListener);
        } else {
            this.mTask = this.mThreadPool.submit(this.mItem.requestImage(1), this.mThumbListener);
        }
    }

    public void pause() {
        Future<?> task = this.mTask;
        task.cancel();
        task.waitDone();
        if (task.get() == null) {
            this.mTask = null;
        }
        if (this.mBitmapScreenNail != null) {
            this.mBitmapScreenNail.recycle();
            this.mBitmapScreenNail = null;
        }
    }

    public void moveTo(int index, int maskOffset) {
        throw new UnsupportedOperationException();
    }

    public void getImageSize(int offset, Size size) {
        if (offset == 0) {
            size.width = this.mItem.getWidth();
            size.height = this.mItem.getHeight();
            return;
        }
        size.width = 0;
        size.height = 0;
    }

    public int getImageRotation(int offset) {
        return offset == 0 ? this.mItem.getFullImageRotation() : 0;
    }

    public ScreenNail getScreenNail(int offset) {
        return offset == 0 ? getScreenNail() : null;
    }

    public void setNeedFullImage(boolean enabled) {
    }

    public boolean isCamera(int offset) {
        return false;
    }

    public boolean isPanorama(int offset) {
        return false;
    }

    public boolean isStaticCamera(int offset) {
        return false;
    }

    public boolean isVideo(int offset) {
        return this.mItem.getMediaType() == 4;
    }

    public MediaItem getMediaItem(int offset) {
        return offset == 0 ? this.mItem : null;
    }

    public int getCurrentIndex() {
        return 0;
    }

    public void setCurrentPhoto(Path path, int indexHint) {
    }

    public void setFocusHintDirection(int direction) {
    }

    public boolean isLCDDownloaded() {
        return true;
    }

    public int getLoadingState(int offset) {
        return this.mLoadingState;
    }

    public void changeSupportFullImage(boolean support) {
        this.mHasFullImage = support;
    }

    public void invalidateData(byte[] bytes, int offset, int length) {
    }

    public void invalidateData(BitmapScreenNail bitmapScreenNail) {
    }

    public void invalidateData(Bitmap bitmap) {
    }

    public void resume(byte[] bytes, int offset, int length) {
    }

    public MediaItem getCurrentMediaItem() {
        return null;
    }
}
