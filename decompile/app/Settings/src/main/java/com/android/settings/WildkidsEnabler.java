package com.android.settings;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class WildkidsEnabler {
    private static final Uri STATUS_URI = Uri.parse("content://com.huawei.wildkids/wildkids_status");
    private static final String TAG = WildkidsEnabler.class.getCanonicalName();
    protected final Context mContext;
    private OnPreferenceChangeListener mPreferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(WildkidsEnabler.this.mContext, preference, newValue);
            WildkidsEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    protected Preference mStatusPreference;
    protected SwitchPreference mSwitchPreference;

    public WildkidsEnabler(Context context, Preference preference) {
        this.mContext = context;
        this.mStatusPreference = preference;
    }

    public void resume() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangeListener);
        }
        updateStatusText();
        updateSwitchStatus();
    }

    public void pause() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
    }

    protected void updateStatusText() {
        if (this.mStatusPreference != null) {
            int i;
            Preference preference = this.mStatusPreference;
            if (isWildkidsModeOn()) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            preference.setSummary(i);
        }
    }

    protected void updateSwitchStatus() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setChecked(isWildkidsModeOn());
        }
    }

    protected void performCheck(boolean checked) {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setChecked(checked);
        }
    }

    private boolean isWildkidsModeOn() {
        int status = 0;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(STATUS_URI, new String[]{"wildkids_status"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                status = cursor.getInt(cursor.getColumnIndex("wildkids_status"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "isWildkidsModeOn get failed");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (status == 1) {
            return true;
        }
        return false;
    }
}
