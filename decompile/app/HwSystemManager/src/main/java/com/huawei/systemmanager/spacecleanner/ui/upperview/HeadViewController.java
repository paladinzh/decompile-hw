package com.huawei.systemmanager.spacecleanner.ui.upperview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.huawei.systemmanager.R;

public class HeadViewController {
    private LayoutInflater inflater;
    private View mScanContainerView = this.mScanHeadView.getHeadView();
    private ScanHeadView mScanHeadView = new ScanHeadView(this.inflater);
    private LinearLayout mScanLinearLayout = ((LinearLayout) this.mScanContainerView.findViewById(R.id.spaceclean_scan_linearlayout));
    private View mTrashContainerView = this.mTrashHeadView.getHeadView();
    private TrashHeadView mTrashHeadView = new TrashHeadView(this.inflater);
    private LinearLayout mTrashLinearLayout = ((LinearLayout) this.mTrashContainerView.findViewById(R.id.spaceclean_trash_linearlayout));

    public HeadViewController(Context context) {
        this.inflater = LayoutInflater.from(context);
        setScanTrashSize(0);
        setTrashTotalTrashSize(0);
    }

    public LinearLayout getScanLinearLayout() {
        return this.mScanLinearLayout;
    }

    public LinearLayout getTrashLinearLayout() {
        return this.mTrashLinearLayout;
    }

    public void addScanHeadView(ListView listView) {
        listView.removeHeaderView(this.mScanHeadView.getHeadView());
        listView.addHeaderView(this.mScanHeadView.getHeadView(), null, false);
    }

    public void setScanTrashSize(long size) {
        this.mScanHeadView.setTrashSize(size);
    }

    public void setScanTrashInfo(String str) {
        this.mScanHeadView.setTrashInfo(str);
    }

    public void setScanProgress(int percent) {
        this.mScanHeadView.setTrashProgress(percent);
    }

    public void setTrashTotalTrashSize(long size) {
        this.mTrashHeadView.setTotalTrashSize(size);
    }

    public void setTrashSelectedTrashSize(long size) {
        this.mTrashHeadView.setSelectedTrashSize(size);
    }

    public void setTrashCleaning() {
        this.mTrashHeadView.setTrashCleaning();
    }

    public void addTrashHeadView(ListView listView) {
        listView.removeHeaderView(this.mTrashHeadView.getHeadView());
        listView.addHeaderView(this.mTrashHeadView.getHeadView(), null, false);
    }
}
