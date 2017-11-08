package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;
import com.android.systemui.R;
import com.android.systemui.qs.QSIconView;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.qs.QSTile.SignalState;
import com.android.systemui.qs.SignalTileView;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.SignalCallbackAdapter;

public class CellularTile extends QSTile<SignalState> {
    static final Intent CELLULAR_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
    private final NetworkController mController;
    private final DataUsageController mDataController;
    private final CellularDetailAdapter mDetailAdapter;
    private final CellSignalCallback mSignalCallback = new CellSignalCallback();

    private static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean airplaneModeEnabled;
        String dataContentDescription;
        int dataTypeIconId;
        boolean enabled;
        String enabledDesc;
        boolean isDataTypeIconWide;
        int mobileSignalIconId;
        boolean noSim;
        String signalContentDescription;
        boolean wifiEnabled;

        private CallbackInfo() {
        }
    }

    private final class CellSignalCallback extends SignalCallbackAdapter {
        private final CallbackInfo mInfo;

        private CellSignalCallback() {
            this.mInfo = new CallbackInfo();
        }

        public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon, boolean activityIn, boolean activityOut, String description) {
            this.mInfo.wifiEnabled = enabled;
            CellularTile.this.refreshState(this.mInfo);
        }

        public void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType, int qsType, boolean activityIn, boolean activityOut, String typeContentDescription, String description, boolean isWide, int subId) {
            if (qsIcon != null) {
                this.mInfo.enabled = qsIcon.visible;
                this.mInfo.mobileSignalIconId = qsIcon.icon;
                this.mInfo.signalContentDescription = qsIcon.contentDescription;
                this.mInfo.dataTypeIconId = qsType;
                this.mInfo.dataContentDescription = typeContentDescription;
                this.mInfo.activityIn = activityIn;
                this.mInfo.activityOut = activityOut;
                this.mInfo.enabledDesc = description;
                CallbackInfo callbackInfo = this.mInfo;
                if (qsType == 0) {
                    isWide = false;
                }
                callbackInfo.isDataTypeIconWide = isWide;
                CellularTile.this.refreshState(this.mInfo);
            }
        }

        public void setNoSims(boolean show) {
            this.mInfo.noSim = show;
            if (this.mInfo.noSim) {
                this.mInfo.mobileSignalIconId = 0;
                this.mInfo.dataTypeIconId = 0;
                this.mInfo.enabled = true;
                this.mInfo.enabledDesc = CellularTile.this.mContext.getString(R.string.keyguard_missing_sim_message_short);
                this.mInfo.signalContentDescription = this.mInfo.enabledDesc;
            }
            CellularTile.this.refreshState(this.mInfo);
        }

        public void setIsAirplaneMode(IconState icon) {
            this.mInfo.airplaneModeEnabled = icon.visible;
            CellularTile.this.refreshState(this.mInfo);
        }

        public void setMobileDataEnabled(boolean enabled) {
            CellularTile.this.mDetailAdapter.setMobileDataEnabled(enabled);
        }
    }

    private final class CellularDetailAdapter implements DetailAdapter {
        private CellularDetailAdapter() {
        }

        public CharSequence getTitle() {
            return CellularTile.this.mContext.getString(R.string.quick_settings_cellular_detail_title);
        }

        public Boolean getToggleState() {
            if (CellularTile.this.mDataController.isMobileDataSupported()) {
                return Boolean.valueOf(CellularTile.this.mDataController.isMobileDataEnabled());
            }
            return null;
        }

        public Intent getSettingsIntent() {
            return CellularTile.CELLULAR_SETTINGS;
        }

        public void setToggleState(boolean state) {
            MetricsLogger.action(CellularTile.this.mContext, 155, state);
            CellularTile.this.mDataController.setMobileDataEnabled(state);
        }

        public int getMetricsCategory() {
            return 117;
        }

        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            DataUsageDetailView v;
            if (convertView != null) {
                v = convertView;
            } else {
                v = LayoutInflater.from(CellularTile.this.mContext).inflate(R.layout.data_usage, parent, false);
            }
            v = v;
            DataUsageInfo info = CellularTile.this.mDataController.getDataUsageInfo();
            if (info == null) {
                return v;
            }
            v.bind(info);
            return v;
        }

        public void setMobileDataEnabled(boolean enabled) {
            CellularTile.this.fireToggleStateChanged(enabled);
        }
    }

    public CellularTile(Host host) {
        super(host);
        this.mController = host.getNetworkController();
        this.mDataController = this.mController.getMobileDataController();
        this.mDetailAdapter = new CellularDetailAdapter();
    }

    public SignalState newTileState() {
        return new SignalState();
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mController.addSignalCallback(this.mSignalCallback);
        } else {
            this.mController.removeSignalCallback(this.mSignalCallback);
        }
    }

    public QSIconView createTileView(Context context) {
        return new SignalTileView(context);
    }

    public Intent getLongClickIntent() {
        return CELLULAR_SETTINGS;
    }

    protected void handleClick() {
        MetricsLogger.action(this.mContext, getMetricsCategory());
        if (this.mDataController.isMobileDataSupported()) {
            showDetail(true);
        } else {
            this.mHost.startActivityDismissingKeyguard(CELLULAR_SETTINGS);
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_cellular_detail_title);
    }

    protected void handleUpdateState(SignalState state, Object arg) {
        int iconId;
        boolean z;
        int i;
        boolean z2;
        CharSequence removeTrailingPeriod;
        String signalContentDesc;
        boolean z3 = false;
        CallbackInfo cb = (CallbackInfo) arg;
        if (cb == null) {
            cb = this.mSignalCallback.mInfo;
        }
        Resources r = this.mContext.getResources();
        if (cb.noSim) {
            iconId = R.drawable.ic_qs_no_sim;
        } else if (!cb.enabled || cb.airplaneModeEnabled) {
            iconId = R.drawable.ic_qs_signal_disabled;
        } else if (cb.mobileSignalIconId > 0) {
            iconId = cb.mobileSignalIconId;
        } else {
            iconId = R.drawable.ic_qs_signal_no_signal;
        }
        state.icon = ResourceIcon.get(iconId);
        state.isOverlayIconWide = cb.isDataTypeIconWide;
        if (cb.noSim) {
            z = false;
        } else {
            z = true;
        }
        state.autoMirrorDrawable = z;
        if (!cb.enabled || cb.dataTypeIconId <= 0) {
            i = 0;
        } else {
            i = cb.dataTypeIconId;
        }
        state.overlayIconId = i;
        if (iconId != R.drawable.ic_qs_no_sim) {
            z2 = true;
        } else {
            z2 = false;
        }
        state.filter = z2;
        if (cb.enabled) {
            z2 = cb.activityIn;
        } else {
            z2 = false;
        }
        state.activityIn = z2;
        if (cb.enabled) {
            z2 = cb.activityOut;
        } else {
            z2 = false;
        }
        state.activityOut = z2;
        if (cb.enabled) {
            removeTrailingPeriod = removeTrailingPeriod(cb.enabledDesc);
        } else {
            removeTrailingPeriod = r.getString(R.string.quick_settings_rssi_emergency_only);
        }
        state.label = removeTrailingPeriod;
        if (!cb.enabled || cb.mobileSignalIconId <= 0) {
            signalContentDesc = r.getString(R.string.accessibility_no_signal);
        } else {
            signalContentDesc = cb.signalContentDescription;
        }
        if (cb.noSim) {
            state.contentDescription = state.label;
        } else {
            String enabledDesc;
            if (cb.enabled) {
                enabledDesc = r.getString(R.string.accessibility_cell_data_on);
            } else {
                enabledDesc = r.getString(R.string.accessibility_cell_data_off);
            }
            state.contentDescription = r.getString(R.string.accessibility_quick_settings_mobile, new Object[]{enabledDesc, signalContentDesc, state.label});
            state.minimalContentDescription = r.getString(R.string.accessibility_quick_settings_mobile, new Object[]{r.getString(R.string.accessibility_cell_data), signalContentDesc, state.label});
        }
        state.contentDescription += "," + r.getString(R.string.accessibility_quick_settings_open_settings, new Object[]{getTileLabel()});
        String name = Button.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
        if (this.mDataController.isMobileDataSupported()) {
            z3 = this.mDataController.isMobileDataEnabled();
        }
        state.value = z3;
    }

    public int getMetricsCategory() {
        return 115;
    }

    public boolean isAvailable() {
        return false;
    }

    public static String removeTrailingPeriod(String string) {
        if (string == null) {
            return null;
        }
        int length = string.length();
        if (string.endsWith(".")) {
            return string.substring(0, length - 1);
        }
        return string;
    }
}
