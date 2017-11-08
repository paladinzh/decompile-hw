package com.android.settings.fuelgauge;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings.Global;
import android.util.Log;
import android.widget.Switch;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.conditional.BatterySaverCondition;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settings.notification.SettingPref;
import com.android.settingslib.Utils;

public class BatterySaverSettings extends SettingsPreferenceFragment {
    private static final boolean DEBUG = Log.isLoggable("BatterySaverSettings", 3);
    private Context mContext;
    private boolean mCreated;
    private final Handler mHandler = new Handler();
    private PowerManager mPowerManager;
    private final Receiver mReceiver = new Receiver();
    private final SettingsObserver mSettingsObserver = new SettingsObserver(this.mHandler);
    private final Runnable mStartMode = new Runnable() {
        public void run() {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    if (BatterySaverSettings.DEBUG) {
                        Log.d("BatterySaverSettings", "Starting low power mode from settings");
                    }
                    BatterySaverSettings.this.trySetPowerSaveMode(true);
                }
            });
        }
    };
    private Switch mSwitch;
    private SettingPref mTriggerPref;
    private final Runnable mUpdateSwitch = new Runnable() {
        public void run() {
            BatterySaverSettings.this.updateSwitch();
        }
    };
    private boolean mValidListener;

    private final class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        private Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (BatterySaverSettings.DEBUG) {
                Log.d("BatterySaverSettings", "Received " + intent.getAction());
            }
            BatterySaverSettings.this.mHandler.post(BatterySaverSettings.this.mUpdateSwitch);
        }

        public void setListening(boolean listening) {
            if (listening && !this.mRegistered) {
                BatterySaverSettings.this.mContext.registerReceiver(this, new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGING"));
                this.mRegistered = true;
            } else if (!listening && this.mRegistered) {
                BatterySaverSettings.this.mContext.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri LOW_POWER_MODE_TRIGGER_LEVEL_URI = Global.getUriFor("low_power_trigger_level");

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.LOW_POWER_MODE_TRIGGER_LEVEL_URI.equals(uri)) {
                BatterySaverSettings.this.mTriggerPref.update(BatterySaverSettings.this.mContext);
            }
        }

        public void setListening(boolean listening) {
            ContentResolver cr = BatterySaverSettings.this.getContentResolver();
            if (listening) {
                cr.registerContentObserver(this.LOW_POWER_MODE_TRIGGER_LEVEL_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }
    }

    protected int getMetricsCategory() {
        return 52;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!this.mCreated) {
            this.mCreated = true;
            addPreferencesFromResource(2131230744);
            this.mContext = getActivity();
            this.mTriggerPref = new SettingPref(1, "turn_on_automatically", "low_power_trigger_level", 0, getResources().getIntArray(2131361930)) {
                protected String getCaption(Resources res, int value) {
                    if (value <= 0 || value >= 100) {
                        return res.getString(2131626043);
                    }
                    return res.getString(2131628112, new Object[]{Utils.formatPercentage(value)});
                }
            };
            this.mTriggerPref.init(this);
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onResume() {
        super.onResume();
        this.mSettingsObserver.setListening(true);
        this.mReceiver.setListening(true);
        if (!this.mValidListener) {
            this.mValidListener = true;
        }
        updateSwitch();
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.setListening(false);
        this.mReceiver.setListening(false);
        if (this.mValidListener) {
            this.mValidListener = false;
        }
    }

    private void trySetPowerSaveMode(boolean mode) {
        if (!this.mPowerManager.setPowerSaveMode(mode)) {
            if (DEBUG) {
                Log.d("BatterySaverSettings", "Setting mode failed, fallback to current value");
            }
            this.mHandler.post(this.mUpdateSwitch);
        }
        Condition cdc = ConditionManager.get(getContext()).getCondition(BatterySaverCondition.class);
        if (cdc != null) {
            cdc.refreshState();
        }
    }

    private void updateSwitch() {
        if (this.mSwitch != null) {
            boolean mode = this.mPowerManager.isPowerSaveMode();
            if (DEBUG) {
                Log.d("BatterySaverSettings", "updateSwitch: isChecked=" + this.mSwitch.isChecked() + " mode=" + mode);
            }
            if (mode != this.mSwitch.isChecked()) {
                this.mSwitch.setChecked(mode);
            }
        }
    }
}
