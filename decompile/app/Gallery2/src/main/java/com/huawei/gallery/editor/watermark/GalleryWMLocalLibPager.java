package com.huawei.gallery.editor.watermark;

import android.content.Context;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.R;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.watermark.ui.watermarklib.WMLocalLibPager;

public class GalleryWMLocalLibPager extends WMLocalLibPager {
    private int mWMLocalLibViewPagerMarginTopLand;
    private int mWMLocalLibViewPagerMarginTopPort;

    public GalleryWMLocalLibPager(Context context) {
        super(context);
        setBackgroundColor(0);
    }

    public void onOrientationChanged(int orientation) {
        setLayoutParams();
    }

    public void setLayoutParams() {
        this.mWMLocalLibViewPagerMarginTopLand = getContext().getResources().getDimensionPixelOffset(R.dimen.watermark_pager_land_top_margin);
        this.mWMLocalLibViewPagerMarginTopPort = getContext().getResources().getDimensionPixelOffset(R.dimen.watermark_pager_port_top_margin);
        LayoutParams rl = new LayoutParams(-1, -2);
        if (LayoutHelper.isPort()) {
            rl.topMargin = this.mWMLocalLibViewPagerMarginTopPort;
        } else {
            rl.topMargin = this.mWMLocalLibViewPagerMarginTopLand;
        }
        rl.addRule(14);
        setLayoutParams(rl);
    }
}
