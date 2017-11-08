package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;

public class AirplaneModeTile extends QSTile<BooleanState> {
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_airplanemode_on2off, R.drawable.ic_airplanemode_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_airplanemode_off2on, R.drawable.ic_airplanemode_tile_on);
    private boolean mListening;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                AirplaneModeTile.this.refreshState();
            }
        }
    };
    private final GlobalSetting mSetting = new GlobalSetting(this.mContext, this.mHandler, "airplane_mode_on") {
        protected void handleValueChanged(int value) {
            AirplaneModeTile.this.handleRefreshState(Integer.valueOf(value));
        }
    };

    public AirplaneModeTile(Host host) {
        super(host);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void handleClick() {
        boolean z;
        boolean z2 = false;
        Context context = this.mContext;
        int metricsCategory = getMetricsCategory();
        if (((BooleanState) this.mState).value) {
            z = false;
        } else {
            z = true;
        }
        MetricsLogger.action(context, metricsCategory, z);
        if (!((BooleanState) this.mState).value) {
            z2 = true;
        }
        setEnabled(z2);
        refreshState();
    }

    private void setEnabled(boolean enabled) {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).setAirplaneMode(enabled);
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.AIRPLANE_MODE_SETTINGS").setPackage("com.android.settings");
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.airplane_mode);
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean airplaneMode;
        int i = 1;
        if ((arg instanceof Integer ? ((Integer) arg).intValue() : this.mSetting.getValue()) != 0) {
            airplaneMode = true;
        } else {
            airplaneMode = false;
        }
        state.value = airplaneMode;
        state.label = this.mContext.getString(R.string.airplane_mode);
        if (!airplaneMode) {
            i = 0;
        }
        state.labelTint = i;
        state.icon = airplaneMode ? this.mEnable : this.mDisable;
        state.textChangedDelay = (long) (airplaneMode ? 333 : 83);
        state.contentDescription = state.label;
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 112;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_airplane_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_airplane_changed_off);
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            if (listening) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.AIRPLANE_MODE");
                this.mContext.registerReceiver(this.mReceiver, filter);
            } else {
                this.mContext.unregisterReceiver(this.mReceiver);
            }
            this.mSetting.setListening(listening);
        }
    }
}
