package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.RectF;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.displayengine.DisplayEngineFactory;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEngine;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.gallery.displayengine.TileAceDisplayEngine;
import com.huawei.gallery.displayengine.TileScaleDisplayEngine;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.LinkedList;
import java.util.Queue;

public class SupportDisplayEngineTileImageView extends TileImageView {
    private static final Object ACE_SYNC_OBJECT = new Object();
    private static final boolean DISPLAY_OPTIMIZATION_ENABLE = DisplayEngineUtils.isOptimizationEnable();
    private TileAceDisplayEngine mAceDisplayEngine = null;
    private TileAceDisplayEngine mAceDisplayEngine2 = null;
    private int mAceEngineUtilNum = 0;
    private Queue<TileAceDisplayEngine> mAceEngineUtilQueue = new LinkedList();
    private boolean mAceFlag = false;
    private GLView mIgnoreTouchView;
    private boolean mNeedToScale = false;
    private float mOldScale = GroundOverlayOptions.NO_DIMENSION;
    private volatile boolean mPositionChanged;
    private int mRenderCountAfterPositionChanged;
    private TileScaleDisplayEngine mScaleDisplayEngine = null;
    private TileScaleDisplayEngine mScaleDisplayEngine2 = null;
    private int mScaleEngineUtilNum = 0;
    private Queue<TileScaleDisplayEngine> mScaleEngineUtilQueue = new LinkedList();
    private final Object mScaleObject = new Object();
    private final ScaleTileQueue mScaleQueue = new ScaleTileQueue();
    private ScreenNailCommonDisplayEngine mScreenNailCommonDisplayEngine = null;
    private long mStartRenderTime;
    private Future<Void> mTileDecoder2;
    private Future<Void> mTileScaler = null;
    private Future<Void> mTileScaler2 = null;
    TileViewEngineInitThread mTileViewEngineInitThread = null;

    private class DisplayEngineTile extends Tile {
        public ScaledTexture mScaledTexture;

        public DisplayEngineTile(int x, int y, int level) {
            super(x, y, level);
            this.mScaledTexture = new ScaledTexture(this);
        }

        protected void onFreeBitmap(Bitmap bitmap) {
            if (!SupportDisplayEngineTileImageView.this.isImageCanScale()) {
                super.onFreeBitmap(bitmap);
            }
        }

        protected Bitmap onGetBitmap() {
            TraceController.traceBegin("DisplayEngineTile.onGetBitmap");
            if (!isDecodedContentReady()) {
                GalleryLog.w("SupportDisplayEngineTileImageView", "mTileState:" + this.mTileState);
                decode();
            }
            int rightEdge = ((SupportDisplayEngineTileImageView.this.mImageWidth - this.mX) >> this.mTileLevel) + SupportDisplayEngineTileImageView.this.mTileBorderSize;
            int bottomEdge = ((SupportDisplayEngineTileImageView.this.mImageHeight - this.mY) >> this.mTileLevel) + SupportDisplayEngineTileImageView.this.mTileBorderSize;
            Bitmap bitmap = this.mDecodedTile;
            if (rightEdge <= 0 || bottomEdge <= 0) {
                GalleryLog.d("SupportDisplayEngineTileImageView", "mImageWidth = " + SupportDisplayEngineTileImageView.this.mImageWidth + ", mImageHeight = " + SupportDisplayEngineTileImageView.this.mImageHeight);
                setSize(bitmap);
            } else {
                setSize(Math.min(SupportDisplayEngineTileImageView.BITMAP_SIZE, rightEdge), Math.min(SupportDisplayEngineTileImageView.BITMAP_SIZE, bottomEdge));
            }
            if (SupportDisplayEngineTileImageView.this.isImageCanScale()) {
                TraceController.traceEnd();
                return bitmap;
            }
            this.mDecodedTile = null;
            this.mTileState = 1;
            TraceController.traceEnd();
            return bitmap;
        }

        public void update(int x, int y, int level) {
            super.update(x, y, level);
            scaledTextureClear(false);
        }

        public void scaledTextureClear(boolean isFreeBitmap) {
            if (this.mScaledTexture.isLoaded()) {
                this.mScaledTexture.recycle();
                this.mScaledTexture.invalidateContent();
            }
            if (isFreeBitmap && this.mScaledTexture.mScaledTile != null) {
                this.mScaledTexture.mScaledTile.recycle();
                this.mScaledTexture.mScaledTile = null;
            }
        }

        public boolean isDecodedContentReady() {
            return this.mTileState == 8 || this.mTileState >= 128;
        }
    }

    private class DisplayEngineTileDecoder extends BaseJob<Void> {
        private CancelListener mNotifier = new CancelListener() {
            public void onCancel() {
                TraceController.traceBegin("DisplayEngineTileDecoder.onCancel");
                synchronized (SupportDisplayEngineTileImageView.this) {
                    DisplayEngineTileDecoder.this.mWaitFlag = false;
                    SupportDisplayEngineTileImageView.this.notifyAll();
                }
                TraceController.traceEnd();
            }
        };
        private volatile boolean mWaitFlag = false;

        public DisplayEngineTileDecoder(boolean tWaitFlag) {
            this.mWaitFlag = tWaitFlag;
        }

