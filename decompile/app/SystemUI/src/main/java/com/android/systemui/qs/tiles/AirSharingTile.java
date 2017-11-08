package com.android.systemui.qs.tiles;

import android.app.StatusBarManager;
import android.content.Intent;
import android.os.UserHandle;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;

public class AirSharingTile extends QSTile<State> {
    public AirSharingTile(Host host) {
        super(host);
    }

    public State newTileState() {
        return new State();
    }

    private Intent getAirShareIntent() {
        String action;
        String pkgName;
        if (SystemUiUtil.isPackageExist(this.mContext, "com.huawei.android.airsharing")) {
            action = "com.huawei.android.airsharing.action.ACTION_DEVICE_SELECTOR";
            pkgName = "com.huawei.android.airsharing";
        } else {
            action = "com.huawei.android.mirrorshare.action.ACTION_DEVICE_SELECTOR";
            pkgName = "com.huawei.android.mirrorshare";
        }
        Intent intent = new Intent(action);
        intent.setPackage(pkgName);
        intent.addFlags(268435456);
        intent.addFlags(262144);
        intent.putExtra("com.huawei.android.airsharing.DEVICE_SELECTOR_CALLER", "com.huawei.android.toolbox");
        return intent;
    }

    protected void handleClick() {
        ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
        if (HwKeyguardUpdateMonitor.getInstance(this.mContext).isRestrictAsEncrypt()) {
            this.mHost.startActivityDismissingKeyguard(getAirShareIntent());
        } else {
            try {
                this.mContext.startActivityAsUser(getAirShareIntent(), UserHandle.CURRENT);
            } catch (Exception ex) {
                HwLog.w("AirSharingTracker", "AirSharingTracker requestStateChange startActivity exception: " + ex.getMessage());
            }
        }
        refreshState();
    }

    protected void handleUpdateState(State state, Object arg) {
        if (SystemUiUtil.isPackageExist(this.mContext, "com.huawei.android.airsharing")) {
            state.label = this.mContext.getString(R.string.airsharing_widget_name);
        } else {
            state.label = this.mContext.getString(R.string.airsharing_widget_name_overseas);
        }
        state.icon = new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_airsharing_tile));
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return null;
    }

    public CharSequence getTileLabel() {
        if (SystemUiUtil.isPackageExist(this.mContext, "com.huawei.android.airsharing")) {
            return this.mContext.getString(R.string.airsharing_widget_name);
        }
        return this.mContext.getString(R.string.airsharing_widget_name_overseas);
    }

    public void setListening(boolean listening) {
    }

    public boolean isAvailable() {
        if (UserSwitchUtils.getCurrentUser() != 0) {
            return false;
        }
        boolean z;
        if (SystemUiUtil.isPackageExist(this.mContext, "com.huawei.android.airsharing")) {
            z = true;
        } else {
            z = SystemUiUtil.isPackageExist(this.mContext, "com.huawei.android.mirrorshare");
        }
        return z;
    }
}
