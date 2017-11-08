package com.huawei.cspcommon.util;

import android.app.AbsWallpaperManager.IBlurWallpaperCallback;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.huawei.android.app.WallpaperManagerEx;
import java.util.HashMap;
import java.util.WeakHashMap;

public class WallPaperImageHelper implements IBlurWallpaperCallback {
    private static final boolean LOG_DEBUG = HwLog.HWDBG;
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

    public Bitmap getBitmapOnlyInCache(Rect rect, float mLeftTopRadius, float mLeftBottomRadius, float mRightTopRadius, float mRightBottomRadius) {
        Bitmap dest = (Bitmap) this.mImages.get(makeKey(rect, mLeftTopRadius, mLeftBottomRadius, mRightTopRadius, mRightBottomRadius));
        if (dest == null || !dest.isRecycled()) {
            return dest;
        }
        return null;
    }

    public Bitmap getBitmap(Rect rect, float mLeftTopRadius, float mLeftBottomRadius, float mRightTopRadius, float mRightBottomRadius) {
        String key = makeKey(rect, mLeftTopRadius, mLeftBottomRadius, mRightTopRadius, mRightBottomRadius);
        if (isRectNoRadiusBorder(mLeftTopRadius, mLeftBottomRadius, mRightTopRadius, mRightBottomRadius)) {
            Bitmap bitmap = WallpaperManagerEx.getBlurBitmap(this.mWallpaperManager, rect);
            printLogIfBitmapNullOrRecycled(bitmap, "WallpaperManagerEx get bitmap");
            if (LOG_DEBUG && bitmap != null) {
                HwLog.d("WallPaperImageHelper", "getBitmap no round corner:" + bitmap.hashCode() + " rect:" + rect.toShortString());
            }
            cacheBitmap(key, bitmap);
            return bitmap;
        }
        if (LOG_DEBUG) {
            HwLog.d("WallPaperImageHelper", "getBitmap:" + key);
        }
        Bitmap dest = (Bitmap) this.mImages.get(key);
        if (dest == null) {
            if (LOG_DEBUG) {
                HwLog.d("WallPaperImageHelper", "getBitmap dest == null");
            }
            String lkey = makeKey(rect, 0.0f, 0.0f, 0.0f, 0.0f);
            Bitmap src = (Bitmap) this.mImages.get(lkey);
            if (src == null || src.isRecycled()) {
                src = WallpaperManagerEx.getBlurBitmap(this.mWallpaperManager, rect);
                printLogIfBitmapNullOrRecycled(src, "WallpaperManagerEx get bitmap src");
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

    public static boolean isRectNoRadiusBorder(float leftTopRadius, float leftBottomRadius, float rightTopRadius, float rightBottomRadius) {
        if (leftTopRadius == 0.0f && leftBottomRadius == 0.0f && rightTopRadius == 0.0f && rightBottomRadius == 0.0f) {
            return true;
        }
        return false;
    }

    private void printLogIfBitmapNullOrRecycled(Bitmap target, String msg) {
        if (target == null) {
            HwLog.e("WallPaperImageHelper", msg + " is null!!");
        } else if (target.isRecycled()) {
            HwLog.e("WallPaperImageHelper", msg + " is Recycled!!");
        }
    }

    private Bitmap getRoundRectBitmap(Bitmap src, Rect rect, float mLeftTopRadius, float mLeftBottomRadius, float mRightTopRadius, float mRightBottomRadius) {
        Bitmap dest = null;
        try {
            if (LOG_DEBUG && src != null) {
                HwLog.d("WallPaperImageHelper", "src bitmap " + src.hashCode() + " width:" + src.getWidth() + " height:" + src.getHeight());
            }
            int scaleWidth = rect.width();
            int scaleHeight = rect.height();
            Bitmap scaled = src;
            if (scaleWidth > 0 && scaleHeight > 0) {
                scaled = Bitmap.createScaledBitmap(src, scaleWidth, scaleHeight, false);
            }
            dest = BitmapUtil.createRoundRectBitmap(scaled, mLeftTopRadius, mRightTopRadius, mLeftBottomRadius, mRightBottomRadius);
            if (LOG_DEBUG) {
                HwLog.d("WallPaperImageHelper", "dest bitmap " + dest.hashCode() + " width:" + dest.getWidth() + " height:" + dest.getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCapture.captureWallPaperImgException("WallPaperImageHelper->getRoundRectBitmap", e);
        }
        return dest;
    }

    public void cacheBitmap(String key, Bitmap bitmap) {
        if (LOG_DEBUG) {
            HwLog.d("WallPaperImageHelper", "cacheBitmap:" + key);
        }
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
        if (LOG_DEBUG) {
            HwLog.d("WallPaperImageHelper", "cleanCache");
        }
        this.mImages.clear();
    }
}
