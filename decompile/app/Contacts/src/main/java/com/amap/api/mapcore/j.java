package com.amap.api.mapcore;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/* compiled from: AMapGLSurfaceView */
public class j extends GLSurfaceView implements ae {
    private ab a;

    public j(Context context) {
        this(context, null);
    }

    public j(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.a = null;
        this.a = new AMapDelegateImp(this, context, attributeSet);
    }

    public ab a() {
        return this.a;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return this.a.a(motionEvent);
    }

    protected void onWindowVisibilityChanged(int i) {
        if (i == 8 || i == 4) {
            this.a.g();
        } else if (i == 0) {
            this.a.f();
        }
        super.onWindowVisibilityChanged(i);
    }

    protected void onDetachedFromWindow() {
        this.a.g();
        super.onDetachedFromWindow();
    }
}
