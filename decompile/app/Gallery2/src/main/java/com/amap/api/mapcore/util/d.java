package com.amap.api.mapcore.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/* compiled from: AMapGLTextureView */
public class d extends k implements m {
    private l a;

    public d(Context context) {
        this(context, null);
    }

    public d(Context context, AttributeSet attributeSet) {
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

    public void setZOrderOnTop(boolean z) {
    }

    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i == 8 || i == 4) {
            this.a.e();
        } else if (i == 0) {
            this.a.d();
        }
    }

    protected void onDetachedFromWindow() {
        this.a.e();
        super.onDetachedFromWindow();
    }
}
