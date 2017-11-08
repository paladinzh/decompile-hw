package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.LongSparseArray;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.GLRoot.OnGLIdleListener;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.WMComponent;
import java.util.concurrent.atomic.AtomicBoolean;

public class TileImageView extends GLView {
    static int BITMAP_SIZE;
    static BitmapPool sTilePool;
    private final Rect[] mActiveRange = new Rect[]{new Rect(), new Rect()};
    protected final LongSparseArray<Tile> mActiveTiles = new LongSparseArray();
    private boolean mBackgroundTileUploaded;
    protected float mCenterX;
    protected float mCenterY;
    protected final TileQueue mDecodeQueue = new TileQueue();
    private DirectShowNail mDirectShowNail;
    private ExtraEffect mExtraEffect;
    protected int mImageHeight = -1;
    protected int mImageWidth = -1;
    private boolean mIsTextureFreed;
    protected int mLevel = 0;
    protected int mLevelCount;
    protected Model mModel;
    private int mOffsetX;
    private int mOffsetY;
    protected final TileQueue mRecycledQueue = new TileQueue();
    protected boolean mRenderComplete;
    protected int mRotation;
    protected float mScale;
    protected ScreenNail mScreenNail;
    protected final RectF mSourceRect = new RectF();
    protected final RectF mTargetRect = new RectF();
    protected final ThreadPool mThreadPool;
    protected final int mTileBorderSize;
    private Future<Void> mTileDecoder;
    private final Rect mTileRange = new Rect();
    protected final int mTileSize;
    private final TileUploader mTileUploader = new TileUploader();
    private final TileQueue mUploadQueue = new TileQueue();
    protected int mUploadQuota;

    public interface Model {
        MediaItem getCurrentMediaItem();

        int getImageHeight();

        int getImageWidth();

        int getLevelCount();

        ScreenNail getScreenNail();

        ScreenNailCommonDisplayEnginePool getScreenNailCommonDisplayEnginePool();

        Bitmap getTile(int i, int i2, int i3, int i4, int i5, BitmapPool bitmapPool);
    }

    public interface DirectShowNail {
        void draw(GLCanvas gLCanvas);

        int getRotation();

        void recycle();
    }

    protected class Tile extends UploadedTexture {
        public Bitmap mDecodedTile;
        public Tile mNext;
        public int mTileLevel;
        public volatile int mTileState = 1;
        public int mX;
        public int mY;

        public Tile(int x, int y, int level) {
            this.mX = x;
            this.mY = y;
            this.mTileLevel = level;
        }

        protected void onFreeBitmap(Bitmap bitmap) {
            if (TileImageView.sTilePool != null) {
                TileImageView.sTilePool.recycle(bitmap);
            }
        }

        boolean decode() {
            try {
                this.mDecodedTile = DecodeUtils.ensureGLCompatibleBitmap(TileImageView.this.mModel.getTile(this.mTileLevel, this.mX, this.mY, TileImageView.this.mTileSize, TileImageView.this.mTileBorderSize, TileImageView.sTilePool));
            } catch (Throwable t) {
                GalleryLog.w("TileImageView", "fail to decode tile." + t.getMessage());
            }
            return this.mDecodedTile != null;
        }

        protected Bitmap onGetBitmap() {
            if (this.mTileState != 8) {
                GalleryLog.w("TileImageView", "mTileState:" + this.mTileState);
                decode();
            }
            setSize(Math.min(TileImageView.BITMAP_SIZE, ((TileImageView.this.mImageWidth - this.mX) >> this.mTileLevel) + TileImageView.this.mTileBorderSize), Math.min(TileImageView.BITMAP_SIZE, ((TileImageView.this.mImageHeight - this.mY) >> this.mTileLevel) + TileImageView.this.mTileBorderSize));
            Bitmap bitmap = this.mDecodedTile;
            this.mDecodedTile = null;
            this.mTileState = 1;
            return bitmap;
        }

        public int getTextureWidth() {
            return TileImageView.this.mTileSize + (TileImageView.this.mTileBorderSize * 2);
        }

        public int getTextureHeight() {
            return TileImageView.this.mTileSize + (TileImageView.this.mTileBorderSize * 2);
        }

