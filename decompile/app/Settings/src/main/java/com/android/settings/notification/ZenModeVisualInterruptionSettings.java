package com.android.settings.notification;

import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;

public class ZenModeVisualInterruptionSettings extends ZenModeSettingsBase {
    private boolean mDisableListeners;
    private Policy mPolicy;
    private SwitchPreference mScreenOff;
    private SwitchPreference mScreenOn;

    private int getNewSuppressedEffects(boolean r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.settings.notification.ZenModeVisualInterruptionSettings.getNewSuppressedEffects(boolean, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.notification.ZenModeVisualInterruptionSettings.getNewSuppressedEffects(boolean, int):int");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230956);
        PreferenceScreen root = getPreferenceScreen();
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
        this.mScreenOff = (SwitchPreference) root.findPreference("screenOff");
        if (!getResources().getBoolean(17956932)) {
            this.mScreenOff.setSummary(2131626843);
        }
        this.mScreenOff.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModeVisualInterruptionSettings.this.mDisableListeners) {
                    return true;
                }
                boolean val = ((Boolean) newValue).booleanValue();
                MetricsLogger.action(ZenModeVisualInterruptionSettings.this.mContext, 263, val);
                if (ZenModeVisualInterruptionSettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange suppressWhenScreenOff=" + val);
                }
                ZenModeVisualInterruptionSettings.this.savePolicy(ZenModeVisualInterruptionSettings.this.getNewSuppressedEffects(val, 1));
                return true;
            }
        });
        this.mScreenOn = (SwitchPreference) root.findPreference("screenOn");
        this.mScreenOn.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModeVisualInterruptionSettings.this.mDisableListeners) {
                    return true;
                }
                boolean val = ((Boolean) newValue).booleanValue();
                MetricsLogger.action(ZenModeVisualInterruptionSettings.this.mContext, 269, val);
                if (ZenModeVisualInterruptionSettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange suppressWhenScreenOn=" + val);
                }
                ZenModeVisualInterruptionSettings.this.savePolicy(ZenModeVisualInterruptionSettings.this.getNewSuppressedEffects(val, 2));
                return true;
            }
        });
    }

    protected int getMetricsCategory() {
        return 262;
    }

    protected void onZenModeChanged() {
    }

    protected void onZenModeConfigChanged() {
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
        updateControls();
    }

    private void updateControls() {
        this.mDisableListeners = true;
        this.mScreenOff.setChecked(isEffectSuppressed(1));
        this.mScreenOn.setChecked(isEffectSuppressed(2));
        this.mDisableListeners = false;
    }

    private boolean isEffectSuppressed(int effect) {
        return (this.mPolicy.suppressedVisualEffects & effect) != 0;
    }

    private void savePolicy(int suppressedVisualEffects) {
        this.mPolicy = new Policy(this.mPolicy.priorityCategories, this.mPolicy.priorityCallSenders, this.mPolicy.priorityMessageSenders, suppressedVisualEffects);
        NotificationManager.from(this.mContext).setNotificationPolicy(this.mPolicy);
    }
}
