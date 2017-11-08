package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.SystemClock;
import com.android.gallery3d.ui.GLRoot.OnGLIdleListener;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class TiledTexture implements Texture {
    private static Paint sBitmapPaint;
    private static Canvas sCanvas;
    private static Tile sFreeTileHead = null;
    private static final Object sFreeTileLock = new Object();
    private static Paint sPaint;
    private static Bitmap sUploadBitmap;
    private final RectF mDestRect = new RectF();
    private final int mHeight;
    private final RectF mSrcRect = new RectF();
    private final Tile[] mTiles;
    private int mUploadIndex = 0;
    private final int mWidth;

    private static class Tile extends UploadedTexture {
        public Bitmap bitmap;
        public int contentHeight;
        public int contentWidth;
        public Tile nextFreeTile;
        public int offsetX;
        public int offsetY;

        private Tile() {
        }

        public void setSize(int width, int height) {
            this.contentWidth = width;
            this.contentHeight = height;
            this.mWidth = width + 2;
            this.mHeight = height + 2;
            this.mTextureWidth = 256;
            this.mTextureHeight = 256;
        }

        protected Bitmap onGetBitmap() {
            int x = 1 - this.offsetX;
            int y = 1 - this.offsetY;
            Bitmap bitmap = this.bitmap;
            if (!(bitmap == null || bitmap.isRecycled())) {
                int r = bitmap.getWidth() + x;
                int b = bitmap.getHeight() + y;
                TiledTexture.sCanvas.drawBitmap(bitmap, (float) x, (float) y, TiledTexture.sBitmapPaint);
                if (x > 0) {
                    TiledTexture.sCanvas.drawLine((float) (x - 1), 0.0f, (float) (x - 1), 256.0f, TiledTexture.sPaint);
                }
                if (y > 0) {
                    TiledTexture.sCanvas.drawLine(0.0f, (float) (y - 1), 256.0f, (float) (y - 1), TiledTexture.sPaint);
                }
                if (r < 254) {
                    TiledTexture.sCanvas.drawLine((float) r, 0.0f, (float) r, 256.0f, TiledTexture.sPaint);
                }
                if (b < 254) {
                    TiledTexture.sCanvas.drawLine(0.0f, (float) b, 256.0f, (float) b, TiledTexture.sPaint);
                }
            }
            return TiledTexture.sUploadBitmap;
        }

        protected void onFreeBitmap(Bitmap bitmap) {
        }
    }

    public static class Uploader implements OnGLIdleListener {
        private final GLRoot mGlRoot;
        private boolean mIsQueued = false;
        private final ArrayDeque<TiledTexture> mTextures = new ArrayDeque(8);

        public Uploader(GLRoot glRoot) {
            this.mGlRoot = glRoot;
        }

        public synchronized void clear() {
            this.mTextures.clear();
        }

        public synchronized void addTexture(TiledTexture t) {
            if (!t.isReady()) {
                this.mTextures.addLast(t);
                if (!this.mIsQueued) {
                    this.mIsQueued = true;
                    this.mGlRoot.addOnGLIdleListener(this);
                }
            }
        }

        public boolean onGLIdle(GLCanvas canvas, boolean renderRequested) {
            boolean z;
            ArrayDeque<TiledTexture> deque = this.mTextures;
            synchronized (this) {
                long now = SystemClock.uptimeMillis();
                long dueTime = now + 4;
                while (now < dueTime && !deque.isEmpty()) {
                    TiledTexture t = (TiledTexture) deque.peekFirst();
                    if (t != null && t.uploadNextTile(canvas)) {
                        deque.removeFirst();
                        this.mGlRoot.requestRender();
                    }
                    now = SystemClock.uptimeMillis();
                }
                this.mIsQueued = !this.mTextures.isEmpty();
                z = this.mIsQueued;
            }
            return z;
        }
    }

    private static void freeTile(Tile tile) {
        tile.invalidateContent();
        tile.bitmap = null;
        synchronized (sFreeTileLock) {
            tile.nextFreeTile = sFreeTileHead;
            sFreeTileHead = tile;
        }
    }

    private static Tile obtainTile() {
        synchronized (sFreeTileLock) {
            Tile result = sFreeTileHead;
            if (result == null) {
                Tile tile = new Tile();
                return tile;
            }
            sFreeTileHead = result.nextFreeTile;
            result.nextFreeTile = null;
            return result;
        }
    }

    private boolean uploadNextTile(GLCanvas canvas) {
        boolean z = true;
        if (this.mUploadIndex == this.mTiles.length) {
            return true;
        }
        synchronized (this.mTiles) {
            Tile[] tileArr = this.mTiles;
            int i = this.mUploadIndex;
            this.mUploadIndex = i + 1;
            Tile next = tileArr[i];
            if (next.bitmap != null) {
                next.updateContent(canvas);
            }
        }
        if (this.mUploadIndex != this.mTiles.length) {
            z = false;
        }
        return z;
    }

    public TiledTexture(Bitmap bitmap) {
        this.mWidth = bitmap.getWidth();
        this.mHeight = bitmap.getHeight();
        ArrayList<Tile> list = new ArrayList();
        int w = this.mWidth;
        for (int x = 0; x < w; x += 254) {
            int h = this.mHeight;
            for (int y = 0; y < h; y += 254) {
                Tile tile = obtainTile();
                tile.offsetX = x;
                tile.offsetY = y;
                tile.bitmap = bitmap;
                tile.setSize(Math.min(254, this.mWidth - x), Math.min(254, this.mHeight - y));
                list.add(tile);
            }
        }
        this.mTiles = (Tile[]) list.toArray(new Tile[list.size()]);
    }

    public boolean isReady() {
        return this.mUploadIndex == this.mTiles.length;
    }

    public void recycle() {
        synchronized (this.mTiles) {
            for (Tile freeTile : this.mTiles) {
                freeTile(freeTile);
            }
        }
    }

    public static void prepareResources() {
        sUploadBitmap = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
        sCanvas = new Canvas(sUploadBitmap);
        sBitmapPaint = new Paint(2);
        sBitmapPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        sPaint = new Paint();
        sPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        sPaint.setColor(0);
    }

    private static void mapRect(RectF output, RectF src, float x0, float y0, float x, float y, float scaleX, float scaleY) {
        output.set(((src.left - x0) * scaleX) + x, ((src.top - y0) * scaleY) + y, ((src.right - x0) * scaleX) + x, ((src.bottom - y0) * scaleY) + y);
    }

    public void draw(GLCanvas canvas, int x, int y, int width, int height) {
        RectF src = this.mSrcRect;
        RectF dest = this.mDestRect;
        float scaleX = ((float) width) / ((float) this.mWidth);
        float scaleY = ((float) height) / ((float) this.mHeight);
        synchronized (this.mTiles) {
            for (Tile t : this.mTiles) {
                src.set(0.0f, 0.0f, (float) t.contentWidth, (float) t.contentHeight);
                src.offset((float) t.offsetX, (float) t.offsetY);
                mapRect(dest, src, 0.0f, 0.0f, (float) x, (float) y, scaleX, scaleY);
                src.offset((float) (1 - t.offsetX), (float) (1 - t.offsetY));
                canvas.drawTexture(t, this.mSrcRect, this.mDestRect);
            }
        }
    }

    public void draw(GLCanvas canvas, RectF source, RectF target) {
        RectF src = this.mSrcRect;
        RectF dest = this.mDestRect;
        float x0 = source.left;
        float y0 = source.top;
        float x = target.left;
        float y = target.top;
        float scaleX = target.width() / source.width();
        float scaleY = target.height() / source.height();
        synchronized (this.mTiles) {
            for (Tile t : this.mTiles) {
                src.set(0.0f, 0.0f, (float) t.contentWidth, (float) t.contentHeight);
                src.offset((float) t.offsetX, (float) t.offsetY);
                if (src.intersect(source)) {
                    mapRect(dest, src, x0, y0, x, y, scaleX, scaleY);
                    src.offset((float) (1 - t.offsetX), (float) (1 - t.offsetY));
                    canvas.drawTexture(t, src, dest);
                }
            }
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void draw(GLCanvas canvas, int x, int y) {
        draw(canvas, x, y, this.mWidth, this.mHeight);
    }

    public boolean isOpaque() {
        return false;
    }
}
