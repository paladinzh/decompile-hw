package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.huawei.gallery.util.LayoutHelper;

public class BurstViewLayout extends RelativeLayout implements OnTouchListener {
    private int paddingBottomLand;
    private int paddingBottomPort;

    public BurstViewLayout(Context context) {
        super(context);
        init();
    }

    public BurstViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BurstViewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnTouchListener(this);
        this.paddingBottomPort = getResources().getDimensionPixelOffset(R.dimen.navigation_bar_height);
        this.paddingBottomLand = 0;
        setPadding(getResources().getConfiguration());
    }

    private void setPadding(Configuration config) {
        int paddingBottom = config.orientation == 1 ? this.paddingBottomPort : this.paddingBottomLand;
        if (LayoutHelper.isDefaultLandOrientationProduct() && config.orientation != 1) {
            paddingBottom += LayoutHelper.getNavigationBarHeightForDefaultLand();
        }
        int paddingTop = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        if (config.orientation != 1) {
            paddingTop += getResources().getDimensionPixelSize(17104919);
        }
        setPadding(0, paddingTop, 0, paddingBottom);
    }

    public boolean onTouch(View arg0, MotionEvent arg1) {
        return true;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setPadding(newConfig);
    }
}
