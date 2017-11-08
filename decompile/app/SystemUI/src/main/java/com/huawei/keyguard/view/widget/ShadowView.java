package com.huawei.keyguard.view.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import com.huawei.keyguard.util.HwLog;

public class ShadowView extends View {
    Bitmap mBmp;
    Paint mPaint = new Paint();

    private ShadowView(View v) {
        super(v.getContext());
    }

    public static ShadowView createShadow(View v) {
        if (v == null) {
            HwLog.w("ShadowView", "createShadow view is null");
            return null;
        }
        ShadowView shadowView = new ShadowView(v);
        if (!shadowView.copyView(v)) {
            shadowView = null;
        }
        return shadowView;
    }

    public void setAlpha(float alpha) {
        this.mPaint.setAlpha((int) (255.0f * alpha));
    }

    public void draw(Canvas canvas) {
        if (this.mBmp != null) {
            canvas.drawBitmap(this.mBmp, 0.0f, 0.0f, this.mPaint);
        } else {
            HwLog.e("ShadowView", "bitmap is null, should not come here!!! ");
        }
    }

    public void clearBitmap() {
        if (this.mBmp != null) {
            this.mBmp.recycle();
        }
    }

    public boolean copyView(View v) {
        boolean willNotCache = v.willNotCacheDrawing();
        int color = v.getDrawingCacheBackgroundColor();
        v.setWillNotCacheDrawing(false);
        v.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            HwLog.e("ShadowView", "copyView failed: " + v);
            return false;
        }
        this.mBmp = Bitmap.createBitmap(cacheBitmap);
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return true;
    }
}
