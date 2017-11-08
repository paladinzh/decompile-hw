package com.huawei.gallery.refocus.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.IImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AbsPhotoView.Size;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageViewAdapter;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import com.huawei.gallery.app.AbsPhotoPage.Model;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.gallery.refocus.ui.RefocusView;

public class RefocusSinglePhotoDataAdapter extends TileImageViewAdapter implements Model {
    private static final boolean DISPLAY_ENGINE_ENABLE = DisplayEngineUtils.isDisplayEngineEnable();
    private GalleryContext mActivity;
    private BitmapScreenNail mBitmapScreenNail;
    private Handler mHandler;
    private boolean mHasFullImage;
    private MediaItem mItem;
    private FutureListener<BitmapRegionDecoder> mLargeListener = new FutureListener<BitmapRegionDecoder>() {
        public void onFutureDone(Future<BitmapRegionDecoder> future) {
            BitmapRegionDecoder decoder = (BitmapRegionDecoder) future.get();
            if (decoder == null) {
                RefocusSinglePhotoDataAdapter.this.mHasFullImage = false;
                RefocusSinglePhotoDataAdapter.this.mTask = RefocusSinglePhotoDataAdapter.this.mThreadPool.submit(RefocusSinglePhotoDataAdapter.this.mItem.requestImage(1), RefocusSinglePhotoDataAdapter.this.mThumbListener);
                return;
            }
            Bitmap bitmap = RefocusSinglePhotoDataAdapter.this.getThumbnailFromDecoder(decoder);
            if (RefocusSinglePhotoDataAdapter.DISPLAY_ENGINE_ENABLE) {
                bitmap = DisplayEngineUtils.processScreenNailACE(bitmap, RefocusSinglePhotoDataAdapter.this.mItem, RefocusSinglePhotoDataAdapter.this.mScreenNailCommonDisplayEnginePool);
            }
            RefocusSinglePhotoDataAdapter.this.mHandler.sendMessage(RefocusSinglePhotoDataAdapter.this.mHandler.obtainMessage(1, new ImageBundle(decoder, bitmap)));
        }
    };
    private int mLoadingState = 0;
    private RefocusView mRefocusView;
    private ScreenNailCommonDisplayEnginePool mScreenNailCommonDisplayEnginePool = new ScreenNailCommonDisplayEnginePool();
    private Future<?> mTask;
    private ThreadPool mThreadPool;
    private FutureListener<Bitmap> mThumbListener = new FutureListener<Bitmap>() {
        public void onFutureDone(Future<Bitmap> future) {
            Bitmap bitmap = (Bitmap) future.get();
            if (RefocusSinglePhotoDataAdapter.DISPLAY_ENGINE_ENABLE) {
                bitmap = DisplayEngineUtils.processScreenNailACE(bitmap, RefocusSinglePhotoDataAdapter.this.mItem, null);
            }
            RefocusSinglePhotoDataAdapter.this.mHandler.sendMessage(RefocusSinglePhotoDataAdapter.this.mHandler.obtainMessage(1, new ImageBundle(null, bitmap)));
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

    public RefocusSinglePhotoDataAdapter(GalleryContext activity, GLRoot glRoot, RefocusView view, MediaItem item) {
        boolean z;
        this.mItem = (MediaItem) Utils.checkNotNull(item);
        if ((item.getSupportedOperations() & 64) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasFullImage = z;
        this.mRefocusView = (RefocusView) Utils.checkNotNull(view);
        this.mActivity = (GalleryContext) Utils.checkNotNull(activity);
        setGLRoot(glRoot);
        this.mThreadPool = activity.getThreadPool();
    }

    public void setGLRoot(GLRoot glRoot) {
        if (glRoot != null) {
            this.mHandler = new SynchronizedHandler(glRoot) {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case 1:
                            if (RefocusSinglePhotoDataAdapter.this.mHasFullImage) {
                                RefocusSinglePhotoDataAdapter.this.onDecodeLargeComplete((ImageBundle) message.obj);
                                return;
                            } else {
                                RefocusSinglePhotoDataAdapter.this.onDecodeThumbComplete((ImageBundle) message.obj);
                                return;
                            }
                        case 2:
                            if (RefocusSinglePhotoDataAdapter.this.mBitmapScreenNail != null) {
                                RefocusSinglePhotoDataAdapter.this.mBitmapScreenNail.recycle();
                                RefocusSinglePhotoDataAdapter.this.mBitmapScreenNail = null;
                            }
                            RefocusSinglePhotoDataAdapter.this.setScreenNail((Bitmap) message.obj, RefocusSinglePhotoDataAdapter.this.getImageWidth(), RefocusSinglePhotoDataAdapter.this.getImageHeight());
                            RefocusSinglePhotoDataAdapter.this.mRefocusView.notifyImageChange(0);
                            RefocusSinglePhotoDataAdapter.this.mRefocusView.onRefocusRegionDecode();
                            return;
                        case 3:
                            RefocusSinglePhotoDataAdapter.this.setScreenNail((BitmapScreenNail) message.obj, RefocusSinglePhotoDataAdapter.this.getImageWidth(), RefocusSinglePhotoDataAdapter.this.getImageHeight());
                            RefocusSinglePhotoDataAdapter.this.mRefocusView.notifyImageChange(0);
                            RefocusSinglePhotoDataAdapter.this.mRefocusView.refresh();
                            return;
                        default:
                            Utils.assertTrue(false);
                            return;
                    }
                }
            };
        }
    }

    private Bitmap getThumbnailFromDecoder(BitmapRegionDecoder decoder) {
        int width = decoder.getWidth();
        int height = decoder.getHeight();
        int targetSize = Math.max(GalleryUtils.getWidthPixels(), GalleryUtils.getHeightPixels()) / 2;
        float scale = ((float) targetSize) / ((float) Math.max(width, height));
        Options options = new Options();
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
        if (bitmap == null) {
            return null;
        }
        scale = ((float) targetSize) / ((float) Math.max(bitmap.getWidth(), bitmap.getHeight()));
        if (((double) scale) <= 0.5d) {
            bitmap = BitmapUtils.resizeBitmapByScale(bitmap, scale, true);
        }
        return BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
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
            this.mRefocusView.notifyImageChange(0);
            this.mRefocusView.onRefocusRegionDecode();
        } catch (Throwable t) {
            GalleryLog.w("RefocusSinglePhotoDataAdapter", "fail to decode large." + t.getMessage());
            this.mLoadingState = 2;
        }
    }