        public Void run(JobContext jc) {
            TraceController.traceBegin("DisplayEngineTileDecoder.run mWaitFlag=" + this.mWaitFlag);
            jc.setMode(0);
            jc.setCancelListener(this.mNotifier);
            while (!jc.isCancelled()) {
                synchronized (SupportDisplayEngineTileImageView.this) {
                    if (this.mWaitFlag) {
                        while (!SupportDisplayEngineTileImageView.this.allowAcceleration()) {
                            try {
                                TraceController.traceBegin("DisplayEngineTileDecoder wait");
                                Utils.waitWithoutInterrupt(SupportDisplayEngineTileImageView.this);
                                TraceController.traceEnd();
                                if (this.mWaitFlag) {
                                }
                            } catch (Throwable th) {
                                GalleryLog.w("SupportDisplayEngineTileImageView", "fail to wait decode. " + th.getMessage());
                            }
                        }
                    }
                    Tile tile = SupportDisplayEngineTileImageView.this.mDecodeQueue.pop();
                    if (tile == null && !jc.isCancelled()) {
                        Utils.waitWithoutInterrupt(SupportDisplayEngineTileImageView.this);
                    }
                }
                if (tile != null && SupportDisplayEngineTileImageView.this.decodeTile(tile)) {
                    SupportDisplayEngineTileImageView.this.queueForUpload(tile);
                }
            }
            TraceController.traceEnd();
            return null;
        }

        public boolean isHeavyJob() {
            return true;
        }

        public String workContent() {
            return "decode tiles";
        }
    }

    private static class ScaleTileQueue {
        private ScaledTexture mHead;

        private ScaleTileQueue() {
        }

        public ScaledTexture pop() {
            ScaledTexture scaledTile = this.mHead;
            if (scaledTile != null) {
                this.mHead = scaledTile.mNext;
                scaledTile.mNext = null;
            }
            return scaledTile;
        }

        public boolean push(ScaledTexture scaledTile) {
            boolean wasEmpty = this.mHead == null;
            scaledTile.mNext = this.mHead;
            this.mHead = scaledTile;
            return wasEmpty;
        }

        public void clean() {
            this.mHead = null;
        }
    }

    private class ScaledTexture extends UploadedTexture {
        public ScaledTexture mNext;
        public Bitmap mScaledTile;
        private Tile mTile;
        private int mTileTextureHeight;
        private int mTileTextureWidth;

        public ScaledTexture(Tile tile) {
            this.mTile = tile;
        }

        protected void onFreeBitmap(Bitmap bitmap) {
            TraceController.traceBegin("ScaledTexture.onFreeBitmap");
            if (bitmap != null) {
                bitmap.recycle();
            }
            TraceController.traceEnd();
        }

        protected Bitmap onGetBitmap() {
            TraceController.traceBegin("ScaledTexture.onGetBitmap");
            if (this.mTile.mTileState != 512) {
                GalleryLog.w("SupportDisplayEngineTileImageView", "mTileState:" + this.mTile.mTileState);
                displayEngine();
            }
            int rightEdge = Math.round(((float) ((SupportDisplayEngineTileImageView.this.mImageWidth - this.mTile.mX) + (SupportDisplayEngineTileImageView.this.mTileBorderSize << this.mTile.mTileLevel))) * SupportDisplayEngineTileImageView.this.mScale);
            int bottomEdge = Math.round(((float) ((SupportDisplayEngineTileImageView.this.mImageHeight - this.mTile.mY) + (SupportDisplayEngineTileImageView.this.mTileBorderSize << this.mTile.mTileLevel))) * SupportDisplayEngineTileImageView.this.mScale);
            int dstBitmapHeight = (int) Math.floor((double) (((float) (SupportDisplayEngineTileImageView.BITMAP_SIZE * (1 << SupportDisplayEngineTileImageView.this.mLevel))) * SupportDisplayEngineTileImageView.this.mScale));
            int scaleTileWidth = this.mScaledTile != null ? this.mScaledTile.getWidth() : (int) Math.floor((double) (((float) (SupportDisplayEngineTileImageView.BITMAP_SIZE * (1 << SupportDisplayEngineTileImageView.this.mLevel))) * SupportDisplayEngineTileImageView.this.mScale));
            int scaleTileHeight = this.mScaledTile != null ? this.mScaledTile.getHeight() : dstBitmapHeight;
            Bitmap bitmap = this.mScaledTile;
            if (bitmap == null || (rightEdge > 0 && bottomEdge > 0)) {
                setSize(Math.min(scaleTileWidth, rightEdge), Math.min(scaleTileHeight, bottomEdge));
            } else {
                GalleryLog.d("SupportDisplayEngineTileImageView", "ScaledTexture, mImageWidth = " + SupportDisplayEngineTileImageView.this.mImageWidth + ", mImageHeight = " + SupportDisplayEngineTileImageView.this.mImageHeight);
                setSize(bitmap);
            }
            this.mScaledTile = null;
            this.mTile.mTileState = 8;
            TraceController.traceEnd();
            return bitmap;
        }

