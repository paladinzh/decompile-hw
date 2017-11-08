package com.huawei.gallery.anim;

import com.android.gallery3d.ui.GLCanvas;

public abstract class CanvasAnimation extends Animation {
    public abstract void apply(GLCanvas gLCanvas);

    public abstract int getCanvasSaveFlags();
}
