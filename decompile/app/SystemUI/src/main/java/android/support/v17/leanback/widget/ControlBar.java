package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

class ControlBar extends LinearLayout {
    private int mChildMarginFromCenter;
    private OnChildFocusedListener mOnChildFocusedListener;

    public interface OnChildFocusedListener {
        void onChildFocusedListener(View view, View view2);
    }

    public ControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (getChildCount() <= 0 || !getChildAt(getChildCount() / 2).requestFocus(direction, previouslyFocusedRect)) {
            return super.requestFocus(direction, previouslyFocusedRect);
        }
        return true;
    }

    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (this.mOnChildFocusedListener != null) {
            this.mOnChildFocusedListener.onChildFocusedListener(child, focused);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mChildMarginFromCenter > 0) {
            int totalExtraMargin = 0;
            for (int i = 0; i < getChildCount() - 1; i++) {
                View first = getChildAt(i);
                View second = getChildAt(i + 1);
                int marginStart = this.mChildMarginFromCenter - ((first.getMeasuredWidth() + second.getMeasuredWidth()) / 2);
                LayoutParams lp = (LayoutParams) second.getLayoutParams();
                int extraMargin = marginStart - lp.getMarginStart();
                lp.setMarginStart(marginStart);
                second.setLayoutParams(lp);
                totalExtraMargin += extraMargin;
            }
            setMeasuredDimension(getMeasuredWidth() + totalExtraMargin, getMeasuredHeight());
        }
    }
}
