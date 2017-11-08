package com.huawei.gallery.ui;

import com.android.gallery3d.ui.ColorTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;

public class RectView extends GLView {
    private boolean mNoRenderWhenPreparePageFadeOut;
    private ColorTexture mTexture;

    public RectView(int color, boolean noRenderWhenPreparePageFadeOut) {
        this.mTexture = new ColorTexture(color);
        this.mNoRenderWhenPreparePageFadeOut = noRenderWhenPreparePageFadeOut;
    }

    protected void render(GLCanvas canvas) {
        if ((!this.mNoRenderWhenPreparePageFadeOut || (canvas.getCustomFlag() & 2) == 0) && getWidth() > 0 && getHeight() > 0) {
            this.mTexture.draw(canvas, 0, 0, getWidth(), getHeight());
        }
    }
}
