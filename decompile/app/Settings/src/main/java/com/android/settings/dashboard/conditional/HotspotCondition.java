package com.android.settings.dashboard.conditional;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import com.android.settings.ItemUseStat;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.TetherUtil;

public class HotspotCondition extends Condition {
    private final WifiManager mWifiManager = ((WifiManager) this.mManager.getContext().getSystemService(WifiManager.class));

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                Condition cdc = ConditionManager.get(context).getCondition(HotspotCondition.class);
                if (cdc != null) {
                    cdc.refreshState();
                }
            }
        }
    }

    public HotspotCondition(ConditionManager manager) {
        super(manager);
        this.mManager.getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(this.mManager.getContext(), Receiver.class), 1, 1);
    }

    public void refreshState() {
        setActive(this.mWifiManager.isWifiApEnabled());
    }

    protected Class<?> getReceiverClass() {
        return Receiver.class;
    }

    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), 2130838422);
    }

    private String getSsid() {
        WifiConfiguration wifiConfig = this.mWifiManager.getWifiApConfiguration();
        if (wifiConfig == null) {
            return this.mManager.getContext().getString(17040332);
        }
        return wifiConfig.SSID;
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(2131627116);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(2131627117, new Object[]{getSsid()});
    }

    public CharSequence[] getActions() {
        if (RestrictedLockUtils.hasBaseUserRestriction(this.mManager.getContext(), "no_config_tethering", UserHandle.myUserId())) {
            return new CharSequence[0];
        }
        return new CharSequence[]{this.mManager.getContext().getString(2131627112)};
    }

    public void onPrimaryClick() {
        try {
            Intent hotpotIntent = new Intent();
            hotpotIntent.setClassName("com.android.settings", "com.android.settings.Settings$WifiApSettingsActivity");
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "start condition", ItemUseStat.getShortName(hotpotIntent.getComponent().getClassName()));
            this.mManager.getContext().startActivity(hotpotIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onActionClick(int index) {
        if (index == 0) {
            Context context = this.mManager.getContext();
            EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_config_tethering", UserHandle.myUserId());
            if (admin != null) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, admin);
                return;
            }
            TetherUtil.setWifiTethering(false, context);
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "condition onActionClick", "Settings$WifiApSettingsActivity");
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    public int getMetricsConstant() {
        return 382;
    }
}
