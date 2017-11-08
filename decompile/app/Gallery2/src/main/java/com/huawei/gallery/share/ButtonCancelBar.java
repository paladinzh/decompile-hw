package com.huawei.gallery.share;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.gallery3d.R;

public class ButtonCancelBar extends LinearLayout {
    public ButtonCancelBar(Context context) {
        super(context);
    }

    public ButtonCancelBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonCancelBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int padding = getResources().getDimensionPixelOffset(R.dimen.share_dlg_btn_padding);
        setPadding(getPaddingLeft(), padding, getPaddingRight(), padding);
    }
}
