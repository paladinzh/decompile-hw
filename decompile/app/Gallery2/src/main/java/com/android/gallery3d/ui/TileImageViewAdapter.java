package com.android.gallery3d.ui;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.TileImageView.Model;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.gallery.util.LayoutHelper;
import java.util.LinkedList;
import java.util.Queue;

public class TileImageViewAdapter implements Model {
    private int mDecoderNum = 0;
    Queue<BitmapRegionDecoder> mDecoderQueue = new LinkedList();
    private String mFilePath;
    protected int mImageHeight;
    protected int mImageWidth;
    protected int mLevelCount;
    protected BitmapRegionDecoder mRegionDecoder;
    protected BitmapRegionDecoder mRegionDecoder2;
    protected ScreenNail mScreenNail;

    public synchronized void clear() {
        this.mScreenNail = null;
        this.mImageWidth = 0;
        this.mImageHeight = 0;
        this.mLevelCount = 0;
        this.mRegionDecoder = null;
        this.mRegionDecoder2 = null;
        this.mDecoderQueue.clear();
        this.mDecoderNum = 0;
        this.mFilePath = null;
        GalleryLog.d("TileImageViewAdapter", "clear()");
    }

    public synchronized void setScreenNail(ScreenNail screenNail, int width, int height) {
        TraceController.traceBegin("TileImageViewAdapter.setScreenNail");
        Utils.checkNotNull(screenNail);
        this.mScreenNail = screenNail;
        this.mImageWidth = width;
        this.mImageHeight = height;
        this.mRegionDecoder = null;
        this.mRegionDecoder2 = null;
        this.mDecoderQueue.clear();
        this.mDecoderNum = 0;
        this.mLevelCount = 0;
        TraceController.traceEnd();
    }

    public synchronized void setRegionDecoder(BitmapRegionDecoder decoder) {
        TraceController.traceBegin("TileImageViewAdapter.setRegionDecoder0");
        this.mRegionDecoder = (BitmapRegionDecoder) Utils.checkNotNull(decoder);
        if (!this.mDecoderQueue.offer(this.mRegionDecoder)) {
            GalleryLog.e("TileImageViewAdapter", "mDecoderQueue.offer(mRegionDecoder) 0 error");
        }
        this.mDecoderNum = this.mDecoderQueue.size();
        this.mImageWidth = decoder.getWidth();
        this.mImageHeight = decoder.getHeight();
        this.mLevelCount = calculateLevelCount();
        TraceController.traceEnd();
    }

    public synchronized void setRegionDecoder(BitmapRegionDecoder decoder, int width, int height) {
        TraceController.traceBegin("TileImageViewAdapter.setRegionDecoder1");
        this.mRegionDecoder = (BitmapRegionDecoder) Utils.checkNotNull(decoder);
        if (!this.mDecoderQueue.offer(this.mRegionDecoder)) {
            GalleryLog.e("TileImageViewAdapter", "mDecoderQueue.offer(mRegionDecoder) error");
        }
        this.mDecoderNum = this.mDecoderQueue.size();
        this.mImageWidth = width;
        this.mImageHeight = height;
        this.mLevelCount = calculateLevelCount();
        TraceController.traceEnd();
    }