        boolean displayEngine() {
            TraceController.traceBegin("ScaledTexture.displayEngine");
            boolean z = false;
            float scaleratio = ((float) (1 << this.mTile.mTileLevel)) * SupportDisplayEngineTileImageView.this.mScale;
            float xstart = (((float) this.mTile.mX) * SupportDisplayEngineTileImageView.this.mScale) - (((float) SupportDisplayEngineTileImageView.this.mTileBorderSize) * scaleratio);
            float ystart = (((float) this.mTile.mY) * SupportDisplayEngineTileImageView.this.mScale) - (((float) SupportDisplayEngineTileImageView.this.mTileBorderSize) * scaleratio);
            int dstBitmapWidth = (int) Math.floor((double) (((float) (SupportDisplayEngineTileImageView.BITMAP_SIZE * (1 << this.mTile.mTileLevel))) * SupportDisplayEngineTileImageView.this.mScale));
            int dstBitmapHeight = (int) Math.floor((double) (((float) (SupportDisplayEngineTileImageView.BITMAP_SIZE * (1 << this.mTile.mTileLevel))) * SupportDisplayEngineTileImageView.this.mScale));
            Bitmap decodeBitmap = this.mTile.mDecodedTile;
            if (decodeBitmap != null) {
                TileScaleDisplayEngine tileScaleDisplayEngine = null;
                synchronized (SupportDisplayEngineTileImageView.this.mScaleObject) {
                    if (SupportDisplayEngineTileImageView.this.mScaleEngineUtilNum > 0) {
                        tileScaleDisplayEngine = (TileScaleDisplayEngine) SupportDisplayEngineTileImageView.this.mScaleEngineUtilQueue.poll();
                        while (tileScaleDisplayEngine == null) {
                            try {
                                Utils.waitWithoutInterrupt(SupportDisplayEngineTileImageView.this.mScaleObject);
                                tileScaleDisplayEngine = (TileScaleDisplayEngine) SupportDisplayEngineTileImageView.this.mScaleEngineUtilQueue.poll();
                                if (tileScaleDisplayEngine == null) {
                                    GalleryLog.i("SupportDisplayEngineTileImageView", "while(tileScaleEngine==null) after wait");
                                }
                            } catch (Throwable th) {
                                GalleryLog.w("SupportDisplayEngineTileImageView", "fail to wait tileScaleEngine. " + th.getMessage());
                            }
                        }
                    }
                }
                if (this.mScaledTile != null) {
                    this.mScaledTile.recycle();
                    this.mScaledTile = null;
                }
                this.mScaledTile = Bitmap.createBitmap(dstBitmapWidth, dstBitmapHeight, Config.ARGB_8888);
                if (tileScaleDisplayEngine != null) {
                    synchronized (tileScaleDisplayEngine) {
                        ScreenNailCommonDisplayEngine screenNailCommonDisplayEngine = SupportDisplayEngineTileImageView.this.mScreenNailCommonDisplayEngine;
                        if (screenNailCommonDisplayEngine != null) {
                            TraceController.traceBegin("srProcess");
                            z = tileScaleDisplayEngine.process(decodeBitmap, this.mScaledTile, scaleratio, xstart, ystart, SupportDisplayEngineTileImageView.this.mImageWidth, SupportDisplayEngineTileImageView.this.mImageHeight, SupportDisplayEngineTileImageView.this.mLevel, screenNailCommonDisplayEngine);
                            TraceController.traceEnd();
                        }
                    }
                }
                synchronized (SupportDisplayEngineTileImageView.this.mScaleObject) {
                    if (isTileScaleEngineValid(tileScaleDisplayEngine) && !SupportDisplayEngineTileImageView.this.mScaleEngineUtilQueue.offer(tileScaleDisplayEngine)) {
                        GalleryLog.e("SupportDisplayEngineTileImageView", "mScaleEngineUtilQueue.offer(tileScaleEngine) error");
                    }
                    if (SupportDisplayEngineTileImageView.this.allowAcceleration()) {
                        SupportDisplayEngineTileImageView.this.mScaleObject.notifyAll();
                    }
                }
            }
            this.mTileTextureWidth = Utils.nextPowerOf2(dstBitmapWidth);
            this.mTileTextureHeight = Utils.nextPowerOf2(dstBitmapHeight);
            if (!(z || this.mScaledTile == null)) {
                this.mScaledTile.recycle();
                this.mScaledTile = null;
            }
            TraceController.traceEnd();
            return z;
        }

        private boolean isTileScaleEngineValid(TileScaleDisplayEngine tileScaleEngine) {
            if (tileScaleEngine != null) {
                return tileScaleEngine == SupportDisplayEngineTileImageView.this.mScaleDisplayEngine || tileScaleEngine == SupportDisplayEngineTileImageView.this.mScaleDisplayEngine2;
            } else {
                return false;
            }
        }

        public int getTextureWidth() {
            return this.mTileTextureWidth;
        }

        public int getTextureHeight() {
            return this.mTileTextureHeight;
        }
    }

    private class TileScaler extends BaseJob<Void> {
        private CancelListener mNotifier;
        private volatile boolean mWaitFlag;

        public TileScaler(SupportDisplayEngineTileImageView this$0) {
            this(false);
        }

        public TileScaler(boolean tWaitFlag) {
            this.mWaitFlag = false;
            this.mNotifier = new CancelListener() {
                public void onCancel() {
                    TraceController.traceBegin("TileScaler.onCancel");
                    synchronized (SupportDisplayEngineTileImageView.this.mScaleObject) {
                        TileScaler.this.mWaitFlag = false;
                        SupportDisplayEngineTileImageView.this.mScaleObject.notifyAll();
                    }
                    TraceController.traceEnd();
                }
            };
            this.mWaitFlag = tWaitFlag;
        }

