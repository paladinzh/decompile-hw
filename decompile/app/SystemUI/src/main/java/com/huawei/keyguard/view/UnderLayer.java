package com.huawei.keyguard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.util.HwLog;

public class UnderLayer extends FrameLayout {
    Bitmap mBmp;
    int mHeight;
    int mSlideY = 0;
    int mWidth;

    public UnderLayer(Context context) {
        super(context);
        HwLog.i("UnderLayer", "UnderLayer view");
    }

    protected void dispatchDraw(Canvas canvas) {
        HwLog.d("UnderLayer", "ondraw " + this.mSlideY + ", " + canvas.getClipBounds());
        if (this.mBmp == null) {
            post(new Runnable() {
                public void run() {
                    UnderLayer.this.createDefaultBitmap();
                }
            });
            return;
        }
        canvas.clipRect(0, this.mHeight - this.mSlideY, this.mWidth, this.mHeight);
        synchronized (this.mBmp) {
            canvas.drawBitmap(this.mBmp, 0.0f, 0.0f, null);
        }
    }

    private static View inflateWidgetView(Context context) {
        return KgViewUtils.inflateViewFromPkg(context, "com.huawei.camera", "com.huawei.camera:layout/keyguard_widget");
    }

    public void createDefaultBitmap() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            HwLog.w("UnderLayer", "Skip create bitmap as view not initialized");
            return;
        }
        this.mWidth = width;
        this.mHeight = height;
        View viewToShow = getChildAt(0);
        if (viewToShow == null) {
            viewToShow = inflateWidgetView(getContext());
            if (viewToShow != null) {
                addView(viewToShow, -1, -1);
                KeyguardCfg.setCameraExists(true);
            }
        } else if (viewToShow.getHeight() == 0 || viewToShow.getWidth() == 0) {
            HwLog.w("UnderLayer", "skip draw view as height or width is 0");
        } else {
            HwLog.w("UnderLayer", "create bitmap view size: " + viewToShow.getWidth() + " " + viewToShow.getHeight());
            HwLog.w("UnderLayer", "Create bmp with view: " + viewToShow.getWidth() + " " + viewToShow.getHeight());
            Bitmap bmp = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.RGB_565);
            viewToShow.draw(new Canvas(bmp));
            removeView(viewToShow);
            HwLog.w("UnderLayer", "Create bmp with " + bmp.getWidth() + " " + bmp.getHeight() + "   " + bmp);
            this.mBmp = bmp;
        }
    }

    public void setClipY(int slideY) {
        HwLog.d("UnderLayer", "setClipY " + slideY);
        if (slideY < 0) {
            slideY = -slideY;
        }
        this.mSlideY = slideY;
        invalidate();
    }
}
