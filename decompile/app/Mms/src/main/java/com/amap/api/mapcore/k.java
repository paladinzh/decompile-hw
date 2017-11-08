package com.amap.api.mapcore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/* compiled from: AMapGLTextureView */
public class k extends y implements ae {
    private ab a;

    public k(Context context) {
        this(context, null);
    }

    public k(Context context, AttributeSet attributeSet) {
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

    public void setZOrderOnTop(boolean z) {
    }

    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i == 8 || i == 4) {
            this.a.g();
        } else if (i == 0) {
            this.a.f();
        }
    }

    protected void onDetachedFromWindow() {
        this.a.g();
        super.onDetachedFromWindow();
    }
}
