package com.huawei.gallery.app.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class PhotoExtraButton extends ImageButton {
    private PhotoExtraButtonOverlay mPhotoExtraButtonOverlay;

    public PhotoExtraButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PhotoExtraButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoExtraButton(Context context) {
        super(context);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mPhotoExtraButtonOverlay != null) {
            this.mPhotoExtraButtonOverlay.onDraw(getResources(), canvas);
        }
    }

    public void setPhotoExtraButtonOverlay(PhotoExtraButtonOverlay overlay) {
        this.mPhotoExtraButtonOverlay = overlay;
    }

    public void refreshOverlayAnim() {
        invalidate();
    }
}
