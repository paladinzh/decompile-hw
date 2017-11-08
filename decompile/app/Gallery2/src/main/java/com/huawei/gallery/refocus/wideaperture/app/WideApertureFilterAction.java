package com.huawei.gallery.refocus.wideaperture.app;

import android.content.Context;

public class WideApertureFilterAction {
    private Context mContext;
    private WideApertureFilter mFilter;
    private String mName;
    private boolean mSelected;
    private int mWideaperturePhotoMode = -1;

    public WideApertureFilterAction(Context context, WideApertureFilter filter, int wideaperturePhotoMode) {
        this.mContext = context;
        setFilter(filter);
        setFilterName(filter.getFilterNameID());
        this.mWideaperturePhotoMode = wideaperturePhotoMode;
    }

    public String getFilterName() {
        return this.mName;
    }

    public void setFilterName(int nameID) {
        this.mName = this.mContext.getResources().getString(nameID);
    }

    public void setFilter(WideApertureFilter filter) {
        this.mFilter = filter;
    }

    public int getFilterIconID() {
        if (this.mWideaperturePhotoMode == 1) {
            return this.mFilter.getFilterMonoIconId();
        }
        return this.mFilter.getFilterColorfulIconID();
    }

    public int getFilterType() {
        return this.mFilter.getFilterType();
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }

    public boolean isSelected() {
        return this.mSelected;
    }
}
