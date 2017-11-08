package com.android.mms.attachment.ui.mediapicker.camerafocus;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import java.util.List;

public class PieItem {
    private float animate;
    private int inner;
    private float mAlpha;
    private float mCenter;
    private Drawable mDrawable;
    private boolean mEnabled;
    private List<PieItem> mItems;
    private OnClickListener mOnClickListener;
    private Path mPath;
    private boolean mSelected;
    private int outer;
    private float start;
    private float sweep;

    public interface OnClickListener {
        void onClick(PieItem pieItem);
    }

    public boolean hasItems() {
        return this.mItems != null;
    }

    public List<PieItem> getItems() {
        return this.mItems;
    }

    public void setPath(Path p) {
        this.mPath = p;
    }

    public Path getPath() {
        return this.mPath;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
        this.mDrawable.setAlpha((int) (255.0f * alpha));
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void setSelected(boolean s) {
        this.mSelected = s;
    }

    public boolean isSelected() {
        return this.mSelected;
    }

    public void setGeometry(float st, float sw, int inside, int outside) {
        this.start = st;
        this.sweep = sw;
        this.inner = inside;
        this.outer = outside;
    }

    public float getCenter() {
        return this.mCenter;
    }

    public float getStartAngle() {
        return this.start + this.animate;
    }

    public float getSweep() {
        return this.sweep;
    }

    public int getInnerRadius() {
        return this.inner;
    }

    public int getOuterRadius() {
        return this.outer;
    }

    public void performClick() {
        if (this.mOnClickListener != null) {
            this.mOnClickListener.onClick(this);
        }
    }

    public int getIntrinsicWidth() {
        return this.mDrawable.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return this.mDrawable.getIntrinsicHeight();
    }

    public void setBounds(int left, int top, int right, int bottom) {
        this.mDrawable.setBounds(left, top, right, bottom);
    }

    public void draw(Canvas canvas) {
        this.mDrawable.draw(canvas);
    }
}
