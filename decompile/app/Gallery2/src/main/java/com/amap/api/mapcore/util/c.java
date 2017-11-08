package com.amap.api.mapcore.util;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/* compiled from: AMapGLSurfaceView */
public class c extends GLSurfaceView implements m {
    private l a;

    public c(Context context) {
        this(context, null);
    }

    public c(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.a = null;
        this.a = new b(this, context, attributeSet);
    }

    public l a() {
        return this.a;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return this.a.a(motionEvent);
    }

    protected void onWindowVisibilityChanged(int i) {
        if (i == 8 || i == 4) {
            this.a.e();
        } else if (i == 0) {
            this.a.d();
        }
        super.onWindowVisibilityChanged(i);
    }

    protected void onDetachedFromWindow() {
        this.a.e();
        super.onDetachedFromWindow();
    }
}
