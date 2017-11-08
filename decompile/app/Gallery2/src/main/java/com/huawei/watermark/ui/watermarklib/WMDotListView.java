package com.huawei.watermark.ui.watermarklib;

import android.content.Context;
import com.huawei.watermark.ui.baseview.HorizontalListView;

public abstract class WMDotListView extends HorizontalListView {
    public abstract void onOrientationChanged(int i);

    public abstract void setLayoutParams();

    public WMDotListView(Context context) {
        super(context);
        setLayoutParams();
    }
}
