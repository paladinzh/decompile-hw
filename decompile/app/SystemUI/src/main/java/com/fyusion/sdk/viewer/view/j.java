package com.fyusion.sdk.viewer.view;

import android.content.Context;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.fyusion.sdk.common.a.a;
import com.fyusion.sdk.common.k;

/* compiled from: Unknown */
class j extends f implements h {
    private m a = null;
    protected k c = null;

    public j(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setEGLContextClientVersion(2);
        this.c = c();
        setRenderer(this.c);
        setRenderMode(0);
    }

    public void a(float f, float f2) {
        if (this.c != null) {
            this.c.applyViewPan(f, f2);
            d();
        }
    }

    public void a(float f, float f2, float f3) {
        if (this.c != null) {
            this.c.applyViewScale(f, f2, f3);
            d();
        }
    }

    protected k c() throws IllegalStateException {
        if (this.c == null) {
            k kVar = new k();
            kVar.setOverlayCompositor(new a());
            return kVar;
        }
        throw new IllegalStateException("Cannot recreate renderer instances");
    }

    public boolean e() {
        return this.c != null;
    }

    public int getDisplayRotation() {
        return getDisplay() != null ? getDisplay().getRotation() : 0;
    }

    @Nullable
    public final i getTweeningRenderer() {
        return (i) this.c.getRenderer();
    }

    public void setImageMatrixPending(Matrix matrix) {
        this.c.setImageMatrixPending(matrix);
    }
}
