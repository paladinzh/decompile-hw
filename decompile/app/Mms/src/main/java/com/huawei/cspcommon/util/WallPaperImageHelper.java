package com.huawei.cspcommon.util;

import android.app.AbsWallpaperManager.IBlurWallpaperCallback;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import com.huawei.android.app.WallpaperManagerEx;
import java.util.HashMap;
import java.util.WeakHashMap;

public class WallPaperImageHelper implements IBlurWallpaperCallback {
    private static WallPaperImageHelper instance;
    private WeakHashMap<BlurWallpaperChangedWatcher, Void> mBWWatcher = new WeakHashMap();
    private final HashMap<String, Bitmap> mImages = new HashMap();
    private WallpaperManager mWallpaperManager;

    public interface BlurWallpaperChangedWatcher {
        void onBlurWallpaperChanged();
    }

    private WallPaperImageHelper(Context context) {
        if (context != null) {
            this.mWallpaperManager = (WallpaperManager) context.getApplicationContext().getSystemService("wallpaper");
            WallpaperManagerEx.setCallback(this.mWallpaperManager, this);
        }
    }

    public static synchronized WallPaperImageHelper getInstance(Context context) {
        WallPaperImageHelper wallPaperImageHelper;
        synchronized (WallPaperImageHelper.class) {
            if (instance == null) {
                instance = new WallPaperImageHelper(context);
            }
            wallPaperImageHelper = instance;
        }
        return wallPaperImageHelper;
    }

    public void onBlurWallpaperChanged() {
        cleanCache();
        for (BlurWallpaperChangedWatcher wc : this.mBWWatcher.keySet()) {
            wc.onBlurWallpaperChanged();
        }
    }

    public Bitmap getBitmap(Rect rect, float mLeftTopRadius, float mLeftBottomRadius, float mRightTopRadius, float mRightBottomRadius) {
        String key = makeKey(rect, mLeftTopRadius, mLeftBottomRadius, mRightTopRadius, mRightBottomRadius);
        if (mLeftTopRadius == 0.0f && mLeftBottomRadius == 0.0f && mRightTopRadius == 0.0f && mRightBottomRadius == 0.0f) {
            Bitmap bitmap = WallpaperManagerEx.getBlurBitmap(this.mWallpaperManager, rect);
            if (bitmap == null) {
                Log.e("WallPaperImageHelper", "WallpaperManagerEx get bitmap is null!!");
            } else if (bitmap.isRecycled()) {
                Log.e("WallPaperImageHelper", "WallpaperManagerEx get bitmap isRecycled!!");
            }
            cacheBitmap(key, bitmap);
            return bitmap;
        }
        Bitmap dest = (Bitmap) this.mImages.get(key);
        if (dest == null) {
            String lkey = makeKey(rect, 0.0f, 0.0f, 0.0f, 0.0f);
            Bitmap src = (Bitmap) this.mImages.get(lkey);
            if (src == null || src.isRecycled()) {
                src = WallpaperManagerEx.getBlurBitmap(this.mWallpaperManager, rect);
                if (src == null) {
                    Log.e("WallPaperImageHelper", "WallpaperManagerEx get bitmap src is null!!");
                } else if (src.isRecycled()) {
                    Log.e("WallPaperImageHelper", "WallpaperManagerEx get bitmap src isRecycled!!");
                }
                cacheBitmap(lkey, src);
            }
            if (src == null || src.isRecycled()) {
                return null;
            }
            dest = getRoundRectBitmap(src, rect, mLeftTopRadius, mLeftBottomRadius, mRightTopRadius, mRightBottomRadius);
            cacheBitmap(key, dest);
        }
        return dest;
    }

    private Bitmap getRoundRectBitmap(Bitmap src, Rect rect, float mLeftTopRadius, float mLeftBottomRadius, float mRightTopRadius, float mRightBottomRadius) {
        Bitmap dest = null;
        try {
            int scaleWidth = rect.width();
            int scaleHeight = rect.height();
            Bitmap scaled = src;
            if (scaleWidth > 0 && scaleHeight > 0) {
                scaled = Bitmap.createScaledBitmap(src, scaleWidth, scaleHeight, false);
            }
            dest = BitmapUtil.createRoundRectBitmap(scaled, mLeftTopRadius, mRightTopRadius, mLeftBottomRadius, mRightBottomRadius);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dest;
    }

    public void cacheBitmap(String key, Bitmap bitmap) {
        if (this.mImages != null) {
            this.mImages.put(key, bitmap);
        }
    }

    public static String makeKey(Rect rect, float mLeftTopRadius, float mLeftBottomRadius, float mRightTopRadius, float mRightBottomRadius) {
        StringBuffer sb = new StringBuffer();
        sb.append(rect.toShortString()).append('-').append(mLeftTopRadius).append('-').append(mLeftBottomRadius).append('-').append(mRightTopRadius).append('-').append(mRightBottomRadius);
        return sb.toString();
    }

    private void cleanCache() {
        this.mImages.clear();
    }

    public void setBlurWallpaperChangedListener(BlurWallpaperChangedWatcher watcher) {
        this.mBWWatcher.put(watcher, null);
    }

    public void clearBlurWallpaperChangedListener(BlurWallpaperChangedWatcher watcher) {
        this.mBWWatcher.remove(watcher);
    }
}
