package com.android.systemui.tuner;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.MenuItem;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import fyusion.vislib.BuildConfig;

public class DemoModeFragment extends PreferenceFragment implements OnPreferenceChangeListener {
    private static final String[] STATUS_ICONS = new String[]{"volume", "bluetooth", "location", "alarm", "zen", "sync", "tty", "eri", "mute", "speakerphone", "managed_profile"};
    private final ContentObserver mDemoModeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange) {
            DemoModeFragment.this.updateDemoModeEnabled();
            DemoModeFragment.this.updateDemoModeOn();
        }
    };
    @SuppressLint({"HwViewID"})
    private SwitchPreference mEnabledSwitch;
    @SuppressLint({"HwViewID"})
    private SwitchPreference mOnSwitch;

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        this.mEnabledSwitch = new SwitchPreference(context);
        this.mEnabledSwitch.setTitle((int) R.string.enable_demo_mode);
        this.mEnabledSwitch.setOnPreferenceChangeListener(this);
        this.mOnSwitch = new SwitchPreference(context);
        this.mOnSwitch.setTitle((int) R.string.show_demo_mode);
        this.mOnSwitch.setEnabled(false);
        this.mOnSwitch.setOnPreferenceChangeListener(this);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        screen.addPreference(this.mEnabledSwitch);
        screen.addPreference(this.mOnSwitch);
        setPreferenceScreen(screen);
        updateDemoModeEnabled();
        updateDemoModeOn();
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.registerContentObserver(Global.getUriFor("sysui_demo_allowed"), false, this.mDemoModeObserver);
        contentResolver.registerContentObserver(Global.getUriFor("sysui_tuner_demo_on"), false, this.mDemoModeObserver);
        setHasOptionsMenu(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                getFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 229, true);
    }

    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 229, false);
    }

    public void onDestroy() {
        getContext().getContentResolver().unregisterContentObserver(this.mDemoModeObserver);
        super.onDestroy();
    }

    private void updateDemoModeEnabled() {
        boolean enabled = Global.getInt(getContext().getContentResolver(), "sysui_demo_allowed", 0) != 0;
        this.mEnabledSwitch.setChecked(enabled);
        this.mOnSwitch.setEnabled(enabled);
    }

    private void updateDemoModeOn() {
        this.mOnSwitch.setChecked(Global.getInt(getContext().getContentResolver(), "sysui_tuner_demo_on", 0) != 0);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        boolean enabled = newValue == Boolean.TRUE;
        if (preference == this.mEnabledSwitch) {
            if (!enabled) {
                this.mOnSwitch.setChecked(false);
                stopDemoMode();
            }
            MetricsLogger.action(getContext(), 235, enabled);
            String str = "sysui_demo_allowed";
            if (enabled) {
                i = 1;
            }
            setGlobal(str, i);
        } else if (preference != this.mOnSwitch) {
            return false;
        } else {
            MetricsLogger.action(getContext(), 236, enabled);
            if (enabled) {
                startDemoMode();
            } else {
                stopDemoMode();
            }
        }
        return true;
    }

    private void startDemoMode() {
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "enter");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "clock");
        intent.putExtra("hhmm", "0700");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "network");
        intent.putExtra("wifi", "show");
        intent.putExtra("mobile", "show");
        intent.putExtra("sims", "1");
        intent.putExtra("nosim", "false");
        intent.putExtra("level", "4");
        intent.putExtra("datatypel", BuildConfig.FLAVOR);
        getContext().sendBroadcast(intent);
        intent.putExtra("fully", "true");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "battery");
        intent.putExtra("level", "100");
        intent.putExtra("plugged", "false");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "status");
        for (String icon : STATUS_ICONS) {
            intent.putExtra(icon, "hide");
        }
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "notifications");
        intent.putExtra("visible", "false");
        getContext().sendBroadcast(intent);
        setGlobal("sysui_tuner_demo_on", 1);
    }

    private void stopDemoMode() {
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "exit");
        getContext().sendBroadcast(intent);
        setGlobal("sysui_tuner_demo_on", 0);
    }

    private void setGlobal(String key, int value) {
        Global.putInt(getContext().getContentResolver(), key, value);
    }
}