        public Void run(JobContext jc) {
            TraceController.traceBegin("TileScaler.run mWaitFlag=" + this.mWaitFlag);
            jc.setMode(0);
            jc.setCancelListener(this.mNotifier);
            while (!jc.isCancelled()) {
                synchronized (SupportDisplayEngineTileImageView.this.mScaleObject) {
                    if (this.mWaitFlag) {
                        while (!SupportDisplayEngineTileImageView.this.allowAcceleration()) {
                            try {
                                TraceController.traceBegin("TileScaler wait");
                                Utils.waitWithoutInterrupt(SupportDisplayEngineTileImageView.this.mScaleObject);
                                TraceController.traceEnd();
                                if (this.mWaitFlag) {
                                }
                            } catch (Throwable th) {
                                GalleryLog.w("SupportDisplayEngineTileImageView", "fail to wait decode. " + th.getMessage());
                            }
                        }
                    }
                    ScaledTexture scaledTile = SupportDisplayEngineTileImageView.this.mScaleQueue.pop();
                    if (scaledTile == null && !jc.isCancelled()) {
                        Utils.waitWithoutInterrupt(SupportDisplayEngineTileImageView.this.mScaleObject);
                    }
                }
                if (scaledTile != null && scaledTile.mTile.mTileLevel == SupportDisplayEngineTileImageView.this.mLevel) {
                    SupportDisplayEngineTileImageView.this.displayEngineTile(scaledTile.mTile);
                }
            }
            TraceController.traceEnd();
            return null;
        }

        public boolean isHeavyJob() {
            return true;
        }

        public String workContent() {
            return "scale tiles";
        }
    }

    private class TileViewEngineInitThread extends Thread {
        private TileViewEngineInitThread() {
        }

        public void run() {
            SupportDisplayEngineTileImageView.this.tileViewEngineInit();
        }
    }

    public SupportDisplayEngineTileImageView(GalleryContext context) {
        super(context);
        TraceController.traceBegin("SupportDisplayEngineTileImageView.SupportDisplayEngineTileImageView");
        if (DISPLAY_OPTIMIZATION_ENABLE) {
            this.mTileDecoder2 = this.mThreadPool.submit(new DisplayEngineTileDecoder(true));
        }
        tileViewEngineInitAsync();
        TraceController.traceEnd();
    }

    protected int getTileBorder() {
        return 8;
    }

    public void setScreenNail(ScreenNail s) {
        if (s != null) {
            TraceController.traceBegin("SupportDisplayEngineTileImageView.setScreenNail w=" + s.getWidth() + ",h=" + s.getHeight());
        } else {
            TraceController.traceBegin("SupportDisplayEngineTileImageView.setScreenNail s is null");
        }
        setScreenNail(s, true);
        TraceController.traceEnd();
    }

    public void setScreenNail(ScreenNail s, boolean needToACE) {
        TraceController.traceBegin("SupportDisplayEngineTileImageView.setScreenNail ScreenNail=" + s);
        clearAnimationProxyView(s);
        this.mScreenNail = s;
        this.mAceFlag = true;
        updateScreenNailCommonDisplayEngine();
        if (s != null) {
            waitTileViewEngineInitEnd();
        }
        TraceController.traceEnd();
    }

    public void notifyModelInvalidated() {
        TraceController.traceBegin("SupportDisplayEngineTileImageView.notifyModelInvalidated");
        this.mOldScale = GroundOverlayOptions.NO_DIMENSION;
        synchronized (this.mScaleObject) {
            this.mScaleQueue.clean();
        }
        super.notifyModelInvalidated();
        if (this.mModel != null) {
            this.mNeedToScale = true;
            updateScreenNailCommonDisplayEngine();
        }
        this.mStartRenderTime = System.currentTimeMillis();
        TraceController.traceEnd();
    }

    private void updateScreenNailCommonDisplayEngine() {
        if (this.mModel != null) {
            MediaItem item = this.mModel.getCurrentMediaItem();
            if (item != null) {
                ScreenNailCommonDisplayEnginePool screenNailCommonDisplayEnginePool = this.mModel.getScreenNailCommonDisplayEnginePool();
                if (screenNailCommonDisplayEnginePool != null) {
                    this.mScreenNailCommonDisplayEngine = screenNailCommonDisplayEnginePool.get(item);
                }
            }
        }
    }

    protected void layoutTiles(float centerX, float centerY, float scale, int rotation) {
        if (rotation % 90 == 0) {
            synchronized (this.mScaleObject) {
                this.mScaleQueue.clean();
            }
        }
        super.layoutTiles(centerX, centerY, scale, rotation);
    }

    public boolean setPosition(float centerX, float centerY, float scale, int rotation) {
        boolean result = super.setPosition(centerX, centerY, scale, rotation);
        if (result) {
            this.mPositionChanged = true;
            this.mRenderCountAfterPositionChanged = 0;
            invalidateScaleTiles();
        }
        return result;
    }

    public void freeTextures() {
        GalleryLog.d("SupportDisplayEngineTileImageView", "enter freeTextures()");
        TraceController.traceBegin("SupportDisplayEngineTileImageView.freeTextures");
        waitTileViewEngineInitEnd();
        if (this.mTileDecoder2 != null) {
            this.mTileDecoder2.cancel();
            this.mTileDecoder2.get();
            this.mTileDecoder2 = null;
        }
        if (this.mTileScaler != null) {
            this.mTileScaler.cancel();
            this.mTileScaler.get();
            this.mNeedToScale = false;
            this.mTileScaler = null;
        }
        if (this.mTileScaler2 != null) {
            this.mTileScaler2.cancel();
            this.mTileScaler2.get();
            this.mNeedToScale = false;
            this.mTileScaler2 = null;
        }
        super.freeTextures();
        if (this.mScaleDisplayEngine != null) {
            this.mScaleDisplayEngine.destroy();
            this.mScaleDisplayEngine = null;
        }
        if (this.mScaleDisplayEngine2 != null) {
            this.mScaleDisplayEngine2.destroy();
            this.mScaleDisplayEngine2 = null;
        }
        this.mScaleEngineUtilQueue.clear();
        this.mScaleEngineUtilNum = 0;
        if (this.mAceDisplayEngine != null) {
            this.mAceDisplayEngine.destroy();
            this.mAceDisplayEngine = null;
        }
        if (this.mAceDisplayEngine2 != null) {
            this.mAceDisplayEngine2.destroy();
            this.mAceDisplayEngine2 = null;
        }
        this.mAceEngineUtilQueue.clear();
        this.mAceEngineUtilNum = 0;
        if (this.mScreenNailCommonDisplayEngine != null) {
            this.mScreenNailCommonDisplayEngine = null;
        }
        TraceController.traceEnd();
        GalleryLog.d("SupportDisplayEngineTileImageView", "exit freeTextures()");
    }

