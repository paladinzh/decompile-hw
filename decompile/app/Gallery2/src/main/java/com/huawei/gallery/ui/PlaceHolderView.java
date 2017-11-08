package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.ColorTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.Texture;
import com.huawei.gallery.util.ImmersionUtils;
import com.huawei.gallery.util.UIUtils;

public class PlaceHolderView extends GLView {
    private ColorTexture mColorTexture;
    private final Context mContext;
    private BitmapTexture mTexture = null;
    private boolean mWallPaperChange = true;

    public PlaceHolderView(Context context) {
        this.mContext = context;
        this.mColorTexture = ImmersionUtils.getColorTexture(context);
    }

    public void textureDirty() {
        this.mWallPaperChange = true;
    }

    public void updateTexture() {
        if (this.mWallPaperChange && this.mColorTexture == null) {
            this.mWallPaperChange = false;
            Rect rect = new Rect(0, 0, getWidth(), getHeight());
            if (rect.width() > 0 && rect.height() > 0) {
                BitmapTexture texture = new BitmapTexture(UIUtils.getWallpaperBitmap(this.mContext, rect));
                synchronized (this) {
                    if (this.mTexture != null) {
                        this.mTexture.recycle();
                    }
                    this.mTexture = texture;
                }
            }
        }
    }

    protected void render(GLCanvas canvas) {
        updateTexture();
        synchronized (this) {
            Texture texture = this.mColorTexture != null ? this.mColorTexture : this.mTexture;
            if (texture != null) {
                texture.draw(canvas, 0, 0, getWidth(), getHeight());
            }
        }
    }

    protected boolean onTouch(MotionEvent event) {
        return true;
    }
}