        public void update(int x, int y, int level) {
            this.mX = x;
            this.mY = y;
            this.mTileLevel = level;
            invalidateContent();
        }

        public Tile getParentTile() {
            if (this.mTileLevel + 1 == TileImageView.this.mLevelCount) {
                return null;
            }
            int size = TileImageView.this.mTileSize << (this.mTileLevel + 1);
            return TileImageView.this.getTile(size * (this.mX / size), size * (this.mY / size), this.mTileLevel + 1);
        }

        public String toString() {
            return String.format("tile(%s, %s, %s / %s)", new Object[]{Integer.valueOf(this.mX / TileImageView.this.mTileSize), Integer.valueOf(this.mY / TileImageView.this.mTileSize), Integer.valueOf(TileImageView.this.mLevel), Integer.valueOf(TileImageView.this.mLevelCount)});
        }
    }

    public interface ExtraEffect {
        void applyExtraEffect(GLCanvas gLCanvas);

        boolean hasExtraEffect();
    }

    private class TileDecoder extends BaseJob<Void> {
        private CancelListener mNotifier;

        private TileDecoder() {
            this.mNotifier = new CancelListener() {
                public void onCancel() {
                    synchronized (TileImageView.this) {
                        TileImageView.this.notifyAll();
                    }
                }
            };
        }

        public Void run(JobContext jc) {
            jc.setMode(0);
            jc.setCancelListener(this.mNotifier);
            while (!jc.isCancelled()) {
                synchronized (TileImageView.this) {
                    Tile tile = TileImageView.this.mDecodeQueue.pop();
                    if (tile == null && !jc.isCancelled()) {
                        Utils.waitWithoutInterrupt(TileImageView.this);
                    }
                }
                if (tile != null && TileImageView.this.decodeTile(tile)) {
                    TileImageView.this.queueForUpload(tile);
                }
            }
            return null;
        }

        public boolean isHeavyJob() {
            return true;
        }

        public String workContent() {
            return "decode tiles";
        }
    }

    protected static class TileQueue {
        private Tile mHead;

        protected TileQueue() {
        }

        public Tile pop() {
            Tile tile = this.mHead;
            if (tile != null) {
                this.mHead = tile.mNext;
                tile.mNext = null;
            }
            return tile;
        }

        public boolean push(Tile tile) {
            boolean wasEmpty = this.mHead == null;
            tile.mNext = this.mHead;
            this.mHead = tile;
            return wasEmpty;
        }

        public void clean() {
            this.mHead = null;
        }
    }

    private class TileUploader implements OnGLIdleListener {
        AtomicBoolean mActive;

        private TileUploader() {
            this.mActive = new AtomicBoolean(false);
        }

        public boolean onGLIdle(GLCanvas canvas, boolean renderRequested) {
            boolean z = true;
            TraceController.traceBegin("TileImageView.onGLIdle");
            if (renderRequested) {
                TraceController.traceEnd();
                return true;
            }
            int quota = 1;
            Tile tile = null;
            while (quota > 0) {
                synchronized (TileImageView.this) {
                    tile = TileImageView.this.mUploadQueue.pop();
                }
                if (tile == null) {
                    break;
                } else if (!tile.isContentValid()) {
                    quota--;
                    if (!TileImageView.this.tileCanNotUpload(tile)) {
                        TraceController.traceBegin("TileImageView.onGLIdle.tile.updateContent");
                        tile.updateContent(canvas);
                        TraceController.traceEnd();
                    }
                }
            }
            if (tile == null) {
                this.mActive.set(false);
            }
            TraceController.traceEnd();
            if (tile == null) {
                z = false;
            }
            return z;
        }
    }

