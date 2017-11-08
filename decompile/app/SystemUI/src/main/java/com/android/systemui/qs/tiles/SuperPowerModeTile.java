package com.android.systemui.qs.tiles;

import android.app.StatusBarManager;
import android.content.Intent;
import android.os.SystemProperties;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;

public class SuperPowerModeTile extends QSTile<State> {
    private static final boolean SUPER_POWERMODE_ENABLE = SystemProperties.getBoolean("ro.config.show_superpower", true);

    public SuperPowerModeTile(Host host) {
        super(host);
    }

    public State newTileState() {
        return new State();
    }

    protected void handleClick() {
        if (!isSuperPowerModeDisable()) {
            ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
            Intent superPowerIntent = new Intent("huawei.intent.action.HWSYSTEMMANAGER_START_SUPER_POWERMODE");
            superPowerIntent.addFlags(268435456);
            superPowerIntent.putExtra("package_name", "com.android.toolbox");
            superPowerIntent.setPackage("com.huawei.systemmanager");
            this.mContext.sendBroadcast(superPowerIntent);
        }
    }

    protected void handleUpdateState(State state, Object arg) {
        if (isSuperPowerModeDisable()) {
            state.icon = ResourceIcon.get(R.drawable.ic_superpowermode_tile_disable);
            state.labelTint = 2;
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_superpowermode_tile_def);
            state.labelTint = 0;
        }
        state.label = this.mContext.getString(R.string.super_power_widget_name);
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        if (isSuperPowerModeDisable()) {
            return null;
        }
        Intent intent = new Intent("huawei.intent.action.POWER_MANAGER");
        intent.setPackage("com.huawei.systemmanager");
        if (!SystemUiUtil.isIntentAvailable(this.mContext, intent)) {
            return new Intent();
        }
        intent.putExtra("package_name", this.mContext.getPackageName());
        return intent;
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.super_power_widget_name);
    }

    public void setListening(boolean listening) {
    }

    public boolean isAvailable() {
        boolean z = false;
        if (UserSwitchUtils.getCurrentUser() != 0) {
            return false;
        }
        if (SUPER_POWERMODE_ENABLE && !SystemUiUtil.isWifiOnly(this.mContext)) {
            z = SystemUiUtil.isVoiceCapable(this.mContext);
        }
        return z;
    }

    private boolean isSuperPowerModeDisable() {
        return HwKeyguardUpdateMonitor.getInstance(this.mContext).isRestrictAsEncrypt();
    }
}
