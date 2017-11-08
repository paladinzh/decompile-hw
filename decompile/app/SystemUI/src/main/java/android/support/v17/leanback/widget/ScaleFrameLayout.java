package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class ScaleFrameLayout extends FrameLayout {
    private float mChildScale;
    private float mLayoutScaleX;
    private float mLayoutScaleY;

    public ScaleFrameLayout(Context context) {
        this(context, null);
    }

    public ScaleFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLayoutScaleX = 1.0f;
        this.mLayoutScaleY = 1.0f;
        this.mChildScale = 1.0f;
    }

    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        child.setScaleX(this.mChildScale);
        child.setScaleY(this.mChildScale);
    }

    protected boolean addViewInLayout(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        boolean ret = super.addViewInLayout(child, index, params, preventRequestLayout);
        if (ret) {
            child.setScaleX(this.mChildScale);
            child.setScaleY(this.mChildScale);
        }
        return ret;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float pivotX;
        int parentLeft;
        int parentRight;
        int parentTop;
        int parentBottom;
        int count = getChildCount();
        int layoutDirection = getLayoutDirection();
        if (layoutDirection == 1) {
            pivotX = ((float) getWidth()) - getPivotX();
        } else {
            pivotX = getPivotX();
        }
        if (this.mLayoutScaleX != 1.0f) {
            parentLeft = getPaddingLeft() + ((int) ((pivotX - (pivotX / this.mLayoutScaleX)) + 0.5f));
            parentRight = ((int) ((((((float) (right - left)) - pivotX) / this.mLayoutScaleX) + pivotX) + 0.5f)) - getPaddingRight();
        } else {
            parentLeft = getPaddingLeft();
            parentRight = (right - left) - getPaddingRight();
        }
        float pivotY = getPivotY();
        if (this.mLayoutScaleY != 1.0f) {
            parentTop = getPaddingTop() + ((int) ((pivotY - (pivotY / this.mLayoutScaleY)) + 0.5f));
            parentBottom = ((int) ((((((float) (bottom - top)) - pivotY) / this.mLayoutScaleY) + pivotY) + 0.5f)) - getPaddingBottom();
        } else {
            parentTop = getPaddingTop();
            parentBottom = (bottom - top) - getPaddingBottom();
        }
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int childLeft;
                int childTop;
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = 8388659;
                }
                int verticalGravity = gravity & 112;
                switch (Gravity.getAbsoluteGravity(gravity, layoutDirection) & 7) {
                    case 1:
                        childLeft = (((((parentRight - parentLeft) - width) / 2) + parentLeft) + lp.leftMargin) - lp.rightMargin;
                        break;
                    case 5:
                        childLeft = (parentRight - width) - lp.rightMargin;
                        break;
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                        break;
                }
                switch (verticalGravity) {
                    case 16:
                        childTop = (((((parentBottom - parentTop) - height) / 2) + parentTop) + lp.topMargin) - lp.bottomMargin;
                        break;
                    case 48:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case 80:
                        childTop = (parentBottom - height) - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                        break;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                child.setPivotX(pivotX - ((float) childLeft));
                child.setPivotY(pivotY - ((float) childTop));
            }
        }
    }

    private static int getScaledMeasureSpec(int measureSpec, float scale) {
        return scale == 1.0f ? measureSpec : MeasureSpec.makeMeasureSpec((int) ((((float) MeasureSpec.getSize(measureSpec)) / scale) + 0.5f), MeasureSpec.getMode(measureSpec));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mLayoutScaleX == 1.0f && this.mLayoutScaleY == 1.0f) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(getScaledMeasureSpec(widthMeasureSpec, this.mLayoutScaleX), getScaledMeasureSpec(heightMeasureSpec, this.mLayoutScaleY));
        setMeasuredDimension((int) ((((float) getMeasuredWidth()) * this.mLayoutScaleX) + 0.5f), (int) ((((float) getMeasuredHeight()) * this.mLayoutScaleY) + 0.5f));
    }

    public void setForeground(Drawable d) {
        throw new UnsupportedOperationException();
    }
}