    protected void extraQueueClean() {
        synchronized (this.mScaleObject) {
            this.mScaleQueue.clean();
        }
    }

    protected void extraFreeTexture(Tile tile) {
        if (tile instanceof DisplayEngineTile) {
            ((DisplayEngineTile) tile).scaledTextureClear(true);
        }
    }

    public void prepareTextures() {
        GalleryLog.d("SupportDisplayEngineTileImageView", "enter prepareTextures()");
        TraceController.traceBegin("SupportDisplayEngineTileImageView.prepareTextures");
        if (DISPLAY_OPTIMIZATION_ENABLE && this.mTileDecoder2 == null) {
            this.mTileDecoder2 = this.mThreadPool.submit(new DisplayEngineTileDecoder(true));
        }
        tileViewEngineInitAsync();
        super.prepareTextures();
        TraceController.traceEnd();
        GalleryLog.d("SupportDisplayEngineTileImageView", "exit prepareTextures()");
    }

    @SuppressWarnings({"NN_NAKED_NOTIFY"})
    protected void checkStatusBeforeRender(boolean isMagnifier) {
        if (DISPLAY_OPTIMIZATION_ENABLE) {
            TraceController.traceBegin("SupportDisplayEngineTileImageView.checkStatusBeforeRender mPositionChanged=" + this.mPositionChanged + ",animating=" + isDoingStateTransitionAnimation() + ",touching=" + isTouching());
            this.mRenderCountAfterPositionChanged++;
            if (this.mRenderCountAfterPositionChanged > 1) {
                this.mPositionChanged = false;
            }
            if (isMagnifier || allowAcceleration()) {
                synchronized (this) {
                    notifyAll();
                }
                synchronized (this.mScaleObject) {
                    this.mScaleObject.notifyAll();
                }
            }
            TraceController.traceEnd();
        }
    }

    protected void drawTiles(GLCanvas canvas, Rect tileRange, int level, int offsetX, int offsetY, int size, float length, boolean isMagnifier) {
        int ty;
        int tx;
        boolean decodeRenderComplete = true;
        boolean allowUpload = true;
        boolean exceedBatchUploadTime = false;
        Tile tile;
        if (DISPLAY_OPTIMIZATION_ENABLE) {
            this.mUploadQuota = 1000;
            if (isMagnifier) {
                allowUpload = true;
            } else {
                if (!allowAcceleration()) {
                    allowUpload = false;
                }
                if (allowUpload) {
                    ty = tileRange.top;
                    while (ty < tileRange.bottom) {
                        tx = tileRange.left;
                        while (tx < tileRange.right) {
                            tile = getTile(tx, ty, level);
                            if ((tile instanceof DisplayEngineTile) && !((DisplayEngineTile) tile).isDecodedContentReady() && !tile.isContentValid()) {
                                allowUpload = false;
                                break;
                            }
                            tx += size;
                        }
                        if (!allowUpload) {
                            break;
                        }
                        ty += size;
                    }
                }
                exceedBatchUploadTime = System.currentTimeMillis() - this.mStartRenderTime > 1000;
                if (!allowUpload && exceedBatchUploadTime) {
                    allowUpload = true;
                    this.mUploadQuota = 1;
                }
            }
        } else {
            ty = tileRange.top;
            while (ty < tileRange.bottom) {
                tx = tileRange.left;
                while (tx < tileRange.right) {
                    tile = getTile(tx, ty, level);
                    if (tile != null && !tile.isContentValid() && tile.mTileState != 16) {
                        decodeRenderComplete = false;
                        break;
                    }
                    tx += size;
                }
                ty += size;
            }
        }
        ty = tileRange.top;
        int i = 0;
        while (ty < tileRange.bottom) {
            float y = ((float) offsetY) + (((float) i) * length);
            tx = tileRange.left;
            int j = 0;
            while (tx < tileRange.right) {
                drawTileForScale(canvas, tx, ty, level, ((float) offsetX) + (((float) j) * length), y, length, decodeRenderComplete, allowUpload, exceedBatchUploadTime, isMagnifier);
                tx += size;
                j++;
            }
            ty += size;
            i++;
        }
    }

