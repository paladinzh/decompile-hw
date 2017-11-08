package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.android.gallery3d.R;

public class GLCoverView extends SurfaceView implements Callback {
    private final int BACKGROUND_COLOR;

    public GLCoverView(Context context) {
        this(context, null);
    }

    public GLCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        this.BACKGROUND_COLOR = context.getResources().getColor(R.color.album_background);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Canvas c = holder.lockCanvas();
        if (c != null) {
            c.drawColor(this.BACKGROUND_COLOR);
            holder.unlockCanvasAndPost(c);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
