package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationController.LocationSettingsChangeCallback;

public class LocationTile extends QSTile<BooleanState> {
    private final Callback mCallback = new Callback();
    private final LocationController mController;
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_gps_on2off, R.drawable.ic_gps_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_gps_off2on, R.drawable.ic_gps_tile_on);
    private final KeyguardMonitor mKeyguard;

    private final class Callback implements LocationSettingsChangeCallback, com.android.systemui.statusbar.policy.KeyguardMonitor.Callback {
        private Callback() {
        }

        public void onLocationSettingsChanged(boolean enabled) {
            LocationTile.this.refreshState();
        }

        public void onKeyguardChanged() {
            LocationTile.this.refreshState();
        }
    }

    public LocationTile(Host host) {
        super(host);
        this.mController = host.getLocationController();
        this.mKeyguard = host.getKeyguardMonitor();
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mController.addSettingsChangedCallback(this.mCallback);
            this.mKeyguard.addCallback(this.mCallback);
            return;
        }
        this.mController.removeSettingsChangedCallback(this.mCallback);
        this.mKeyguard.removeCallback(this.mCallback);
    }

    public Intent getLongClickIntent() {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            return null;
        }
        return new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
    }

    protected void handleClick() {
        boolean z = false;
        if (this.mKeyguard.isSecure() && this.mKeyguard.isShowing()) {
            this.mHost.startRunnableDismissingKeyguard(new Runnable() {
                public void run() {
                    boolean z;
                    boolean z2 = false;
                    boolean wasEnabled = Boolean.valueOf(((BooleanState) LocationTile.this.mState).value).booleanValue();
                    LocationTile.this.mHost.openPanels();
                    Context -get0 = LocationTile.this.mContext;
                    int metricsCategory = LocationTile.this.getMetricsCategory();
                    if (wasEnabled) {
                        z = false;
                    } else {
                        z = true;
                    }
                    MetricsLogger.action(-get0, metricsCategory, z);
                    LocationController -get1 = LocationTile.this.mController;
                    if (!wasEnabled) {
                        z2 = true;
                    }
                    -get1.setLocationEnabled(z2);
                }
            });
            return;
        }
        boolean wasEnabled = Boolean.valueOf(((BooleanState) this.mState).value).booleanValue();
        MetricsLogger.action(this.mContext, getMetricsCategory(), !wasEnabled);
        LocationController locationController = this.mController;
        if (!wasEnabled) {
            z = true;
        }
        locationController.setLocationEnabled(z);
        refreshState();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.gps_widget_name_new);
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean locationEnabled = this.mController.isLocationEnabled();
        state.value = locationEnabled;
        checkIfRestrictionEnforcedByAdminOnly(state, "no_share_location");
        if (locationEnabled) {
            state.label = this.mContext.getString(R.string.gps_widget_name_new);
            state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_location_on);
        } else {
            state.label = this.mContext.getString(R.string.gps_widget_name_new);
            state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_location_off);
        }
        state.icon = locationEnabled ? this.mEnable : this.mDisable;
        state.textChangedDelay = (long) (locationEnabled ? 267 : 83);
        state.labelTint = locationEnabled ? 1 : 0;
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 122;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_location_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_location_changed_off);
    }
}
