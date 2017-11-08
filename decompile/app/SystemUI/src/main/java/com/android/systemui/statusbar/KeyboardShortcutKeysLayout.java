package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;

public final class KeyboardShortcutKeysLayout extends ViewGroup {
    private final Context mContext;
    private int mLineHeight;

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        public final int mHorizontalSpacing;
        public final int mVerticalSpacing;

        public LayoutParams(int horizontalSpacing, int verticalSpacing, android.view.ViewGroup.LayoutParams viewGroupLayout) {
            super(viewGroupLayout);
            this.mHorizontalSpacing = horizontalSpacing;
            this.mVerticalSpacing = verticalSpacing;
        }

        public LayoutParams(int mHorizontalSpacing, int verticalSpacing) {
            super(0, 0);
            this.mHorizontalSpacing = mHorizontalSpacing;
            this.mVerticalSpacing = verticalSpacing;
        }
    }

    public KeyboardShortcutKeysLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    public KeyboardShortcutKeysLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childHeightMeasureSpec;
        int width = (MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        int height = (MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()) - getPaddingBottom();
        int lineHeight = 0;
        int xPos = getPaddingLeft();
        int yPos = getPaddingTop();
        if (MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        }
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                child.measure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), childHeightMeasureSpec);
                int childWidth = child.getMeasuredWidth();
                lineHeight = Math.max(lineHeight, child.getMeasuredHeight() + layoutParams.mVerticalSpacing);
                if (xPos + childWidth > width) {
                    xPos = getPaddingLeft();
                    yPos += lineHeight;
                }
                xPos += layoutParams.mHorizontalSpacing + childWidth;
            }
        }
        this.mLineHeight = lineHeight;
        if (MeasureSpec.getMode(heightMeasureSpec) == 0) {
            height = yPos + lineHeight;
        } else if (MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE && yPos + lineHeight < height) {
            height = yPos + lineHeight;
        }
        setMeasuredDimension(width, height);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        int spacing = getHorizontalVerticalSpacing();
        return new LayoutParams(spacing, spacing);
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        int spacing = getHorizontalVerticalSpacing();
        return new LayoutParams(spacing, spacing, layoutParams);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int xPos;
        int childCount = getChildCount();
        int fullRowWidth = r - l;
        if (isRTL()) {
            xPos = fullRowWidth - getPaddingRight();
        } else {
            xPos = getPaddingLeft();
        }
        int yPos = getPaddingTop();
        int lastHorizontalSpacing = 0;
        int rowStartIdx = 0;
        for (int i = 0; i < childCount; i++) {
            View currentChild = getChildAt(i);
            if (currentChild.getVisibility() != 8) {
                int currentChildWidth = currentChild.getMeasuredWidth();
                LayoutParams lp = (LayoutParams) currentChild.getLayoutParams();
                boolean childDoesNotFitOnRow = isRTL() ? (xPos - getPaddingLeft()) - currentChildWidth < 0 : xPos + currentChildWidth > fullRowWidth;
                if (childDoesNotFitOnRow) {
                    layoutChildrenOnRow(rowStartIdx, i, fullRowWidth, xPos, yPos, lastHorizontalSpacing);
                    if (isRTL()) {
                        xPos = fullRowWidth - getPaddingRight();
                    } else {
                        xPos = getPaddingLeft();
                    }
                    yPos += this.mLineHeight;
                    rowStartIdx = i;
                }
                if (isRTL()) {
                    xPos = (xPos - currentChildWidth) - lp.mHorizontalSpacing;
                } else {
                    xPos = (xPos + currentChildWidth) + lp.mHorizontalSpacing;
                }
                lastHorizontalSpacing = lp.mHorizontalSpacing;
            }
        }
        if (rowStartIdx < childCount) {
            layoutChildrenOnRow(rowStartIdx, childCount, fullRowWidth, xPos, yPos, lastHorizontalSpacing);
        }
    }

    private int getHorizontalVerticalSpacing() {
        return (int) TypedValue.applyDimension(1, 4.0f, getResources().getDisplayMetrics());
    }

    private void layoutChildrenOnRow(int startIndex, int endIndex, int fullRowWidth, int xPos, int yPos, int lastHorizontalSpacing) {
        if (!isRTL()) {
            xPos = ((getPaddingLeft() + fullRowWidth) - xPos) + lastHorizontalSpacing;
        }
        int j = startIndex;
        while (j < endIndex) {
            View currentChild = getChildAt(j);
            int currentChildWidth = currentChild.getMeasuredWidth();
            LayoutParams lp = (LayoutParams) currentChild.getLayoutParams();
            if (isRTL() && j == startIndex) {
                xPos = (((fullRowWidth - xPos) - getPaddingRight()) - currentChildWidth) - lp.mHorizontalSpacing;
            }
            currentChild.layout(xPos, yPos, xPos + currentChildWidth, currentChild.getMeasuredHeight() + yPos);
            if (isRTL()) {
                int nextChildWidth;
                if (j < endIndex - 1) {
                    nextChildWidth = getChildAt(j + 1).getMeasuredWidth();
                } else {
                    nextChildWidth = 0;
                }
                xPos -= lp.mHorizontalSpacing + nextChildWidth;
            } else {
                xPos += lp.mHorizontalSpacing + currentChildWidth;
            }
            j++;
        }
    }

    private boolean isRTL() {
        return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
    }
}
