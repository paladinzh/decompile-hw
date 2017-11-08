package com.amap.api.mapcore.util;

import javax.microedition.khronos.opengles.GL10;

/* compiled from: GLOverlay */
public abstract class cl {
    private l a;

    public abstract int getZIndex();

    public abstract void onDrawFrame(GL10 gl10);

    public void destroy() {
        if (this.a != null) {
        }
    }
}