    public synchronized void setRegionDecoder2(BitmapRegionDecoder decoder, int width, int height) {
        TraceController.traceBegin("TileImageViewAdapter.setRegionDecoder2");
        this.mRegionDecoder2 = (BitmapRegionDecoder) Utils.checkNotNull(decoder);
        if (!this.mDecoderQueue.offer(this.mRegionDecoder2)) {
            GalleryLog.e("TileImageViewAdapter", "mDecoderQueue.offer(mRegionDecoder2) error");
        }
        this.mDecoderNum = this.mDecoderQueue.size();
        this.mImageWidth = width;
        this.mImageHeight = height;
        this.mLevelCount = calculateLevelCount();
        TraceController.traceEnd();
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    private int calculateLevelCount() {
        int supportRegionDecodeMinSide = LayoutHelper.getScreenShortSide() / 10;
        int minLevel = 1;
        if (this.mImageWidth <= supportRegionDecodeMinSide && this.mImageHeight <= supportRegionDecodeMinSide) {
            minLevel = 0;
        }
        return Math.max(minLevel, Utils.ceilLog2(((float) this.mImageWidth) / ((float) this.mScreenNail.getWidth())));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @TargetApi(11)
    public Bitmap getTile(int level, int x, int y, int tileSize, int borderSize, BitmapPool pool) {
        if (!ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER) {
            return getTileWithoutReusingBitmap(level, x, y, tileSize, borderSize);
        }
        TraceController.traceBegin("TileImageViewAdapter.getTile");
        int b = borderSize << level;
        int t = tileSize << level;
        Rect wantRegion = new Rect(x - b, y - b, (x + t) + b, (y + t) + b);
        BitmapRegionDecoder bitmapRegionDecoder = null;
        synchronized (this) {
            if (this.mDecoderNum > 0) {
                bitmapRegionDecoder = (BitmapRegionDecoder) this.mDecoderQueue.poll();
                while (bitmapRegionDecoder == null) {
                    try {
                        Utils.waitWithoutInterrupt(this);
                        bitmapRegionDecoder = (BitmapRegionDecoder) this.mDecoderQueue.poll();
                        if (bitmapRegionDecoder == null) {
                            GalleryLog.i("TileImageViewAdapter", "get regionDecoder is null after wait");
                        }
                        if (this.mDecoderNum <= 0) {
                            GalleryLog.w("TileImageViewAdapter", "mDecoderNum:" + this.mDecoderNum + ", we need break");
                            break;
                        }
                    } catch (Throwable th) {
                        GalleryLog.w("TileImageViewAdapter", "fail to wait decode. " + th.getMessage());
                    }
                }
            }
            if (bitmapRegionDecoder == null) {
                TraceController.traceEnd();
                return null;
            }
            boolean needClear = !new Rect(0, 0, this.mImageWidth, this.mImageHeight).contains(wantRegion);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Bitmap getTileWithoutReusingBitmap(int level, int x, int y, int tileSize, int borderSize) {
        int b = borderSize << level;
        int t = tileSize << level;
        Rect wantRegion = new Rect(x - b, y - b, (x + t) + b, (y + t) + b);
        TraceController.traceBegin("TileImageViewAdapter.getTileWithoutReusingBitmap");
        BitmapRegionDecoder bitmapRegionDecoder = null;
        synchronized (this) {
            if (this.mDecoderNum > 0) {
                bitmapRegionDecoder = (BitmapRegionDecoder) this.mDecoderQueue.poll();
                while (bitmapRegionDecoder == null) {
                    try {
                        Utils.waitWithoutInterrupt(this);
                        bitmapRegionDecoder = (BitmapRegionDecoder) this.mDecoderQueue.poll();
                        if (bitmapRegionDecoder == null) {
                            GalleryLog.i("TileImageViewAdapter", "getTileWithoutReusingBitmap regionDecoder is null after wait");
                        }
                    } catch (Throwable th) {
                        GalleryLog.w("TileImageViewAdapter", "getTileWithoutReusingBitmap_fail to wait decode. " + th.getMessage());
                    }
                }
            }
            if (bitmapRegionDecoder == null) {
                TraceController.traceEnd();
                return null;
            }
            Rect overlapRegion = new Rect(0, 0, this.mImageWidth, this.mImageHeight);
            Utils.assertTrue(overlapRegion.intersect(wantRegion));
        }
    }

    public ScreenNail getScreenNail() {
        return this.mScreenNail;
    }

    public int getImageHeight() {
        return this.mImageHeight;
    }

    public int getImageWidth() {
        return this.mImageWidth;
    }

    public int getLevelCount() {
        return this.mLevelCount;
    }

    public MediaItem getCurrentMediaItem() {
        return null;
    }

    public ScreenNailCommonDisplayEnginePool getScreenNailCommonDisplayEnginePool() {
        return null;
    }
}
