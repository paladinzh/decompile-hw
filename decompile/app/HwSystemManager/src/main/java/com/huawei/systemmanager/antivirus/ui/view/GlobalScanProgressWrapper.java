package com.huawei.systemmanager.antivirus.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ui.view.IVirusScanProgressShow.ScanStatus;

public class GlobalScanProgressWrapper implements IVirusScanProgressShow {
    private Context mContext = this.mViewGroup.getContext();
    private TextView mScanInfoView = ((TextView) this.mViewGroup.findViewById(R.id.scan_info));
    private TextView mScanInfoViewItem = ((TextView) this.mViewGroup.findViewById(R.id.scan_info_item));
    private ViewGroup mViewGroup;

    public GlobalScanProgressWrapper(ViewGroup viewGroup) {
        this.mViewGroup = viewGroup;
    }

    public void initView() {
        this.mViewGroup.removeAllViews();
        LayoutInflater.from(this.mContext).inflate(R.layout.virus_globalscan_progress_layout, this.mViewGroup);
        this.mScanInfoView = (TextView) this.mViewGroup.findViewById(R.id.scan_info);
        this.mScanInfoViewItem = (TextView) this.mViewGroup.findViewById(R.id.scan_info_item);
    }

    public void show(String params) {
        this.mScanInfoView.setText(this.mContext.getString(R.string.virus_scanning));
        this.mScanInfoViewItem.setText(this.mContext.getString(R.string.virus_scanning_item, new Object[]{params}));
    }

    public void cancel() {
        this.mScanInfoView.setText(this.mContext.getString(R.string.antivirus_scan_uncomplete01));
        this.mScanInfoViewItem.setText(null);
    }

    public void finish(ScanStatus status) {
    }

    public void play() {
    }
}
