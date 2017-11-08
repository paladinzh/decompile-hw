package com.android.systemui.statusbar;

import android.os.SystemProperties;
import com.android.systemui.R;

public class HwCustMSimSignalClusterViewImpl extends HwCustMSimSignalClusterView {
    private static final boolean SGLTE = SystemProperties.getBoolean("ro.config.hw_sglte", false);
    private static final boolean mDownloadBooster = SystemProperties.getBoolean("ro.config.hw_download_booster", false);
    private HwSignalClusterView mParent = null;

    public HwCustMSimSignalClusterViewImpl(HwSignalClusterView parent) {
        super(parent);
        this.mParent = parent;
    }

    public void dualCardNetworkBooster() {
        if (mDownloadBooster) {
            this.mParent.mDataPosFixed = false;
        }
    }

    public int getMobileActivityIconId(int iconId, boolean activityIn, boolean activityOut) {
        if (!SGLTE) {
            return iconId;
        }
        if (activityIn && activityOut) {
            return R.drawable.stat_sys_signal_inout_sglte;
        }
        if (activityIn) {
            return R.drawable.stat_sys_signal_in_sglte;
        }
        if (activityOut) {
            return R.drawable.stat_sys_signal_out_sglte;
        }
        return R.drawable.single_stat_sys_signal_connected_sglte;
    }
}
