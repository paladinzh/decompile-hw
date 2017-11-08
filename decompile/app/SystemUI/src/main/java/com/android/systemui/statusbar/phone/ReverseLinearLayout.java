package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class ReverseLinearLayout extends LinearLayout {
    private boolean mIsLayoutRtl;

    public ReverseLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        boolean z = true;
        super.onFinishInflate();
        if (getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsLayoutRtl = z;
    }

    public void addView(View child) {
        reversParams(child.getLayoutParams());
        if (this.mIsLayoutRtl) {
            super.addView(child);
        } else {
            super.addView(child, 0);
        }
    }

    public void addView(View child, LayoutParams params) {
        reversParams(params);
        if (this.mIsLayoutRtl) {
            super.addView(child, params);
        } else {
            super.addView(child, 0, params);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateRTLOrder();
    }

    private void updateRTLOrder() {
        boolean isLayoutRtl = getResources().getConfiguration().getLayoutDirection() == 1;
        if (this.mIsLayoutRtl != isLayoutRtl) {
            int i;
            int childCount = getChildCount();
            ArrayList<View> childList = new ArrayList(childCount);
            for (i = 0; i < childCount; i++) {
                childList.add(getChildAt(i));
            }
            removeAllViews();
            for (i = childCount - 1; i >= 0; i--) {
                super.addView((View) childList.get(i));
            }
            this.mIsLayoutRtl = isLayoutRtl;
        }
    }

    private void reversParams(LayoutParams params) {
        if (params != null) {
            int width = params.width;
            params.width = params.height;
            params.height = width;
        }
    }
}