    public TileImageView(GalleryContext context) {
        BitmapPool bitmapPool = null;
        this.mThreadPool = context.getThreadPool();
        this.mTileDecoder = this.mThreadPool.submit(new TileDecoder());
        if (BITMAP_SIZE == 0) {
            if (GalleryUtils.isHighResolution(context.getAndroidContext())) {
                BITMAP_SIZE = 512;
            } else {
                BITMAP_SIZE = 256;
            }
            if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER) {
                bitmapPool = new BitmapPool(BITMAP_SIZE, BITMAP_SIZE, 128);
            }
            sTilePool = bitmapPool;
        }
        this.mTileBorderSize = getTileBorder();
        this.mTileSize = BITMAP_SIZE - (this.mTileBorderSize * 2);
    }

    protected int getTileBorder() {
        return 1;
    }

    public void setModel(Model model) {
        TraceController.traceBegin("TileImageView.setModel");
        this.mModel = model;
        if (model != null) {
            notifyModelInvalidated();
        }
        TraceController.traceEnd();
    }

    protected void clearAnimationProxyView(ScreenNail s) {
        GLRoot root = getGLRoot();
        if (s != null && root != null) {
            root.clearAnimationProxyView(false);
        }
    }

    public void setScreenNail(ScreenNail s) {
        this.mScreenNail = s;
        clearAnimationProxyView(s);
    }

    public void setScreenNail(ScreenNail s, boolean needToACE) {
        setScreenNail(s);
    }

    public void setDirectShowNail(DirectShowNail directShowNail) {
        TraceController.traceBegin("TileImageView.setDirectShowNail");
        this.mDirectShowNail = directShowNail;
        if ((this.mScreenNail instanceof TiledScreenNail) && this.mScreenNail.getBitmap() == null) {
            this.mScreenNail = null;
        }
        invalidate();
        TraceController.traceEnd();
    }

    public boolean hasDirectShowNail() {
        return this.mDirectShowNail != null;
    }

    public void notifyModelInvalidated() {
        invalidateTiles();
        if (this.mModel == null) {
            this.mScreenNail = null;
            this.mImageWidth = 0;
            this.mImageHeight = 0;
            this.mLevelCount = 0;
        } else {
            setScreenNail(this.mModel.getScreenNail());
            this.mImageWidth = this.mModel.getImageWidth();
            this.mImageHeight = this.mModel.getImageHeight();
            this.mLevelCount = this.mModel.getLevelCount();
        }
        layoutTiles(this.mCenterX, this.mCenterY, this.mScale, this.mRotation);
        invalidate();
        if (this.mScreenNail != null) {
            if (this.mDirectShowNail != null) {
                GalleryLog.i("TileImageView", "DirectShowNail recycle");
                this.mDirectShowNail.recycle();
            }
            this.mDirectShowNail = null;
        }
    }

    public void updateScreenNailSize(int width, int height) {
        this.mImageWidth = width;
        this.mImageHeight = height;
    }

    public int getImageWidth() {
        return this.mImageWidth;
    }

    public int getImageHeight() {
        return this.mImageHeight;
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        TraceController.traceBegin("TileImageView.onLayout");
        super.onLayout(changeSize, left, top, right, bottom);
        if (changeSize) {
            layoutTiles(this.mCenterX, this.mCenterY, this.mScale, this.mRotation);
        }
        TraceController.traceEnd();
    }

    public void layoutTiles() {
        layoutTiles(this.mCenterX, this.mCenterY, this.mScale, this.mRotation);
        invalidate();
    }

    protected void layoutTiles(float centerX, float centerY, float scale, int rotation) {
        int fromLevel;
        int i;
        TraceController.traceBegin("TileImageView.layoutTiles");
        int width = getWidth();
        int height = getHeight();
        this.mLevel = Utils.clamp(Utils.floorLog2(WMElement.CAMERASIZEVALUE1B1 / scale), 0, this.mLevelCount);
        if (this.mLevel != this.mLevelCount) {
            Rect range = this.mTileRange;
            getRange(range, centerX, centerY, this.mLevel, scale, rotation);
            this.mOffsetX = Math.round((((float) width) / 2.0f) + ((((float) range.left) - centerX) * scale));
            this.mOffsetY = Math.round((((float) height) / 2.0f) + ((((float) range.top) - centerY) * scale));
            fromLevel = ((float) (1 << this.mLevel)) * scale > 0.75f ? this.mLevel - 1 : this.mLevel;
        } else {
            fromLevel = this.mLevel - 2;
            this.mOffsetX = Math.round((((float) width) / 2.0f) - (centerX * scale));
            this.mOffsetY = Math.round((((float) height) / 2.0f) - (centerY * scale));
        }
        fromLevel = Math.max(0, Math.min(fromLevel, this.mLevelCount - 2));
        int endLevel = Math.min(fromLevel + 2, this.mLevelCount);
        Rect[] range2 = this.mActiveRange;
        for (i = fromLevel; i < endLevel; i++) {
            getRange(range2[i - fromLevel], centerX, centerY, i, rotation);
        }
        if (rotation % 90 != 0) {
            TraceController.traceEnd();
            return;
        }
        synchronized (this) {
            this.mDecodeQueue.clean();
            this.mUploadQueue.clean();
            this.mBackgroundTileUploaded = false;
            int n = this.mActiveTiles.size();
            i = 0;
            while (i < n) {
                Tile tile = (Tile) this.mActiveTiles.valueAt(i);
                int level = tile.mTileLevel;
                if (level < fromLevel || level >= endLevel || !range2[level - fromLevel].contains(tile.mX, tile.mY)) {
                    this.mActiveTiles.removeAt(i);
                    i--;
                    n--;
                    recycleTile(tile);
                }
                i++;
            }
        }
        for (i = fromLevel; i < endLevel; i++) {
            TraceController.traceBegin("for fromLevel->endLevel i=" + i);
            int size = this.mTileSize << i;
            Rect r = range2[i - fromLevel];
            int bottom = r.bottom;
            for (int y = r.top; y < bottom; y += size) {
                int right = r.right;
                for (int x = r.left; x < right; x += size) {
                    activateTile(x, y, i);
                }
            }
            TraceController.traceEnd();
        }
        invalidate();
        TraceController.traceEnd();
    }

    protected synchronized void invalidateTiles() {
        this.mDecodeQueue.clean();
        this.mUploadQueue.clean();
        int n = this.mActiveTiles.size();
        for (int i = 0; i < n; i++) {
            recycleTile((Tile) this.mActiveTiles.valueAt(i));
        }
        this.mActiveTiles.clear();
    }

    private void getRange(Rect out, float cX, float cY, int level, int rotation) {
        getRange(out, cX, cY, level, WMElement.CAMERASIZEVALUE1B1 / ((float) (1 << (level + 1))), rotation);
    }

    private void getRange(Rect out, float cX, float cY, int level, float scale, int rotation, double imageViewerWidth, double imageViewerHeight) {
        double radians = Math.toRadians((double) (-rotation));
        double w = imageViewerWidth;
        double h = imageViewerHeight;
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int width = (int) Math.ceil(Math.max(Math.abs((cos * imageViewerWidth) - (sin * imageViewerHeight)), Math.abs((cos * imageViewerWidth) + (sin * imageViewerHeight))));
        int height = (int) Math.ceil(Math.max(Math.abs((sin * imageViewerWidth) + (cos * imageViewerHeight)), Math.abs((sin * imageViewerWidth) - (cos * imageViewerHeight))));
        float floatLeft = cX - (((float) width) / (2.0f * scale));
        float floatTop = cY - (((float) height) / (2.0f * scale));
        int top = (int) Math.floor((double) floatTop);
        int right = (int) Math.ceil((double) ((((float) width) / scale) + floatLeft));
        int bottom = (int) Math.ceil((double) ((((float) height) / scale) + floatTop));
        int size = this.mTileSize << level;
        out.set(Math.max(0, (((int) Math.floor((double) floatLeft)) / size) * size), Math.max(0, (top / size) * size), Math.min(this.mImageWidth, right), Math.min(this.mImageHeight, bottom));
    }

    private void getRange(Rect out, float cX, float cY, int level, float scale, int rotation) {
        getRange(out, cX, cY, level, scale, rotation, (double) getWidth(), (double) getHeight());
    }

    public void getImageCenter(Point center) {
        float distW;
        float distH;
        int viewW = getWidth();
        int viewH = getHeight();
        switch (this.mRotation) {
            case WMComponent.ORI_90 /*90*/:
                distW = this.mCenterY - (((float) this.mImageHeight) / 2.0f);
                distH = (((float) this.mImageWidth) / 2.0f) - this.mCenterX;
                break;
            case 180:
                distW = this.mCenterX - (((float) this.mImageWidth) / 2.0f);
                distH = this.mCenterY - (((float) this.mImageHeight) / 2.0f);
                break;
            case 270:
                distW = (((float) this.mImageHeight) / 2.0f) - this.mCenterY;
                distH = this.mCenterX - (((float) this.mImageWidth) / 2.0f);
                break;
            default:
                distW = (((float) this.mImageWidth) / 2.0f) - this.mCenterX;
                distH = (((float) this.mImageHeight) / 2.0f) - this.mCenterY;
                break;
        }
        center.x = Math.round((((float) viewW) / 2.0f) + (this.mScale * distW));
        center.y = Math.round((((float) viewH) / 2.0f) + (this.mScale * distH));
    }

    public boolean setPosition(float centerX, float centerY, float scale, int rotation) {
        if (Utils.equal(this.mCenterX, centerX) && Utils.equal(this.mCenterY, centerY) && Utils.equal(this.mScale, scale) && this.mRotation == rotation) {
            return false;
        }
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mScale = scale;
        this.mRotation = rotation;
        layoutTiles(centerX, centerY, scale, rotation);
        invalidate();
        return true;
    }

    public void freeTextures() {
        TraceController.traceBegin("TileImageView.freeTextures");
        this.mIsTextureFreed = true;
        if (this.mTileDecoder != null) {
            this.mTileDecoder.cancel();
            this.mTileDecoder.get();
            this.mTileDecoder = null;
        }
        int n = this.mActiveTiles.size();
        for (int i = 0; i < n; i++) {
            Tile texture = (Tile) this.mActiveTiles.valueAt(i);
            texture.recycle();
            extraFreeTexture(texture);
        }
        this.mActiveTiles.clear();
        this.mTileRange.set(0, 0, 0, 0);
        extraQueueClean();
        synchronized (this) {
            this.mUploadQueue.clean();
            this.mDecodeQueue.clean();
            Tile tile = this.mRecycledQueue.pop();
            while (tile != null) {
                tile.recycle();
                tile = this.mRecycledQueue.pop();
            }
        }
        setScreenNail(null);
        if (sTilePool != null) {
            sTilePool.clear();
        }
        TraceController.traceEnd();
    }

    protected void extraQueueClean() {
    }

    protected void extraFreeTexture(Tile tile) {
    }

    public void prepareTextures() {
        ScreenNail screenNail = null;
        if (this.mTileDecoder == null) {
            this.mTileDecoder = this.mThreadPool.submit(new TileDecoder());
        }
        if (this.mIsTextureFreed) {
            layoutTiles(this.mCenterX, this.mCenterY, this.mScale, this.mRotation);
            this.mIsTextureFreed = false;
            if (this.mModel != null) {
                screenNail = this.mModel.getScreenNail();
            }
            setScreenNail(screenNail);
        }
    }

    public boolean isScreenNailFromCache() {
        if (this.mScreenNail instanceof TiledScreenNail) {
            return this.mScreenNail.isBitmapFromCache();
        }
        return false;
    }

    public void layoutMagnifierTiles(RectF photoRange, float scale, Rect tileRange, Rect[] activeRange, Point offset) {
        int fromLevel;
        int tileImageFromLevel;
        int i;
        int rotation = this.mRotation;
        float centerX = photoRange.centerX();
        float centerY = photoRange.centerY();
        int width = PhotoMagnifierView.MAGNIFIER_WIDTH;
        int height = PhotoMagnifierView.MAGNIFIER_HEIGHT;
        int tileLevel = Utils.clamp(Utils.floorLog2(WMElement.CAMERASIZEVALUE1B1 / scale), 0, this.mLevelCount);
        Rect[] magnifierTileActiveRange = activeRange;
        Point magnifierTileOffset = offset;
        if (tileLevel != this.mLevelCount) {
            Rect magnifierTileRange = tileRange;
            getRange(tileRange, centerX, centerY, tileLevel, scale, rotation, (double) width, (double) height);
            offset.x = Math.round((((float) width) / 2.0f) + ((((float) tileRange.left) - centerX) * scale));
            offset.y = Math.round((((float) height) / 2.0f) + ((((float) tileRange.top) - centerY) * scale));
            fromLevel = ((float) (1 << tileLevel)) * scale > 0.75f ? tileLevel - 1 : tileLevel;
            tileImageFromLevel = this.mScale * ((float) (1 << this.mLevel)) > 0.75f ? this.mLevel - 1 : this.mLevel;
        } else {
            fromLevel = tileLevel - 2;
            tileImageFromLevel = this.mLevel - 2;
            offset.x = Math.round((((float) width) / 2.0f) - (centerX * scale));
            offset.y = Math.round((((float) height) / 2.0f) - (centerY * scale));
        }
        tileImageFromLevel = Math.max(0, Math.min(tileImageFromLevel, this.mLevelCount - 2));
        int tileImageEndLevel = Math.min(tileImageFromLevel + 2, this.mLevelCount);
        fromLevel = Math.max(0, Math.min(fromLevel, this.mLevelCount - 2));
        if (fromLevel == tileImageFromLevel) {
            fromLevel = Math.max(tileImageFromLevel - 2, 0);
        }
        int endLevel = Math.min(fromLevel + 2, this.mLevelCount);
        for (i = fromLevel; i < endLevel; i++) {
            getRange(activeRange[i - fromLevel], centerX, centerY, tileLevel, scale, rotation, (double) width, (double) height);
        }
        extraQueueClean();
        synchronized (this) {
            this.mDecodeQueue.clean();
            this.mUploadQueue.clean();
            this.mBackgroundTileUploaded = false;
            int n = this.mActiveTiles.size();
            i = 0;
            while (i < n) {
                Tile tile = (Tile) this.mActiveTiles.valueAt(i);
                int level = tile.mTileLevel;
                if (level < tileImageFromLevel || level > tileImageEndLevel) {
                    if (level < fromLevel || level >= endLevel || !activeRange[level - fromLevel].contains(tile.mX, tile.mY)) {
                        this.mActiveTiles.removeAt(i);
                        i--;
                        n--;
                        recycleTile(tile);
                    }
                }
                i++;
            }
        }
        for (i = fromLevel; i < endLevel; i++) {
            int y;
            int size = this.mTileSize << i;
            Rect r = activeRange[i - fromLevel];
            int bottom = r.bottom;
            for (y = r.top; y < bottom; y += size) {
                int x;
                int right = r.right;
                for (x = r.left; x < right; x += size) {
                    activateTile(x, y, i);
                }
            }
        }
        fromLevel = tileImageFromLevel;
        endLevel = tileImageEndLevel;
        Rect[] range = this.mActiveRange;
        for (i = tileImageFromLevel; i < tileImageEndLevel; i++) {
            size = this.mTileSize << i;
            r = range[i - tileImageFromLevel];
            bottom = r.bottom;
            for (y = r.top; y < bottom; y += size) {
                right = r.right;
                for (x = r.left; x < right; x += size) {
                    activateTile(x, y, i);
                }
            }
        }
        invalidate();
    }

    public void drawPhotoInMagnifier(GLCanvas canvas, Rect tileRange, Point offset, float scale) {
        TraceController.traceBegin("TileImageView.drawPhotoInMagnifier");
        renderTile(canvas, tileRange, Utils.clamp(Utils.floorLog2(WMElement.CAMERASIZEVALUE1B1 / scale), 0, this.mLevelCount), PhotoMagnifierView.MAGNIFIER_WIDTH, PhotoMagnifierView.MAGNIFIER_HEIGHT, offset.x, offset.y, scale, true);
        TraceController.traceEnd();
    }

    protected void render(GLCanvas canvas) {
        TraceController.traceBegin("TileImageView.render");
        renderTile(canvas, this.mTileRange, this.mLevel, getWidth(), getHeight(), this.mOffsetX, this.mOffsetY, this.mScale, false);
        TraceController.traceEnd();
    }

    protected void checkStatusBeforeRender(boolean isMagnifier) {
    }

    protected void drawTiles(GLCanvas canvas, Rect tileRange, int level, int offsetX, int offsetY, int size, float length, boolean isMagnifier) {
        int ty = tileRange.top;
        int i = 0;
        while (ty < tileRange.bottom) {
            float y = ((float) offsetY) + (((float) i) * length);
            int tx = tileRange.left;
            int j = 0;
            while (tx < tileRange.right) {
                drawTile(canvas, tx, ty, level, ((float) offsetX) + (((float) j) * length), y, length);
                tx += size;
                j++;
            }
            ty += size;
            i++;
        }
    }

    protected void renderTile(GLCanvas canvas, Rect tileRange, int level, int viewWidth, int viewHeight, int offsetX, int offsetY, float scale, boolean isMagnifier) {
        checkStatusBeforeRender(isMagnifier);
        this.mUploadQuota = 1;
        this.mRenderComplete = true;
        ExtraEffect extraEffect = this.mExtraEffect;
        boolean hasExtraEffect = extraEffect != null ? extraEffect.hasExtraEffect() : false;
        int rotation = this.mRotation;
        DirectShowNail directShowNail = this.mDirectShowNail;
        if (directShowNail != null) {
            rotation = directShowNail.getRotation();
        }
        if (rotation != 0) {
            hasExtraEffect = true;
        }
        int flags = 0;
        if (hasExtraEffect) {
            flags = 2;
        }
        if (flags != 0) {
            canvas.save(flags);
            if (hasExtraEffect) {
                int centerX = viewWidth / 2;
                int centerY = viewHeight / 2;
                canvas.translate((float) centerX, (float) centerY);
                canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
                if (extraEffect != null) {
                    extraEffect.applyExtraEffect(canvas);
                }
                canvas.translate((float) (-centerX), (float) (-centerY));
            }
        }
        if (this.mScreenNail instanceof TiledScreenNail) {
            ((TiledScreenNail) this.mScreenNail).setPreCanvasOperateParam((float) rotation, viewWidth / 2, viewHeight / 2);
        }
        try {
            if (level != this.mLevelCount && !isScreenNailAnimating()) {
                if (this.mScreenNail != null) {
                    this.mScreenNail.noDraw();
                }
                int size = this.mTileSize << level;
                drawTiles(canvas, tileRange, level, offsetX, offsetY, size, ((float) size) * scale, isMagnifier);
            } else if (this.mScreenNail != null) {
                this.mScreenNail.draw(canvas, offsetX, offsetY, Math.round(((float) this.mImageWidth) * scale), Math.round(((float) this.mImageHeight) * scale));
                if (isScreenNailAnimating()) {
                    invalidate();
                }
                if (directShowNail != null) {
                    directShowNail.recycle();
                    this.mDirectShowNail = null;
                }
            } else if (directShowNail != null) {
                directShowNail.draw(canvas);
            }
            if (flags != 0) {
                canvas.restore();
            }
            if (this.mRenderComplete) {
                synchronized (this) {
                    if (!this.mBackgroundTileUploaded) {
                        uploadBackgroundTiles(canvas);
                    }
                }
                return;
            }
            invalidate();
        } catch (Throwable th) {
            if (flags != 0) {
                canvas.restore();
            }
        }
    }

    private boolean isScreenNailAnimating() {
        if (this.mScreenNail instanceof TiledScreenNail) {
            return ((TiledScreenNail) this.mScreenNail).isAnimating();
        }
        return false;
    }

    private void uploadBackgroundTiles(GLCanvas canvas) {
        TraceController.traceBegin("TileImageView.uploadBackgroundTiles");
        this.mBackgroundTileUploaded = true;
        int n = this.mActiveTiles.size();
        for (int i = 0; i < n; i++) {
            Tile tile = (Tile) this.mActiveTiles.valueAt(i);
            if (!tile.isContentValid()) {
                queueForDecode(tile);
            }
        }
        TraceController.traceEnd();
    }

    void queueForUpload(Tile tile) {
        synchronized (this) {
            this.mUploadQueue.push(tile);
        }
        if (this.mTileUploader.mActive.compareAndSet(false, true)) {
            getGLRoot().addOnGLIdleListener(this.mTileUploader);
        }
    }

    synchronized void queueForDecode(Tile tile) {
        if (tile.mTileState == 1) {
            tile.mTileState = 2;
            if (this.mDecodeQueue.push(tile)) {
                notifyAll();
            }
        }
    }

    protected void aceTile(Tile tile) {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean decodeTile(Tile tile) {
        TraceController.traceBegin("TileImageView.decodeTile");
        synchronized (this) {
            if (tile.mTileState != 2) {
                TraceController.traceEnd();
                return false;
            }
            tile.mTileState = 4;
        }
    }

    protected synchronized Tile obtainTile(int x, int y, int level) {
        Tile tile = this.mRecycledQueue.pop();
        if (tile != null) {
            tile.mTileState = 1;
            tile.update(x, y, level);
            return tile;
        }
        return newInstanceTile(x, y, level);
    }

    protected Tile newInstanceTile(int x, int y, int level) {
        return new Tile(x, y, level);
    }

    protected synchronized void recycleTile(Tile tile) {
        if (tile.mTileState == 4) {
            tile.mTileState = 32;
            return;
        }
        tile.mTileState = 64;
        if (tile.mDecodedTile != null) {
            if (sTilePool != null) {
                sTilePool.recycle(tile.mDecodedTile);
            }
            tile.mDecodedTile = null;
        }
        this.mRecycledQueue.push(tile);
    }

    protected void activateExtraTileState(Tile tile) {
    }

    private void activateTile(int x, int y, int level) {
        TraceController.traceBegin("TileImageView.activateTile");
        long key = makeTileKey(x, y, level);
        Tile tile = (Tile) this.mActiveTiles.get(key);
        if (tile != null) {
            if (tile.mTileState == 2) {
                tile.mTileState = 1;
            }
            activateExtraTileState(tile);
            TraceController.traceEnd();
            return;
        }
        this.mActiveTiles.put(key, obtainTile(x, y, level));
        TraceController.traceEnd();
    }

    protected Tile getTile(int x, int y, int level) {
        return (Tile) this.mActiveTiles.get(makeTileKey(x, y, level));
    }

    protected static long makeTileKey(int x, int y, int level) {
        return (((((long) x) << 16) | ((long) y)) << 16) | ((long) level);
    }

    protected boolean tileCanNotUpload(Tile tile) {
        return tile.mTileState != 8;
    }

    public void drawTile(GLCanvas canvas, int tx, int ty, int level, float x, float y, float length) {
        TraceController.traceBegin("TileImageView.drawTile");
        RectF source = this.mSourceRect;
        RectF target = this.mTargetRect;
        target.set(x, y, x + length, y + length);
        source.set(0.0f, 0.0f, (float) this.mTileSize, (float) this.mTileSize);
        Tile tile = getTile(tx, ty, level);
        if (tile != null) {
            if (!tile.isContentValid()) {
                if (tile.mTileState == 8) {
                    if (this.mUploadQuota > 0) {
                        this.mUploadQuota--;
                        tile.updateContent(canvas);
                    } else {
                        this.mRenderComplete = false;
                    }
                } else if (tile.mTileState != 16) {
                    this.mRenderComplete = false;
                    queueForDecode(tile);
                }
            }
            if (drawTile(tile, canvas, source, target)) {
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

    boolean drawTile(Tile tile, GLCanvas canvas, RectF source, RectF target) {
        TraceController.traceBegin("static TileImageView.drawTile");
        while (!tile.isContentValid()) {
            Tile parent = tile.getParentTile();
            if (parent == null) {
                TraceController.traceEnd();
                return false;
            }
            if (tile.mX == parent.mX) {
                source.left /= 2.0f;
                source.right /= 2.0f;
            } else {
                source.left = (((float) this.mTileSize) + source.left) / 2.0f;
                source.right = (((float) this.mTileSize) + source.right) / 2.0f;
            }
            if (tile.mY == parent.mY) {
                source.top /= 2.0f;
                source.bottom /= 2.0f;
            } else {
                source.top = (((float) this.mTileSize) + source.top) / 2.0f;
                source.bottom = (((float) this.mTileSize) + source.bottom) / 2.0f;
            }
            tile = parent;
        }
        source.offset((float) this.mTileBorderSize, (float) this.mTileBorderSize);
        canvas.drawTexture(tile, source, target);
        TraceController.traceEnd();
        return true;
    }

    public void setReleativeView(GLView glView) {
    }
}