    protected void aceTile(Tile tile) {
        if (tile.mDecodedTile != null && this.mAceFlag) {
            TileAceDisplayEngine tileAceDisplayEngine = null;
            synchronized (ACE_SYNC_OBJECT) {
                if (this.mAceEngineUtilNum > 0) {
                    tileAceDisplayEngine = (TileAceDisplayEngine) this.mAceEngineUtilQueue.poll();
                    while (tileAceDisplayEngine == null) {
                        try {
                            Utils.waitWithoutInterrupt(ACE_SYNC_OBJECT);
                            tileAceDisplayEngine = (TileAceDisplayEngine) this.mAceEngineUtilQueue.poll();
                            if (tileAceDisplayEngine == null) {
                                GalleryLog.i("SupportDisplayEngineTileImageView", "while tileAceEngine is null after wait");
                            }
                        } catch (Throwable th) {
                            GalleryLog.w("SupportDisplayEngineTileImageView", "fail to wait tileAceEngine. " + th.getMessage());
                        }
                    }
                }
            }
            if (tileAceDisplayEngine != null) {
                synchronized (tileAceDisplayEngine) {
                    ScreenNailCommonDisplayEngine screenNailCommonDisplayEngine = this.mScreenNailCommonDisplayEngine;
                    if (screenNailCommonDisplayEngine != null) {
                        TraceController.traceBegin("aceTileProcess");
                        tileAceDisplayEngine.process(tile.mDecodedTile, tile.mDecodedTile, this.mImageWidth >> tile.mTileLevel, this.mImageHeight >> tile.mTileLevel, tile.mX >> tile.mTileLevel, tile.mY >> tile.mTileLevel, this.mTileBorderSize, screenNailCommonDisplayEngine);
                        TraceController.traceEnd();
                    }
                }
            }
            synchronized (ACE_SYNC_OBJECT) {
                if (tileAceDisplayEngine != null) {
                    if ((tileAceDisplayEngine == this.mAceDisplayEngine || tileAceDisplayEngine == this.mAceDisplayEngine2) && !this.mAceEngineUtilQueue.offer(tileAceDisplayEngine)) {
                        GalleryLog.e("SupportDisplayEngineTileImageView", "mAceEngineUtilQueue.offer(tileAceEngine) error");
                    }
                }
                ACE_SYNC_OBJECT.notifyAll();
            }
        }
    }

    protected Tile newInstanceTile(int x, int y, int level) {
        return new DisplayEngineTile(x, y, level);
    }

    protected synchronized void recycleTile(Tile tile) {
        if (!(tile instanceof DisplayEngineTile)) {
            return;
        }
        if (tile.mTileState == 4) {
            tile.mTileState = 32;
        } else if (tile.mTileState == 256) {
            tile.mTileState = 32;
        } else {
            tile.mTileState = 64;
            if (tile.mDecodedTile != null) {
                if (sTilePool != null) {
                    sTilePool.recycle(tile.mDecodedTile);
                }
                tile.mDecodedTile = null;
            }
            ((DisplayEngineTile) tile).scaledTextureClear(true);
            this.mRecycledQueue.push(tile);
        }
    }

    protected void activateExtraTileState(Tile tile) {
        if (tile.mTileState == 128) {
            tile.mTileState = 8;
        }
    }

    protected boolean tileCanNotUpload(Tile tile) {
        if (!(tile instanceof DisplayEngineTile) || ((DisplayEngineTile) tile).isDecodedContentReady()) {
            return false;
        }
        return true;
    }

    private void tileViewEngineInitAsync() {
        if (this.mTileViewEngineInitThread == null) {
            this.mTileViewEngineInitThread = new TileViewEngineInitThread();
            this.mTileViewEngineInitThread.start();
        }
    }

    private void waitTileViewEngineInitEnd() {
        if (this.mTileViewEngineInitThread != null) {
            try {
                this.mTileViewEngineInitThread.join();
            } catch (InterruptedException e) {
            }
            this.mTileViewEngineInitThread = null;
        }
    }

