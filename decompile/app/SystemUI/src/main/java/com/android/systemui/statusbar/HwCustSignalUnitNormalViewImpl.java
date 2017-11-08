package com.android.systemui.statusbar;

import android.graphics.drawable.AnimationDrawable;
import android.os.SystemProperties;
import android.widget.ImageView;

public class HwCustSignalUnitNormalViewImpl extends HwCustSignalUnitNormalView {
    private static final boolean IS_ATT = SystemProperties.getBoolean("ro.config.replace_signal_icon", false);
    private static final boolean mDownloadBooster = SystemProperties.getBoolean("ro.config.hw_download_booster", false);
    private AnimationDrawable animationDrawable;
    protected SignalUnitNormalView mParent;

    public HwCustSignalUnitNormalViewImpl(SignalUnitNormalView parent) {
        super(parent);
        this.mParent = parent;
    }

    public void dualCardNetworkBooster() {
        if (mDownloadBooster) {
            this.mParent.mDataPosFixed = false;
        }
    }

    public void updateViewAnimation(ImageView targetView) {
        if (IS_ATT) {
            this.animationDrawable = (AnimationDrawable) targetView.getDrawable();
            if (this.animationDrawable != null) {
                this.animationDrawable.start();
            }
        }
    }
}
