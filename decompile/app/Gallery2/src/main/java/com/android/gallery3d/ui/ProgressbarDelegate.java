package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.ProgressBar;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.watermark.manager.parse.WMElement;

public class ProgressbarDelegate extends ProgressBar {
    private ScreenNailRoot mRoot;
    private AbstractGifScreenNail mScreenNail;

    public interface ScreenNailRoot {
        int getRootBgColor();

        void invalidate();
    }

    public ProgressbarDelegate(Context context, ScreenNailRoot root, AbstractGifScreenNail screenNail) {
        super(context);
        this.mRoot = root;
        this.mScreenNail = screenNail;
    }

    protected void onDraw(Canvas canvas0) {
        super.onDraw(canvas0);
        if (this.mRoot != null && this.mScreenNail != null) {
            AbstractGifScreenNail bsn = this.mScreenNail;
            boolean shouldResize = false;
            float sx = WMElement.CAMERASIZEVALUE1B1;
            float sy = WMElement.CAMERASIZEVALUE1B1;
            int width = getWidth();
            int height = getHeight();
            if (width * height > 1048576) {
                int frameWidth = width;
                int frameHeight = height;
                width = bsn.getWidth();
                height = bsn.getHeight();
                sx = ((float) width) / ((float) frameWidth);
                sy = ((float) height) / ((float) frameHeight);
                GalleryLog.i("ProgressbarScreenNail", String.format("ScreenNail[%s,%s],Frame[%s,%s], scale[%s,%s]", new Object[]{Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(frameWidth), Integer.valueOf(frameHeight), Float.valueOf(sx), Float.valueOf(sy)}));
                shouldResize = true;
            }
            int backgroundColor = this.mRoot.getRootBgColor();
            Bitmap frameBmp = bsn.dequeue(width, height);
            if (frameBmp != null) {
                frameBmp.eraseColor(backgroundColor);
                Canvas canvas = new Canvas(frameBmp);
                if (shouldResize) {
                    canvas.scale(sx, sy);
                }
                super.draw(canvas);
                bsn.enqueue(frameBmp);
            }
            this.mRoot.invalidate();
        }
    }
}
