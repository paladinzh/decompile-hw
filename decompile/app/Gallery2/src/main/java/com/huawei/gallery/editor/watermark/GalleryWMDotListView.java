package com.huawei.gallery.editor.watermark;

import android.content.Context;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.R;
import com.huawei.watermark.ui.watermarklib.WMDotListView;

public class GalleryWMDotListView extends WMDotListView {
    private int mDotListViewDefaultHeight;
    private int mDotListViewDefaultWidth;
    private int mDotListViewLandScapeMarginBottom;
    private int mDotListViewLandScapeMarginTop;
    private int mDotListViewPortraitBottomMargin;
    private int mDotListViewPortraitMarginBottom;
    private int mDotListViewPortraitMarginTop;

    public GalleryWMDotListView(Context context) {
        super(context);
        setBackgroundColor(0);
    }

    public void setLayoutParams() {
        this.mDotListViewDefaultWidth = getContext().getResources().getDimensionPixelOffset(R.dimen.dot_listview_default_width);
        this.mDotListViewDefaultHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.dot_listview_default_height);
        this.mDotListViewPortraitMarginTop = getContext().getResources().getDimensionPixelOffset(R.dimen.dot_listview_prot_margin_top);
        this.mDotListViewPortraitMarginBottom = getContext().getResources().getDimensionPixelOffset(R.dimen.dot_listview_port_margin_bottom);
        this.mDotListViewPortraitBottomMargin = getContext().getResources().getDimensionPixelOffset(R.dimen.dot_listview_port_bottom_margin);
        this.mDotListViewLandScapeMarginTop = getContext().getResources().getDimensionPixelOffset(R.dimen.dot_listview_land_margin_top);
        this.mDotListViewLandScapeMarginBottom = getContext().getResources().getDimensionPixelOffset(R.dimen.dot_listview_land_scape_margin_bootom);
        LayoutParams rl = new LayoutParams(this.mDotListViewDefaultWidth, this.mDotListViewDefaultHeight);
        rl.topMargin = this.mDotListViewPortraitMarginTop;
        rl.bottomMargin = this.mDotListViewPortraitMarginBottom;
        rl.addRule(14);
        setLayoutParams(rl);
    }

    public void onOrientationChanged(int orientation) {
        setLayoutParams(orientation);
    }

    public void setLayoutParams(int orientation) {
        LayoutParams rl = (LayoutParams) getLayoutParams();
        int i = (orientation == 0 || orientation == 180) ? this.mDotListViewPortraitMarginTop : this.mDotListViewLandScapeMarginTop;
        rl.topMargin = i;
        i = (orientation == 0 || orientation == 180) ? this.mDotListViewPortraitBottomMargin : this.mDotListViewLandScapeMarginBottom;
        rl.bottomMargin = i;
        setLayoutParams(rl);
    }
}