    private void onDecodeThumbComplete(ImageBundle bundle) {
        try {
            Bitmap backup = bundle.backupImage;
            if (backup == null) {
                this.mLoadingState = 2;
                ContextedUtils.showToastQuickly(this.mActivity.getActivityContext(), (int) R.string.fail_to_load_image_Toast, 1);
                return;
            }
            this.mLoadingState = 1;
            setScreenNail(backup, backup.getWidth(), backup.getHeight());
            this.mRefocusView.notifyImageChange(0);
        } catch (Throwable t) {
            GalleryLog.w("RefocusSinglePhotoDataAdapter", "fail to decode thumb." + t.getMessage());
        }
    }

    public void invalidateData(Bitmap bitmap) {
        if (DISPLAY_ENGINE_ENABLE) {
            bitmap = DisplayEngineUtils.processScreenNailACE(bitmap, this.mItem, null);
        }
        this.mHandler.obtainMessage(2, bitmap).sendToTarget();
    }

    public void invalidateData(BitmapScreenNail bitmapScreenNail) {
        this.mHandler.obtainMessage(3, bitmapScreenNail).sendToTarget();
    }

    public void invalidateData(byte[] bytes, int offset, int length) {
        if (this.mTask != null) {
            Future<?> task = this.mTask;
            task.cancel();
            task.waitDone();
            this.mTask = null;
        }
        if (this.mBitmapScreenNail != null) {
            this.mBitmapScreenNail.recycle();
            this.mBitmapScreenNail = null;
        }
        if (!this.mHasFullImage) {
            this.mTask = this.mThreadPool.submit(this.mItem.requestImage(1), this.mThumbListener);
        } else if (!(this.mItem instanceof IImage)) {
            this.mTask = this.mThreadPool.submit(this.mItem.requestLargeImage(), this.mLargeListener);
        } else if (bytes == null || bytes.length == 0) {
            this.mTask = this.mThreadPool.submit(this.mItem.requestLargeImage(), this.mLargeListener);
        } else {
            this.mTask = this.mThreadPool.submit(((IImage) this.mItem).requestLargeImage(bytes, offset, length), this.mLargeListener);
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

    public void resume(byte[] bytes, int offset, int length) {
        if (this.mTask != null) {
            return;
        }
        if (!this.mHasFullImage) {
            this.mTask = this.mThreadPool.submit(this.mItem.requestImage(1), this.mThumbListener);
        } else if (!(this.mItem instanceof IImage)) {
            this.mTask = this.mThreadPool.submit(this.mItem.requestLargeImage(), this.mLargeListener);
        } else if (bytes == null) {
            this.mTask = this.mThreadPool.submit(this.mItem.requestLargeImage(), this.mLargeListener);
        } else {
            this.mTask = this.mThreadPool.submit(((IImage) this.mItem).requestLargeImage(bytes, offset, length), this.mLargeListener);
        }
    }

    public void pause() {
        Future<?> task = this.mTask;
        Future<?> microThumbTask = this.mThreadPool.submit(this.mItem.requestImage(2), null);
        task.cancel();
        task.waitDone();
        if (task.get() == null) {
            this.mTask = null;
        }
        if (this.mBitmapScreenNail != null) {
            this.mBitmapScreenNail.recycle();
            this.mBitmapScreenNail = null;
        }
        Future<?> thumbTask = this.mThreadPool.submit(this.mItem.requestImage(1), null);
        microThumbTask.waitDone();
        thumbTask.waitDone();
    }

    public int getImageRotation(int offset) {
        return offset == 0 ? this.mItem.getFullImageRotation() : 0;
    }

    public ScreenNail getScreenNail(int offset) {
        return offset == 0 ? getScreenNail() : null;
    }

    public void setNeedFullImage(boolean enabled) {
    }

    public MediaItem getMediaItem(int offset) {
        return offset == 0 ? this.mItem : null;
    }

    public int getCurrentIndex() {
        GalleryLog.printDFXLog("RefocusSinglePhotoDataAdapter.getCurrentIndex");
        return 0;
    }

    public void setCurrentPhoto(Path path, int indexHint) {
    }

    public void setFocusHintDirection(int direction) {
        GalleryLog.printDFXLog("RefocusSinglePhotoDataAdapter.setFocusHintDirection");
    }

    public boolean isLCDDownloaded() {
        GalleryLog.printDFXLog("RefocusSinglePhotoDataAdapter.isLCDDownloaded");
        return true;
    }

    public int getLoadingState(int offset) {
        return this.mLoadingState;
    }

    public void moveTo(int index, int maskOffset) {
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

    public boolean isCamera(int offset) {
        GalleryLog.v("RefocusSinglePhotoDataAdapter", "is camera");
        return false;
    }

    public boolean isPanorama(int offset) {
        GalleryLog.v("RefocusSinglePhotoDataAdapter", "is not panorama photo");
        return false;
    }

    public boolean isStaticCamera(int offset) {
        GalleryLog.v("RefocusSinglePhotoDataAdapter", "is not static camera");
        return false;
    }

    public boolean isVideo(int offset) {
        GalleryLog.v("RefocusSinglePhotoDataAdapter", "is not video");
        return false;
    }

    public MediaItem getCurrentMediaItem() {
        return this.mItem;
    }

    public ScreenNailCommonDisplayEnginePool getScreenNailCommonDisplayEnginePool() {
        return this.mScreenNailCommonDisplayEnginePool;
    }
}
