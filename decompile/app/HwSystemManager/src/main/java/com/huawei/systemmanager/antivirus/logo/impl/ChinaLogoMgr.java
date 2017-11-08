package com.huawei.systemmanager.antivirus.logo.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.logo.ILogoManager;

public class ChinaLogoMgr implements ILogoManager {
    private Context mContext = this.mViewGroup.getContext();
    private ImageView mLogoImageMiddleLine;
    private ImageView mLogoImageView;
    private ViewGroup mViewGroup;

    public ChinaLogoMgr(ViewGroup viewGroup) {
        this.mViewGroup = viewGroup;
    }

    public void showLogo(boolean enableCloud) {
        if (enableCloud) {
            if (this.mLogoImageView != null) {
                this.mLogoImageView.setVisibility(0);
            }
            if (this.mLogoImageMiddleLine != null) {
                this.mLogoImageMiddleLine.setVisibility(0);
                return;
            }
            return;
        }
        if (this.mLogoImageView != null) {
            this.mLogoImageView.setVisibility(8);
        }
        if (this.mLogoImageMiddleLine != null) {
            this.mLogoImageMiddleLine.setVisibility(8);
        }
    }

    public void initView() {
        this.mViewGroup.removeAllViews();
        LayoutInflater.from(this.mContext).inflate(R.layout.virus_china_logo_view, this.mViewGroup);
        this.mLogoImageView = (ImageView) this.mViewGroup.findViewById(R.id.vendor_logo_trustlook);
        this.mLogoImageMiddleLine = (ImageView) this.mViewGroup.findViewById(R.id.vendor_logo_middle_line);
    }
}
