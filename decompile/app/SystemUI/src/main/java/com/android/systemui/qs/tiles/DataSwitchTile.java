package com.android.systemui.qs.tiles;

import android.app.StatusBarManager;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManager;
import android.widget.Switch;
import android.widget.Toast;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.statusbar.policy.NetWorkUtils;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.android.app.admin.DeviceRestrictionManager;
import com.huawei.cust.HwCustUtils;

public class DataSwitchTile extends QSTile<BooleanState> {
    private static final boolean IS_CHINA_TELECOM;
    private final NetworkController mController;
    private final DataUsageController mDataController;
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_dataswitch_on2off, R.drawable.ic_dataswitch_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_dataswitch_off2on, R.drawable.ic_dataswitch_tile_on);
    private HwCustDataSwitchTile mHwCustTile = ((HwCustDataSwitchTile) HwCustUtils.createObj(HwCustDataSwitchTile.class, new Object[]{this}));
    OnChangeListener mStateChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            DataSwitchTile.this.onObserverChanged();
        }
    };

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_optb", "0").equals("156")) {
            equals = SystemProperties.get("ro.config.hw_opta", "0").equals("92");
        } else {
            equals = false;
        }
        IS_CHINA_TELECOM = equals;
    }

    public DataSwitchTile(Host host) {
        super(host);
        this.mController = host.getNetworkController();
        this.mDataController = this.mController.getMobileDataController();
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    protected void handleClick() {
        if (!isDataSwitchDisable()) {
            final boolean newState = !((BooleanState) this.mState).value;
            if (!this.mProcessingState) {
                this.mProcessingState = true;
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    private boolean isDataSwitchDisabledByMDM = false;

                    public boolean runInThread() {
                        this.isDataSwitchDisabledByMDM = DataSwitchTile.this.isDataConnectivityDisabled();
                        if (this.isDataSwitchDisabledByMDM) {
                            HwLog.w("DataSwitchTile", "Data connectivity is disabled by mdm apk, user cannot open it");
                            return true;
                        }
                        if (newState && DataSwitchTile.this.shouldShowThePdpWarning()) {
                            ((StatusBarManager) DataSwitchTile.this.mContext.getSystemService("statusbar")).collapsePanels();
                        }
                        DataSwitchTile.this.refreshState();
                        DataSwitchTile.this.mDataController.setMobileDataEnabled(newState);
                        if (DataSwitchTile.this.mHwCustTile != null) {
                            DataSwitchTile.this.mHwCustTile.saveAfterChangeState(DataSwitchTile.this.mContext, newState ? 1 : 0);
                        }
                        return true;
                    }

                    public void runInUI() {
                        if (this.isDataSwitchDisabledByMDM && newState) {
                            Toast.makeText(DataSwitchTile.this.mContext, R.string.data_connectivity_not_enabled_user_restriction, 0).show();
                        }
                    }
                });
                refreshState(Boolean.valueOf(newState));
            }
        }
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean isDataEnable = arg != null ? ((Boolean) arg).booleanValue() : isMobileDataEnable();
        boolean isStateChanged = false;
        if (isDataEnable != this.mLastState) {
            isStateChanged = true;
        }
        this.mLastState = isDataEnable;
        if (isDataSwitchDisable()) {
            state.icon = ResourceIcon.get(R.drawable.ic_dataswitch_tile_disable);
            state.labelTint = 2;
        } else if (this.mProcessingState) {
            state.icon = ResourceIcon.get(R.drawable.ic_dataswitch_tile_process);
            state.labelTint = 3;
        } else if (isDataEnable) {
            state.icon = isStateChanged ? this.mEnable : ResourceIcon.get(R.drawable.ic_dataswitch_tile_on);
            state.labelTint = 1;
        } else {
            state.icon = isStateChanged ? this.mDisable : ResourceIcon.get(R.drawable.ic_dataswitch_tile_off);
            state.labelTint = 0;
        }
        state.label = this.mContext.getString(R.string.data_widget_name);
        state.value = isDataEnable;
        int i = isStateChanged ? isDataEnable ? 450 : 83 : 0;
        state.textChangedDelay = (long) i;
        state.contentDescription = this.mContext.getString(R.string.data_widget_name);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        if (isDataSwitchDisable() || SystemProperties.getBoolean("sys.super_power_save", false)) {
            return null;
        }
        if (IS_CHINA_TELECOM) {
            return new Intent("android.settings.SETTINGS").setPackage("com.android.settings").addFlags(32768);
        }
        if (!SystemUiUtil.isSupportVSim() || NetWorkUtils.getVSimSubId() == -1) {
            return new Intent("android.settings.DATA_ROAMING_SETTINGS").setPackage("com.android.phone");
        }
        return null;
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.data_widget_name);
    }

    public void setListening(boolean listening) {
        if (listening) {
            SystemUIObserver.getObserver(2).addOnChangeListener(this.mStateChangeListener);
            SystemUIObserver.getObserver(19).addOnChangeListener(this.mStateChangeListener);
            SystemUIObserver.getObserver(23).addOnChangeListener(this.mStateChangeListener);
            return;
        }
        SystemUIObserver.getObserver(2).removeOnChangeListener(this.mStateChangeListener);
        SystemUIObserver.getObserver(19).removeOnChangeListener(this.mStateChangeListener);
        SystemUIObserver.getObserver(23).removeOnChangeListener(this.mStateChangeListener);
        this.mProcessingState = false;
    }

    public boolean isAvailable() {
        return !SystemUiUtil.isWifiOnly(this.mContext);
    }

    private boolean isMobileDataEnable() {
        boolean dateEnable = false;
        Object obj = SystemUIObserver.get(23);
        if (obj != null && (obj instanceof Boolean)) {
            dateEnable = ((Boolean) obj).booleanValue();
        }
        boolean cotrollerEnable = this.mDataController.isMobileDataEnabled();
        HwLog.i("DataSwitchTile", "isMobileDataEnable::dateEnable=" + dateEnable + ", cotrollerEnable=" + cotrollerEnable);
        return !dateEnable ? cotrollerEnable : true;
    }

    private boolean isDataSwitchDisable() {
        if (((Boolean) SystemUIObserver.get(2)).booleanValue() || !SystemUiUtil.isSimCardReady(this.mContext)) {
            return true;
        }
        return isVSimEnable();
    }

    private boolean isDataConnectivityDisabled() {
        try {
            return new DeviceRestrictionManager().isDataConnectivityDisabled(null);
        } catch (RuntimeException e) {
            HwLog.e("DataSwitchTile", "RuntimeException happened from DeviceRestrictionManager.");
            return false;
        } catch (Exception e2) {
            HwLog.e("DataSwitchTile", "can not get data connectivity status from DeviceRestrictionManager.");
            return false;
        }
    }

    private boolean shouldShowThePdpWarning() {
        boolean z = true;
        if (SystemUiUtil.isMulityCard(this.mContext)) {
            return shouldShowThePdpWarningMsim();
        }
        String enable_Not_Remind_Function = System.getString(this.mContext.getContentResolver(), "enable_not_remind_function");
        boolean remindDataServiceToPdp = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        int pdpWarningValue = System.getInt(this.mContext.getContentResolver(), "whether_show_pdp_warning", 1);
        if (!"true".equals(enable_Not_Remind_Function)) {
            return remindDataServiceToPdp;
        }
        if (!(remindDataServiceToPdp && pdpWarningValue == 1)) {
            z = false;
        }
        return z;
    }

    private boolean shouldShowThePdpWarningMsim() {
        boolean z = true;
        String enableNotRemindFunction = System.getString(this.mContext.getContentResolver(), "enable_not_remind_function");
        boolean remindDataAllow = false;
        int lDataVal = HwTelephonyManager.getDefault().getPreferredDataSubscription();
        if (1 == lDataVal) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (lDataVal == 0) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        }
        int pdpWarningValue = System.getInt(this.mContext.getContentResolver(), "whether_show_pdp_warning", 1);
        if (!"true".equals(enableNotRemindFunction)) {
            return remindDataAllow;
        }
        if (!(remindDataAllow && pdpWarningValue == 1)) {
            z = false;
        }
        return z;
    }

    private boolean isVSimEnable() {
        return SystemUiUtil.isSupportVSim() && -1 != NetWorkUtils.getVSimSubId();
    }

    protected String composeChangeAnnouncement() {
        if (this.mState.value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_data_switch_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_data_switch_changed_off);
    }
}
