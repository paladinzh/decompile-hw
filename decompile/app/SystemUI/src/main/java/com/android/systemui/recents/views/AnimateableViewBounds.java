package com.android.systemui.recents.views;

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewOutlineProvider;
import com.android.systemui.recents.misc.Utilities;

public class AnimateableViewBounds extends ViewOutlineProvider {
    @ExportedProperty(category = "recents")
    float mAlpha = 1.0f;
    @ExportedProperty(category = "recents")
    Rect mClipBounds = new Rect();
    @ExportedProperty(category = "recents")
    Rect mClipRect = new Rect();
    @ExportedProperty(category = "recents")
    int mCornerRadius;
    @ExportedProperty(category = "recents")
    Rect mLastClipBounds = new Rect();
    View mSourceView;

    public AnimateableViewBounds(View source, int cornerRadius) {
        this.mSourceView = source;
        this.mCornerRadius = cornerRadius;
    }

    public void reset() {
        this.mClipRect.set(-1, -1, -1, -1);
        updateClipBounds();
    }

    public void getOutline(View view, Outline outline) {
        outline.setAlpha(Utilities.mapRange(this.mAlpha, 0.1f, 0.8f));
        if (this.mCornerRadius > 0) {
            outline.setRoundRect(this.mClipRect.left, this.mClipRect.top, this.mSourceView.getWidth() - this.mClipRect.right, this.mSourceView.getHeight() - this.mClipRect.bottom, (float) this.mCornerRadius);
            return;
        }
        outline.setRect(this.mClipRect.left, this.mClipRect.top, this.mSourceView.getWidth() - this.mClipRect.right, this.mSourceView.getHeight() - this.mClipRect.bottom);
    }

    void setAlpha(float alpha) {
        if (Float.compare(alpha, this.mAlpha) != 0) {
            this.mAlpha = alpha;
            this.mSourceView.invalidateOutline();
        }
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public void setClipBottom(int bottom) {
        this.mClipRect.bottom = bottom;
        updateClipBounds();
    }

    private void updateClipBounds() {
        this.mClipBounds.set(Math.max(0, this.mClipRect.left), Math.max(0, this.mClipRect.top), this.mSourceView.getWidth() - Math.max(0, this.mClipRect.right), this.mSourceView.getHeight() - Math.max(0, this.mClipRect.bottom));
        if (!this.mLastClipBounds.equals(this.mClipBounds)) {
            this.mSourceView.setClipBounds(this.mClipBounds);
            this.mSourceView.invalidateOutline();
            this.mLastClipBounds.set(this.mClipBounds);
        }
    }
}
