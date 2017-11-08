package com.huawei.gallery.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.util.LayoutHelper;

public class PlaceHolderFragment extends AbstractGalleryFragment {
    private boolean mNeedLoadTip;

    public PlaceHolderFragment() {
        this(true);
    }

    public PlaceHolderFragment(boolean needLoadTip) {
        this.mNeedLoadTip = needLoadTip;
    }

    public void onResume() {
        updatePadding();
        super.onResume();
    }

    private void updatePadding() {
        getView().setPadding(0, LayoutHelper.getStatusBarHeight() + getGalleryActionBar().getActionBarHeight(), 0, 0);
    }

    protected void onCreateActionBar(Menu menu) {
        requestFeature(258);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return this.mNeedLoadTip ? inflater.inflate(R.layout.layout_loading, container, false) : new View(container.getContext());
    }

    public void onActionItemClicked(Action action) {
    }

    public boolean onBackPressed() {
        return false;
    }
}
