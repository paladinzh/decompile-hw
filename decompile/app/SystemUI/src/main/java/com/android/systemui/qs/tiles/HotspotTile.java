package com.android.systemui.qs.tiles;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkPolicyManager;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.AirplaneBooleanState;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Icon;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;

public class HotspotTile extends QSTile<AirplaneBooleanState> {
    private static final boolean IS_TETHER_DENIED = SystemProperties.getBoolean("ro.tether.denied", false);
    private final GlobalSetting mAirplaneMode;
    private final Callback mCallback = new Callback();
    private final HotspotController mController;
    private boolean mDataSaverEnabled = false;
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_hotspot_on2off, R.drawable.ic_hotspot_tile_off);
    private final Icon mDisableNoAnimation = ResourceIcon.get(R.drawable.ic_hotspot_enable);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_hotspot_off2on, R.drawable.ic_hotspot_tile_on);
    private boolean mListening;
    private final NetworkPolicyManager mPolicyManager = NetworkPolicyManager.from(this.mContext);
    private final Icon mUnavailable = ResourceIcon.get(R.drawable.ic_hotspot_tile_disable);

    private final class Callback implements com.android.systemui.statusbar.policy.HotspotController.Callback {
        private Callback() {
        }

        public void onHotspotChanged(boolean enabled) {
            HotspotTile.this.mProcessingState = false;
            HotspotTile.this.refreshState(Boolean.valueOf(enabled));
        }
    }

    public HotspotTile(Host host) {
        super(host);
        this.mController = host.getHotspotController();
        this.mAirplaneMode = new GlobalSetting(this.mContext, this.mHandler, "airplane_mode_on") {
            protected void handleValueChanged(int value) {
                HotspotTile.this.refreshState();
            }
        };
    }

    public boolean isAvailable() {
        boolean z = false;
        if (IS_TETHER_DENIED || UserSwitchUtils.getCurrentUser() != 0) {
            return false;
        }
        if (!SystemUiUtil.isWifiOnly(this.mContext)) {
            z = true;
        }
        return z;
    }

    protected void handleDestroy() {
        super.handleDestroy();
    }

    public AirplaneBooleanState newTileState() {
        return new AirplaneBooleanState();
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            if (listening) {
                this.mController.addCallback(this.mCallback);
                new IntentFilter().addAction("android.intent.action.AIRPLANE_MODE");
                refreshState();
            } else {
                this.mController.removeCallback(this.mCallback);
                this.mProcessingState = false;
            }
            this.mAirplaneMode.setListening(listening);
        }
    }

    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.settings.WIFI_AP_SETTINGS");
        intent.setPackage("com.android.settings");
        return intent;
    }

    protected void handleClick() {
        boolean isEnabled = Boolean.valueOf(((AirplaneBooleanState) this.mState).value).booleanValue();
        if (!isEnabled && this.mAirplaneMode.getValue() != 0) {
            return;
        }
        if (this.mDataSaverEnabled) {
            if (!isEnabled) {
                ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
                this.mUiHandler.post(new Runnable() {
                    public void run() {
                        HotspotTile.this.showDialog();
                    }
                });
            }
            Log.i(this.TAG, "handleClick: isEnabled" + isEnabled);
        } else if (!this.mProcessingState) {
            if (this.mHwCustQSTile == null || !this.mHwCustQSTile.hasCustomForClick()) {
                if (!(isEnabled || this.mHwCustQSTile == null)) {
                    this.mHwCustQSTile.showNotificationForVowifi(this.mContext);
                }
                setNewState(isEnabled);
            } else {
                this.mHwCustQSTile.requestStateClick(this.mContext, isEnabled);
            }
        }
    }

    public void setNewState(boolean isEnabled) {
        boolean z;
        boolean newState;
        this.mProcessingState = true;
        Context context = this.mContext;
        int metricsCategory = getMetricsCategory();
        if (isEnabled) {
            z = false;
        } else {
            z = true;
        }
        MetricsLogger.action(context, metricsCategory, z);
        HotspotController hotspotController = this.mController;
        if (isEnabled) {
            z = false;
        } else {
            z = true;
        }
        hotspotController.setHotspotEnabled(z);
        if (((AirplaneBooleanState) this.mState).value) {
            newState = false;
        } else {
            newState = true;
        }
        refreshState(Boolean.valueOf(newState));
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_hotspot_label);
    }

    protected void handleUpdateState(AirplaneBooleanState state, Object arg) {
        boolean z = true;
        state.label = this.mContext.getString(R.string.quick_settings_hotspot_label);
        checkIfRestrictionEnforcedByAdminOnly(state, "no_config_tethering");
        if (arg instanceof Boolean) {
            state.value = ((Boolean) arg).booleanValue();
        } else {
            state.value = this.mController.isHotspotEnabled();
        }
        boolean isStateChanged = false;
        if (state.value != this.mLastState) {
            isStateChanged = true;
        }
        this.mLastState = state.value;
        int i = isStateChanged ? state.value ? 167 : 83 : 0;
        state.textChangedDelay = (long) i;
        if (state.value) {
            i = 1;
        } else {
            i = 0;
        }
        state.labelTint = i;
        if (this.mProcessingState) {
            state.icon = ResourceIcon.get(R.drawable.ic_hotspot_tile_process);
        } else if (isStateChanged) {
            state.icon = state.value ? this.mEnable : this.mDisable;
        } else {
            Icon drawableIcon;
            if (state.value) {
                drawableIcon = new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_hotspot_tile_on));
            } else {
                drawableIcon = new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_hotspot_tile_off));
            }
            state.icon = drawableIcon;
        }
        i = this.mProcessingState ? 3 : state.value ? 1 : 0;
        state.labelTint = i;
        this.mDataSaverEnabled = this.mPolicyManager.getRestrictBackground();
        if (this.mDataSaverEnabled) {
            state.icon = ResourceIcon.get(R.drawable.ic_hotspot_tile_off);
            state.labelTint = 0;
        }
        if (this.mAirplaneMode.getValue() == 0) {
            z = false;
        }
        state.isAirplaneMode = z;
        boolean z2 = this.mDataSaverEnabled ? state.value : false;
        if (state.isAirplaneMode || z2) {
            state.labelTint = 2;
            state.icon = this.mUnavailable;
        }
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
        state.contentDescription = state.label;
    }

    public int getMetricsCategory() {
        return 120;
    }

    protected String composeChangeAnnouncement() {
        if (((AirplaneBooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_hotspot_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_hotspot_changed_off);
    }

    private void showDialog() {
        SystemUIDialog dialog = new SystemUIDialog(this.mContext);
        dialog.setTitle(R.string.turn_off_data_saver);
        dialog.setMessage(R.string.turn_off_data_saver_message);
        dialog.setPositiveButton(R.string.confirm_turning_off_data_saver, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    public boolean runInThread() {
                        HotspotTile.this.mPolicyManager.setRestrictBackground(false);
                        HotspotTile.this.mController.setHotspotEnabled(true);
                        return true;
                    }

                    public void runInUI() {
                        HotspotTile.this.refreshState(Boolean.valueOf(true));
                    }
                });
            }
        });
        dialog.setNegativeButton(R.string.cancel_turning_off_data_saver, null);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}
