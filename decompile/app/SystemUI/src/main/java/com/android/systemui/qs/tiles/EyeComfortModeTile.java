package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class EyeComfortModeTile extends QSTile<BooleanState> {
    private static final int SMART_DISPLAY_ENABLE = SystemProperties.getInt("ro.config.hw_eyes_protection", 1);
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_eyecomfort_on2off, R.drawable.ic_eyecomfort_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_eyecomfort_off2on, R.drawable.ic_eyecomfort_tile_on);
    OnChangeListener mStateChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            EyeComfortModeTile.this.refreshState();
        }
    };

    public EyeComfortModeTile(Host host) {
        super(host);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    protected void handleClick() {
        boolean newState = !((BooleanState) this.mState).value;
        setEyeComfortModeStatus(this.mContext, newState ? 1 : 0);
        refreshState(Boolean.valueOf(newState));
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean isEyeComfortModeOn = arg != null ? ((Boolean) arg).booleanValue() : isComfortModeOn();
        state.label = this.mContext.getString(R.string.eye_comfort_widget_name);
        state.labelTint = isEyeComfortModeOn ? 1 : 0;
        state.value = isEyeComfortModeOn;
        state.icon = isEyeComfortModeOn ? this.mEnable : this.mDisable;
        state.textChangedDelay = 83;
        state.contentDescription = this.mContext.getString(R.string.eye_comfort_widget_name);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        Intent intent = new Intent("com.android.settings.EyeComfortSettings");
        intent.setPackage("com.android.settings");
        return intent;
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.eye_comfort_widget_name);
    }

    public void setListening(boolean listening) {
        if (listening) {
            SystemUIObserver.getObserver(17).addOnChangeListener(this.mStateChangeListener);
        } else {
            SystemUIObserver.getObserver(17).removeOnChangeListener(this.mStateChangeListener);
        }
    }

    public boolean isAvailable() {
        return isComfortFeatureSupported();
    }

    public static boolean isComfortFeatureSupported() {
        boolean z = false;
        try {
            Class classz = Class.forName("com.huawei.android.hwsmartdisplay.HwSmartDisplay");
            Object instance = classz.newInstance();
            z = ((Boolean) classz.getDeclaredMethod("isFeatureSupported", new Class[]{Integer.TYPE}).invoke(instance, new Object[]{Integer.valueOf(1)})).booleanValue();
            HwLog.i("EyeComfortModeTile", " isSupportComfort: " + z);
        } catch (RuntimeException ex) {
            Log.e("EyeComfortModeTile", ": reflection exception is " + ex.getMessage());
        } catch (Exception ex2) {
            Log.e("EyeComfortModeTile", ": Exception happend when check if eye protecton feature supported. Message is: " + ex2.getMessage());
        }
        if (z) {
            return z;
        }
        boolean z2;
        if (SMART_DISPLAY_ENABLE != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        return z2;
    }

    private boolean isComfortModeOn() {
        return ((Boolean) SystemUIObserver.get(17)).booleanValue();
    }

    private void setEyeComfortModeStatus(Context context, int value) {
        System.putIntForUser(context.getContentResolver(), "eyes_protection_mode", value, UserSwitchUtils.getCurrentUser());
    }

    protected String composeChangeAnnouncement() {
        if (this.mState.value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_eye_comfort_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_eye_comfort_changed_off);
    }
}
