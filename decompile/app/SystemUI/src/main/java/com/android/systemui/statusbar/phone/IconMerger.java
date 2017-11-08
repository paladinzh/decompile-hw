package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.R;

public class IconMerger extends LinearLayout {
    protected int mIconHPadding;
    protected int mIconSize;
    private View mMoreView;

    public IconMerger(Context context, AttributeSet attrs) {
        super(context, attrs);
        reloadDimens();
    }

    protected void reloadDimens() {
        Resources res = this.mContext.getResources();
        this.mIconSize = res.getDimensionPixelSize(R.dimen.notification_icon_size);
        this.mIconHPadding = res.getDimensionPixelSize(R.dimen.notification_icon_padding);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadDimens();
    }

    public void setOverflowIndicator(View v) {
        this.mMoreView = v;
    }

    private int getFullIconWidth() {
        return this.mIconSize + (this.mIconHPadding * 2);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width - (width % getFullIconWidth()), getMeasuredHeight());
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        checkOverflow(r - l);
    }

    private void checkOverflow(int width) {
        boolean moreRequired = true;
        if (this.mMoreView != null) {
            boolean overflowShown;
            int N = getChildCount();
            int visibleChildren = 0;
            for (int i = 0; i < N; i++) {
                if (getChildAt(i).getVisibility() != 8) {
                    visibleChildren++;
                }
            }
            if (this.mMoreView.getVisibility() == 0) {
                overflowShown = true;
            } else {
                overflowShown = false;
            }
            if (overflowShown) {
                visibleChildren--;
            }
            if (getFullIconWidth() * visibleChildren <= width) {
                moreRequired = false;
            }
            if (moreRequired != overflowShown) {
                post(new Runnable() {
                    public void run() {
                        IconMerger.this.mMoreView.setVisibility(moreRequired ? 0 : 8);
                    }
                });
            }
        }
    }
}