    private void tileViewEngineInit() {
        GalleryLog.d("SupportDisplayEngineTileImageView", "enter tileViewEngineInit()");
        TraceController.traceBegin("SupportDisplayEngineTileImageView.tileViewEngineInit");
        if (this.mScaleDisplayEngine == null) {
            this.mScaleDisplayEngine = (TileScaleDisplayEngine) DisplayEngineFactory.buildDisplayEngine(BITMAP_SIZE, BITMAP_SIZE, this.mTileBorderSize, 5);
            if (this.mScaleDisplayEngine == null) {
                GalleryLog.w("SupportDisplayEngineTileImageView", "fail to getScaleEngineInstance mScaleDisplayEngine");
            } else {
                if (!this.mScaleEngineUtilQueue.offer(this.mScaleDisplayEngine)) {
                    GalleryLog.e("SupportDisplayEngineTileImageView", "mScaleEngineUtilQueue.offer(mScaleDisplayEngine) error");
                }
                this.mScaleEngineUtilNum = this.mScaleEngineUtilQueue.size();
            }
        }
        if (DISPLAY_OPTIMIZATION_ENABLE) {
            if (this.mScaleDisplayEngine2 == null) {
                this.mScaleDisplayEngine2 = (TileScaleDisplayEngine) DisplayEngineFactory.buildDisplayEngine(BITMAP_SIZE, BITMAP_SIZE, this.mTileBorderSize, 5);
                if (this.mScaleDisplayEngine2 == null) {
                    GalleryLog.w("SupportDisplayEngineTileImageView", "fail to getScaleEngineInstance mScaleDisplayEngine2");
                } else {
                    if (!this.mScaleEngineUtilQueue.offer(this.mScaleDisplayEngine2)) {
                        GalleryLog.e("SupportDisplayEngineTileImageView", "mScaleEngineUtilQueue.offer(mScaleDisplayEngine2) error");
                    }
                    this.mScaleEngineUtilNum = this.mScaleEngineUtilQueue.size();
                }
            }
            if (!(this.mScaleDisplayEngine == null || this.mScaleDisplayEngine2 == null)) {
                if (this.mTileScaler == null) {
                    this.mTileScaler = this.mThreadPool.submit(new TileScaler(true));
                }
                if (this.mTileScaler2 == null) {
                    this.mTileScaler2 = this.mThreadPool.submit(new TileScaler(true));
                }
            }
        } else if (this.mScaleDisplayEngine != null && this.mTileScaler == null) {
            this.mTileScaler = this.mThreadPool.submit(new TileScaler(this));
        }
        if (this.mAceDisplayEngine == null) {
            this.mAceDisplayEngine = (TileAceDisplayEngine) DisplayEngineFactory.buildDisplayEngine(BITMAP_SIZE, BITMAP_SIZE, this.mTileBorderSize, 4);
            if (this.mAceDisplayEngine == null) {
                GalleryLog.w("SupportDisplayEngineTileImageView", "fail to getAceEngineInstance mAceDisplayEngine");
            } else {
                if (!this.mAceEngineUtilQueue.offer(this.mAceDisplayEngine)) {
                    GalleryLog.e("SupportDisplayEngineTileImageView", "mAceEngineUtilQueue.offer(mAceDisplayEngine)");
                }
                this.mAceEngineUtilNum = this.mAceEngineUtilQueue.size();
            }
        }
        if (DISPLAY_OPTIMIZATION_ENABLE && this.mAceDisplayEngine2 == null) {
            this.mAceDisplayEngine2 = (TileAceDisplayEngine) DisplayEngineFactory.buildDisplayEngine(BITMAP_SIZE, BITMAP_SIZE, this.mTileBorderSize, 4);
            if (this.mAceDisplayEngine2 == null) {
                GalleryLog.w("SupportDisplayEngineTileImageView", " fail to getAceEngineInstance mAceDisplayEngine2");
            } else {
                if (!this.mAceEngineUtilQueue.offer(this.mAceDisplayEngine2)) {
                    GalleryLog.e("SupportDisplayEngineTileImageView", "mAceEngineUtilQueue.offer(mAceDisplayEngine2)");
                }
                this.mAceEngineUtilNum = this.mAceEngineUtilQueue.size();
            }
        }
        TraceController.traceEnd();
        GalleryLog.d("SupportDisplayEngineTileImageView", "exit tileViewEngineInit()");
    }

    private boolean isToScaleReady(boolean isDecodeRenderComplete) {
        return isDecodeRenderComplete ? isImageCanScale() : false;
    }

    private boolean isImageCanScale() {
        return this.mNeedToScale && this.mScaleDisplayEngine != null;
    }

