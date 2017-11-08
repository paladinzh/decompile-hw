package com.huawei.gallery.editor.watermark;

import android.content.Context;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.watermark.ui.watermarklib.WMLocalLibBaseView;

public class GalleryWMLocalLibBaseView extends WMLocalLibBaseView {
    public GalleryWMLocalLibBaseView(Context context) {
        super(context);
        setBackgroundColor();
    }

    public void setLayoutParams() {
        LayoutParams rl = new LayoutParams(-1, -2);
        rl.topMargin = GalleryUtils.dpToPixel(getResources().getConfiguration().orientation == 1 ? 48 : 37);
        rl.bottomMargin = GalleryUtils.dpToPixel(59);
        setLayoutParams(rl);
    }

    public void setBackgroundColor() {
        setBackgroundColor(0);
    }

    public void onOrientationChanged(int ori) {
    }
}
