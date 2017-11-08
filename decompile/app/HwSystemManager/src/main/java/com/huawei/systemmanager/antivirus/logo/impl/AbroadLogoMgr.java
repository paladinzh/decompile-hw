package com.huawei.systemmanager.antivirus.logo.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.logo.ILogoManager;

public class AbroadLogoMgr implements ILogoManager {
    private Context mContext = this.mViewGroup.getContext();
    private TextView mLogoTextView = new TextView(this.mContext);
    private ViewGroup mViewGroup;

    public AbroadLogoMgr(ViewGroup viewGroup) {
        this.mViewGroup = viewGroup;
    }

    public void showLogo(boolean enableCloud) {
        this.mLogoTextView.setText(this.mContext.getResources().getString(R.string.virus_abroad_scan_logo_text));
    }

    public void initView() {
        this.mViewGroup.removeAllViews();
        LayoutInflater.from(this.mContext).inflate(R.layout.virus_abroad_logo_view, this.mViewGroup);
        this.mLogoTextView = (TextView) this.mViewGroup.findViewById(R.id.vendor_logo_txt);
    }
}