    private void invalidateScaleTiles() {
        TraceController.traceBegin("SupportDisplayEngineTileImageView.invalidateScaleTiles");
        if (!(((int) Math.floor((double) (this.mOldScale * 1000000.0f))) == ((int) Math.floor((double) (this.mScale * 1000000.0f))) || !this.mNeedToScale || this.mScaleDisplayEngine == null)) {
            int i;
            Tile tile;
            int n = this.mActiveTiles.size();
            for (i = 0; i < n; i++) {
                tile = (Tile) this.mActiveTiles.valueAt(i);
                if (tile instanceof DisplayEngineTile) {
                    ((DisplayEngineTile) tile).scaledTextureClear(false);
                }
            }
            synchronized (this) {
                for (i = 0; i < n; i++) {
                    tile = (Tile) this.mActiveTiles.valueAt(i);
                    if (tile != null) {
                        if (tile.mTileState == 512 || tile.mTileState == 1024) {
                            tile.mTileState = 8;
                        }
                        if (tile.mTileState == 64) {
                            tile = obtainTile(tile.mX, tile.mY, tile.mTileLevel);
                        }
                        if (tile.mTileState == 256) {
                            Tile tileTmp = obtainTile(tile.mX, tile.mY, tile.mTileLevel);
                            tileTmp.mDecodedTile = tile.mDecodedTile;
                            tileTmp.mTileState = 8;
                            tile.mTileState = 2048;
                            this.mActiveTiles.put(TileImageView.makeTileKey(tile.mX, tile.mY, tile.mTileLevel), tileTmp);
                        }
                    }
                }
            }
        }
        this.mOldScale = this.mScale;
        TraceController.traceEnd();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void queueForScale(Tile tile) {
        if (tile instanceof DisplayEngineTile) {
            synchronized (this) {
                if (tile.mTileState == 8) {
                    tile.mTileState = 128;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean displayEngineTile(Tile tile) {
        if (!(tile instanceof DisplayEngineTile)) {
            return false;
        }
        DisplayEngineTile displayEngineTile = (DisplayEngineTile) tile;
        TraceController.traceBegin("SupportDisplayEngineTileImageView.displayEngineTile");
        synchronized (this) {
            if (tile.mTileState != 128) {
                TraceController.traceEnd();
                return false;
            }
            tile.mTileState = 256;
        }
    }

    private void drawTileForScale(GLCanvas canvas, int tx, int ty, int level, float x, float y, float length, boolean decodeRenderComplete, boolean allowUpload, boolean exceedBatchUploadTime, boolean isMagnifier) {
        TraceController.traceBegin("SupportDisplayEngineTileImageView.drawTileForScale");
        boolean z = true;
        RectF source = this.mSourceRect;
        RectF target = this.mTargetRect;
        target.set(x, y, x + length, y + length);
        source.set(0.0f, 0.0f, (float) this.mTileSize, (float) this.mTileSize);
        Tile tile = getTile(tx, ty, level);
        if (tile instanceof DisplayEngineTile) {
            DisplayEngineTile displayEngineTile = (DisplayEngineTile) tile;
            if (!tile.isContentValid()) {
                if (displayEngineTile.isDecodedContentReady()) {
                    if (DISPLAY_OPTIMIZATION_ENABLE) {
                        if (this.mUploadQuota > 0) {
                            if (isMagnifier) {
                                this.mUploadQuota--;
                                TraceController.traceBegin("tile.updateContent");
                                tile.updateContent(canvas);
                                TraceController.traceEnd();
                            } else if (allowUpload && (!isTouching() || this.mUploadQuota == 1)) {
                                this.mUploadQuota--;
                                TraceController.traceBegin("tile.updateContent");
                                tile.updateContent(canvas);
                                TraceController.traceEnd();
                                z = exceedBatchUploadTime;
                            }
                        }
                    } else if (this.mUploadQuota > 0) {
                        this.mUploadQuota--;
                        TraceController.traceBegin("tile.updateContent");
                        tile.updateContent(canvas);
                        TraceController.traceEnd();
                    }
                    if (isImageCanScale()) {
                        queueForScale(tile);
                    }
                } else if (tile.mTileState != 16) {
                    queueForDecode(tile);
                }
                if (tile.mTileState != 16) {
                    this.mRenderComplete = false;
                }
            } else if (isToScaleReady(decodeRenderComplete) && !displayEngineTile.mScaledTexture.isContentValid()) {
                if (tile.mTileState == 512) {
                    if (DISPLAY_OPTIMIZATION_ENABLE) {
                        if (this.mUploadQuota <= 0) {
                            this.mRenderComplete = false;
                        } else if (isMagnifier) {
                            this.mUploadQuota--;
                            TraceController.traceBegin("ScaledTexture.updateContent");
                            displayEngineTile.mScaledTexture.updateContent(canvas);
                            TraceController.traceEnd();
                        } else if (allowUpload && !isTouching()) {
                            this.mUploadQuota--;
                            TraceController.traceBegin("ScaledTexture.updateContent");
                            displayEngineTile.mScaledTexture.updateContent(canvas);
                            TraceController.traceEnd();
                        }
                    } else if (this.mUploadQuota > 0) {
                        this.mUploadQuota--;
                        TraceController.traceBegin("ScaledTexture.updateContent");
                        displayEngineTile.mScaledTexture.updateContent(canvas);
                        TraceController.traceEnd();
                    } else {
                        this.mRenderComplete = false;
                    }
                } else if (tile.mTileState == 8) {
                    this.mRenderComplete = false;
                    queueForScale(tile);
                } else if (!(tile.mTileState == 1024 || tile.mTileState == 16)) {
                    this.mRenderComplete = false;
                }
            }
            if (z) {
                if (drawTileForScale(tile, canvas, source, target, this.mScale)) {
                    TraceController.traceEnd();
                    return;
                }
            }
            if (this.mScreenNail != null) {
                int size = this.mTileSize << level;
                float scaleX = ((float) this.mScreenNail.getWidth()) / ((float) this.mImageWidth);
                float scaleY = ((float) this.mScreenNail.getHeight()) / ((float) this.mImageHeight);
                source.set(((float) tx) * scaleX, ((float) ty) * scaleY, ((float) (tx + size)) * scaleX, ((float) (ty + size)) * scaleY);
                this.mScreenNail.draw(canvas, source, target);
            }
            TraceController.traceEnd();
        }
    }

    private boolean drawTileForScale(Tile tile, GLCanvas canvas, RectF source, RectF target, float scale) {
        TraceController.traceBegin("static drawTileForScale");
        if (!(tile instanceof DisplayEngineTile)) {
            return false;
        }
        DisplayEngineTile displayEngineTile = (DisplayEngineTile) tile;
        if (displayEngineTile.mScaledTexture.isContentValid()) {
            float length = ((float) (this.mTileSize << tile.mTileLevel)) * scale;
            source.set(0.0f, 0.0f, length, length);
            float scaleratio = ((float) (1 << tile.mTileLevel)) * scale;
            float x = ((float) tile.mX) * scale;
            float y = ((float) tile.mY) * scale;
            RectF rectF = source;
            rectF.offset(x - ((float) ((int) Math.ceil((double) (x - (((float) this.mTileBorderSize) * scaleratio))))), y - ((float) ((int) Math.ceil((double) (y - (((float) this.mTileBorderSize) * scaleratio))))));
            canvas.drawTexture(displayEngineTile.mScaledTexture, source, target);
            TraceController.traceEnd();
            return true;
        }
        boolean ret = drawTile(tile, canvas, source, target);
        TraceController.traceEnd();
        return ret;
    }

    public boolean isTouching() {
        boolean z = false;
        GLView ignoreTouchView = this.mIgnoreTouchView;
        boolean needIgnore = ignoreTouchView != null ? ignoreTouchView.getVisibility() == 0 : false;
        if (needIgnore) {
            return false;
        }
        GLRoot root = getGLRoot();
        if (root != null) {
            z = root.getInstantTouchingState();
        }
        return z;
    }

    public boolean isDoingStateTransitionAnimation() {
        GLRoot root = getGLRoot();
        if (root != null) {
            return root.isDoingStateTransitionAnimation();
        }
        return false;
    }

    public boolean allowAcceleration() {
        return (this.mPositionChanged || isDoingStateTransitionAnimation() || isTouching()) ? false : true;
    }

    public void setReleativeView(GLView glView) {
        this.mIgnoreTouchView = glView;
    }
}
