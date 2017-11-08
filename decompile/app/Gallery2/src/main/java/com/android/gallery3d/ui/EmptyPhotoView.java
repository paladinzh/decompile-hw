package com.android.gallery3d.ui;

import android.graphics.Rect;
import com.huawei.gallery.anim.PhotoFallbackEffect;

public class EmptyPhotoView extends AbsPhotoView {
    public float getScaleForAnimation(float progress) {
        return 0.0f;
    }

    public boolean getFilmMode() {
        return false;
    }

    public Rect getPhotoRect(int index) {
        return null;
    }

    public PhotoFallbackEffect buildFallbackEffect(GLView root, GLCanvas canvas) {
        return new PhotoFallbackEffect();
    }

    public boolean isTileViewFromCache() {
        return false;
    }

    public boolean isExtraActionDoing() {
        return false;
    }

    public boolean resetToFullView() {
        return false;
    }
}
