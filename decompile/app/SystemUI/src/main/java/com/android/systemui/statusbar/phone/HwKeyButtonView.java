package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;

public class HwKeyButtonView extends KeyButtonView {
    public HwKeyButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTintType("navigationBarType");
    }

    public HwKeyButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTintType("navigationBarType");
    }

    protected void setTint(Drawable drawable) {
        if (drawable != null) {
            if (this.mIsResever) {
                if (-1275068417 != TintManager.getInstance().getIconColorByType(this.mTintType)) {
                    if (DEBUG) {
                        HwLog.i("TintImageView", "setTint:" + String.format("#%08X", new Object[]{Integer.valueOf(color)}) + " " + this);
                    }
                    drawable.setTintList(null);
                    drawable.setTint(getContext().getColor(R.color.navigation_bar_icon_color_black));
                } else if (TintManager.getInstance().isLuncherStyle()) {
                    drawable.setTint(getContext().getColor(R.color.navigation_bar_icon_luncher_color));
                } else {
                    drawable.setTint(getContext().getColor(R.color.navigation_bar_icon_color));
                }
                invalidate();
                return;
            }
            if (DEBUG) {
                HwLog.i("TintImageView", "no resever setTintList " + this);
            }
        }
    }
}
