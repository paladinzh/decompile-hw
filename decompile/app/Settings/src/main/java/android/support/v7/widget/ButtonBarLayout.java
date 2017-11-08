package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.support.v4.content.res.ConfigurationHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$id;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class ButtonBarLayout extends LinearLayout {
    private boolean mAllowStacking;
    private int mLastWidthSize = -1;

    public ButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        boolean allowStackingDefault = ConfigurationHelper.getScreenHeightDp(getResources()) >= 320;
        TypedArray ta = context.obtainStyledAttributes(attrs, R$styleable.ButtonBarLayout);
        this.mAllowStacking = ta.getBoolean(R$styleable.ButtonBarLayout_allowStacking, allowStackingDefault);
        ta.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int initialWidthMeasureSpec;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (this.mAllowStacking) {
            if (widthSize > this.mLastWidthSize && isStacked()) {
                setStacked(false);
            }
            this.mLastWidthSize = widthSize;
        }
        boolean needsRemeasure = false;
        if (isStacked() || MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            initialWidthMeasureSpec = widthMeasureSpec;
        } else {
            initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE);
            needsRemeasure = true;
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);
        if (this.mAllowStacking && !isStacked()) {
            boolean stack;
            if (VERSION.SDK_INT >= 11) {
                stack = (ViewCompat.getMeasuredWidthAndState(this) & -16777216) == 16777216;
            } else {
                int childWidthTotal = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    childWidthTotal += getChildAt(i).getMeasuredWidth();
                }
                stack = (getPaddingLeft() + childWidthTotal) + getPaddingRight() > widthSize;
            }
            if (stack) {
                setStacked(true);
                needsRemeasure = true;
            }
        }
        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void setStacked(boolean stacked) {
        int i = 0;
        if (stacked) {
            i = 1;
        }
        setOrientation(i);
        setGravity(stacked ? 5 : 80);
        View spacer = findViewById(R$id.spacer);
        if (spacer != null) {
            spacer.setVisibility(stacked ? 8 : 4);
        }
        for (int i2 = getChildCount() - 2; i2 >= 0; i2--) {
            bringChildToFront(getChildAt(i2));
        }
    }

    private boolean isStacked() {
        return getOrientation() == 1;
    }
}
