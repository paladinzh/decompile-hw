package com.android.settings.datausage;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import com.android.settings.datausage.DataSaverBackend.Listener;

public class DataSaverPreference extends Preference implements Listener {
    private final DataSaverBackend mDataSaverBackend;

    public DataSaverPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDataSaverBackend = new DataSaverBackend(context);
    }

    public void onAttached() {
        super.onAttached();
        this.mDataSaverBackend.addListener(this);
    }

    public void onDetached() {
        super.onDetached();
        this.mDataSaverBackend.addListener(this);
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        setSummary(isDataSaving ? 2131627161 : 2131627162);
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
    }
}
