package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class ExpandableView extends FrameLayout {
    private int mActualHeight;
    private boolean mChangingPosition = false;
    protected Rect mClipRect = new Rect();
    protected boolean mClipToActualHeight = true;
    protected int mClipTopAmount;
    private boolean mDark;
    private ArrayList<View> mMatchParentViews = new ArrayList();
    private int mMinClipTopAmount = 0;
    protected OnHeightChangedListener mOnHeightChangedListener;
    protected boolean mOverlap;
    private ViewGroup mTransientContainer;
    private boolean mWillBeGone;

    public interface OnHeightChangedListener {
        void onHeightChanged(ExpandableView expandableView, boolean z);

        void onReset(ExpandableView expandableView);
    }

    public abstract void performAddAnimation(long j, long j2);

    public abstract void performRemoveAnimation(long j, float f, Runnable runnable);

    public ExpandableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ownHeight;
        int givenSize = MeasureSpec.getSize(heightMeasureSpec);
        int ownMaxHeight = Integer.MAX_VALUE;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (!(heightMode == 0 || givenSize == 0)) {
            ownMaxHeight = Math.min(givenSize, Integer.MAX_VALUE);
        }
        int newHeightSpec = MeasureSpec.makeMeasureSpec(ownMaxHeight, Integer.MIN_VALUE);
        int maxChildHeight = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int childHeightSpec = newHeightSpec;
                LayoutParams layoutParams = child.getLayoutParams();
                if (layoutParams.height != -1) {
                    if (layoutParams.height >= 0) {
                        if (layoutParams.height > ownMaxHeight) {
                            childHeightSpec = MeasureSpec.makeMeasureSpec(ownMaxHeight, 1073741824);
                        } else {
                            childHeightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, 1073741824);
                        }
                    }
                    child.measure(getChildMeasureSpec(widthMeasureSpec, 0, layoutParams.width), childHeightSpec);
                    maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
                } else {
                    this.mMatchParentViews.add(child);
                }
            }
        }
        if (heightMode == 1073741824) {
            ownHeight = givenSize;
        } else {
            ownHeight = Math.min(ownMaxHeight, maxChildHeight);
        }
        newHeightSpec = MeasureSpec.makeMeasureSpec(ownHeight, 1073741824);
        for (View child2 : this.mMatchParentViews) {
            child2.measure(getChildMeasureSpec(widthMeasureSpec, 0, child2.getLayoutParams().width), newHeightSpec);
        }
        this.mMatchParentViews.clear();
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), ownHeight);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateClipping();
    }

    public boolean pointInView(float localX, float localY, float slop) {
        float top = (float) this.mClipTopAmount;
        float bottom = (float) this.mActualHeight;
        if (localX < (-slop) || localY < top - slop || localX >= ((float) (this.mRight - this.mLeft)) + slop || localY >= bottom + slop) {
            return false;
        }
        return true;
    }

    public void setActualHeight(int actualHeight, boolean notifyListeners) {
        this.mActualHeight = actualHeight;
        updateClipping();
        if (notifyListeners) {
            notifyHeightChanged(false);
        }
    }

    public void setActualHeight(int actualHeight) {
        setActualHeight(actualHeight, true);
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public int getMaxContentHeight() {
        return getHeight();
    }

    public int getMinHeight() {
        return getHeight();
    }

    public int getCollapsedHeight() {
        return getHeight();
    }

    public void setDimmed(boolean dimmed, boolean fade) {
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        this.mDark = dark;
    }

    public boolean isDark() {
        return this.mDark;
    }

    public void setOverlap(boolean overlap) {
    }

    public void setHideSensitiveForIntrinsicHeight(boolean hideSensitive) {
    }

    public void setHideSensitive(boolean hideSensitive, boolean animated, long delay, long duration) {
    }

    public int getIntrinsicHeight() {
        return getHeight();
    }

    public void setClipTopAmount(int clipTopAmount) {
        this.mClipTopAmount = clipTopAmount;
        updateClipping();
    }

    public int getClipTopAmount() {
        return this.mClipTopAmount;
    }

    public void setOnHeightChangedListener(OnHeightChangedListener listener) {
        this.mOnHeightChangedListener = listener;
    }

    public boolean isContentExpandable() {
        return false;
    }

    public void notifyHeightChanged(boolean needsAnimation) {
        if (this.mOnHeightChangedListener != null) {
            this.mOnHeightChangedListener.onHeightChanged(this, needsAnimation);
        }
    }

    public boolean isTransparent() {
        return false;
    }

    public void setBelowSpeedBump(boolean below) {
    }

    public void setTranslation(float translation) {
        setTranslationX(translation);
    }

    public float getTranslation() {
        return getTranslationX();
    }

    public void onHeightReset() {
        if (this.mOnHeightChangedListener != null) {
            this.mOnHeightChangedListener.onReset(this);
        }
    }

    public void getDrawingRect(Rect outRect) {
        super.getDrawingRect(outRect);
        outRect.left = (int) (((float) outRect.left) + getTranslationX());
        outRect.right = (int) (((float) outRect.right) + getTranslationX());
        outRect.bottom = (int) ((((float) outRect.top) + getTranslationY()) + ((float) getActualHeight()));
        outRect.top = (int) (((float) outRect.top) + (getTranslationY() + ((float) getClipTopAmount())));
    }

    public void getBoundsOnScreen(Rect outRect, boolean clipToParent) {
        super.getBoundsOnScreen(outRect, clipToParent);
        if (((float) getTop()) + getTranslationY() < 0.0f) {
            outRect.top = (int) (((float) outRect.top) + (((float) getTop()) + getTranslationY()));
        }
        outRect.bottom = outRect.top + getActualHeight();
        outRect.top += getClipTopAmount();
    }

    public boolean isSummaryWithChildren() {
        return false;
    }

    public boolean areChildrenExpanded() {
        return false;
    }

    protected void updateClipping() {
        if (this.mClipToActualHeight) {
            int top = getClipTopAmount();
            if (top >= getActualHeight()) {
                top = getActualHeight() - 1;
            }
            this.mClipRect.set(0, top, getWidth(), getActualHeight() + getExtraBottomPadding());
            setClipBounds(this.mClipRect);
            return;
        }
        setClipBounds(null);
    }

    public void setClipToActualHeight(boolean clipToActualHeight) {
        this.mClipToActualHeight = clipToActualHeight;
        updateClipping();
    }

    public boolean willBeGone() {
        return this.mWillBeGone;
    }

    public void setWillBeGone(boolean willBeGone) {
        this.mWillBeGone = willBeGone;
    }

    public void setMinClipTopAmount(int minClipTopAmount) {
        this.mMinClipTopAmount = minClipTopAmount;
    }

    public void setLayerType(int layerType, Paint paint) {
        if (hasOverlappingRendering()) {
            super.setLayerType(layerType, paint);
        }
    }

    public boolean hasOverlappingRendering() {
        return super.hasOverlappingRendering() && getActualHeight() <= getHeight();
    }

    public float getShadowAlpha() {
        return 0.0f;
    }

    public void setShadowAlpha(float shadowAlpha) {
    }

    public float getIncreasedPaddingAmount() {
        return 0.0f;
    }

    public boolean mustStayOnScreen() {
        return false;
    }

    public void setFakeShadowIntensity(float shadowIntensity, float outlineAlpha, int shadowYEnd, int outlineTranslation) {
    }

    public float getOutlineAlpha() {
        return 0.0f;
    }

    public int getOutlineTranslation() {
        return 0;
    }

    public void setChangingPosition(boolean changingPosition) {
        this.mChangingPosition = changingPosition;
    }

    public boolean isChangingPosition() {
        return this.mChangingPosition;
    }

    public void setTransientContainer(ViewGroup transientContainer) {
        this.mTransientContainer = transientContainer;
    }

    public ViewGroup getTransientContainer() {
        return this.mTransientContainer;
    }

    public int getExtraBottomPadding() {
        return 0;
    }

    public boolean isGroupExpansionChanging() {
        return false;
    }

    public boolean isGroupExpanded() {
        return false;
    }

    public boolean isChildInGroup() {
        return false;
    }

    public void setActualHeightAnimating(boolean animating) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ExpandableView:" + this);
        pw.print(" mActualHeight=" + getActualHeight());
        pw.print(" mClipTopAmount=" + getClipTopAmount());
        pw.print(" mDark=" + this.mDark);
        pw.print(" mClipRect=" + this.mClipRect);
        pw.print(" mWillBeGone=" + this.mWillBeGone);
        pw.print(" mMinClipTopAmount=" + this.mMinClipTopAmount);
        pw.print(" mClipToActualHeight=" + this.mClipToActualHeight);
        pw.print(" mChangingPosition=" + this.mChangingPosition);
        pw.println();
    }
}
