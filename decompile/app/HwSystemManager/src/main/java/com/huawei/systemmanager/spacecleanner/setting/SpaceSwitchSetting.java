package com.huawei.systemmanager.spacecleanner.setting;

import android.content.SharedPreferences;
import com.huawei.systemmanager.comm.misc.GlobalContext;

public abstract class SpaceSwitchSetting implements ISetting<Boolean> {
    protected static final String INTENT_PKG_SYSTEM_MANAGER = "com.huawei.systemmanager";
    public static final String PREFERENCE_NAME = "space_prefence";
    static SharedPreferences mPreferences;
    protected String mKey;

    public abstract void doSwitchOff();

    public abstract void doSwitchOn();

    public SpaceSwitchSetting(String key) {
        this.mKey = key;
        createPreference();
    }

    public String getKey() {
        return this.mKey;
    }

    public Boolean getValue() {
        Boolean valueOf;
        String key = this.mKey;
        synchronized (SpaceSwitchSetting.class) {
            valueOf = Boolean.valueOf(mPreferences.getBoolean(key, false));
        }
        return valueOf;
    }

    public void setValue(Boolean value) {
        String key = this.mKey;
        synchronized (SpaceSwitchSetting.class) {
            mPreferences.edit().putBoolean(key, value.booleanValue()).commit();
        }
        doSettingChanged(value);
    }

    protected void createPreference() {
        synchronized (SpaceSwitchSetting.class) {
            if (mPreferences == null) {
                mPreferences = GlobalContext.getContext().getSharedPreferences("space_prefence", 4);
            }
        }
    }

    public void doSettingChanged(Boolean change, boolean manually) {
        if (change.booleanValue()) {
            doSwitchOn();
        } else {
            doSwitchOff();
        }
    }

    public void doSettingChanged(Boolean change) {
        doSettingChanged(change, false);
    }

    public void onBackup(String value) {
        setValue(Boolean.valueOf(value));
    }

    public boolean isSwitchOn() {
        return getValue().booleanValue();
    }

    protected void doCheck() {
    }
}
