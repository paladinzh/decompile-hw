package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.TileImageView.Model;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import java.util.ArrayList;

public class BitmapTileProvider implements Model {
    private final Config mConfig;
    private final int mImageHeight;
    private final int mImageWidth;
    private final Bitmap[] mMipmaps;
    private boolean mRecycled = false;
    private final ScreenNail mScreenNail;

    public BitmapTileProvider(Bitmap bitmap, int maxBackupSize) {
        this.mImageWidth = bitmap.getWidth();
        this.mImageHeight = bitmap.getHeight();
        ArrayList<Bitmap> list = new ArrayList();
        list.add(bitmap);
        while (true) {
            if (bitmap.getWidth() > maxBackupSize || bitmap.getHeight() > maxBackupSize) {
                bitmap = BitmapUtils.resizeBitmapByScale(bitmap, 0.5f, false);
                list.add(bitmap);
            } else {
                this.mScreenNail = new BitmapScreenNail((Bitmap) list.remove(list.size() - 1));
                this.mMipmaps = (Bitmap[]) list.toArray(new Bitmap[list.size()]);
                this.mConfig = Config.ARGB_8888;
                return;
            }
        }
    }

    public BitmapTileProvider(Bitmap bitmap) {
        this.mImageWidth = bitmap.getWidth();
        this.mImageHeight = bitmap.getHeight();
        ArrayList<Bitmap> list = new ArrayList();
        list.add(bitmap);
        this.mScreenNail = new BitmapScreenNail((Bitmap) list.remove(list.size() - 1));
        this.mMipmaps = (Bitmap[]) list.toArray(new Bitmap[list.size()]);
        this.mConfig = Config.ARGB_8888;
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
        return this.mMipmaps.length;
    }

    public Bitmap getTile(int level, int x, int y, int tileSize, int borderSize, BitmapPool pool) {
        x >>= level;
        y >>= level;
        int size = tileSize + (borderSize * 2);
        Bitmap result = pool == null ? null : pool.getBitmap();
        if (result == null) {
            result = Bitmap.createBitmap(size, size, this.mConfig);
        } else {
            result.eraseColor(0);
        }
        new Canvas(result).drawBitmap(this.mMipmaps[level], (float) ((-x) + borderSize), (float) ((-y) + borderSize), null);
        return result;
    }

    public MediaItem getCurrentMediaItem() {
        return null;
    }

    public ScreenNailCommonDisplayEnginePool getScreenNailCommonDisplayEnginePool() {
        return null;
    }
}
