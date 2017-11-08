package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import java.util.Locale;

public class HwSystemIcons extends LinearLayout {
    DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    public HwSystemIcons(Context context) {
        super(context);
    }

    public HwSystemIcons(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwSystemIcons(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        if (getDisplay() != null) {
            getDisplay().getMetrics(this.mDisplayMetrics);
        }
        super.onConfigurationChanged(newConfig);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mDisplayMetrics.widthPixels == 0 && getDisplay() != null) {
            getDisplay().getMetrics(this.mDisplayMetrics);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(Math.max(this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels), Integer.MIN_VALUE), heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int offset;
        int i;
        super.onLayout(changed, left, top, right, bottom);
        View parent = (View) getParent();
        if (SystemUiUtil.isFaAr(Locale.getDefault().getLanguage())) {
            offset = left - (parent.getLeft() + parent.getPaddingLeft());
            if (offset < 0) {
                for (i = 0; i < getChildCount(); i++) {
                    getChildAt(i).offsetLeftAndRight(-offset);
                }
            }
        } else {
            offset = right - (parent.getRight() - parent.getPaddingRight());
            if (offset > 0) {
                for (i = 0; i < getChildCount(); i++) {
                    getChildAt(i).offsetLeftAndRight(-offset);
                }
            }
        }
        if (changed) {
            HwLog.i("HwSystemIcons", "onLayout:left=" + left + ", top=" + top + ", right=" + right + ", bottom=" + bottom + ", offset=" + offset);
        }
        LinearLayout statusIcons = (LinearLayout) findViewById(R.id.statusIcons);
        if (statusIcons != null) {
            for (i = 0; i < statusIcons.getChildCount(); i++) {
                View child = statusIcons.getChildAt(i);
                child.setAlpha(isOverflow(child) ? 0.0f : 1.0f);
            }
        }
        View networkSpeedView = findViewById(R.id.network_speed_view);
        if (networkSpeedView != null) {
            float f;
            if (isOverflow(networkSpeedView)) {
                f = 0.0f;
            } else {
                f = 1.0f;
            }
            networkSpeedView.setAlpha(f);
        }
    }

    public static boolean isOverflow(View child) {
        View parent = getSystemIcon(child);
        if (parent == null) {
            HwLog.w("HwSystemIcons", "isOverflow failed, child=" + child);
            return false;
        }
        int[] parentLoc = new int[2];
        parent.getLocationInWindow(parentLoc);
        int[] childLoc = new int[2];
        child.getLocationInWindow(childLoc);
        if (SystemUiUtil.isFaAr(Locale.getDefault().getLanguage())) {
            if (childLoc[0] + child.getWidth() > parentLoc[0] + parent.getWidth()) {
                return true;
            }
        } else if (childLoc[0] < parentLoc[0]) {
            return true;
        }
        return false;
    }

    static View getSystemIcon(View child) {
        if (child == null) {
            return null;
        }
        View parent = (View) child.getParent();
        while (parent != null && !(parent instanceof HwSystemIcons)) {
            parent = (View) parent.getParent();
        }
        return parent;
    }
}
