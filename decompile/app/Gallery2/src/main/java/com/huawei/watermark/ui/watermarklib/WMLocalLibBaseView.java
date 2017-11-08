package com.huawei.watermark.ui.watermarklib;

import android.content.Context;
import android.widget.RelativeLayout;

public abstract class WMLocalLibBaseView extends RelativeLayout {
    public abstract void onOrientationChanged(int i);

    public abstract void setBackgroundColor();

    public abstract void setLayoutParams();

    public WMLocalLibBaseView(Context context) {
        super(context);
        setLayoutParams();
    }
}
