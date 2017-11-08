package com.android.huawei.slideunlock;

import android.content.Context;
import android.widget.TextView;

public class HwCustSlideUnlockScreen {
    Context context;
    TextView mUnlockTip;

    public HwCustSlideUnlockScreen(Context context) {
        this.context = context;
    }

    public void init() {
    }

    public void showTalkBackTips() {
    }

    public void addListener() {
    }

    public void removeListener() {
    }

    public void setUnlockText(TextView textView) {
        this.mUnlockTip = textView;
    }
}
