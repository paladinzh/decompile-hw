package com.android.settings.fingerprint;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.CustomSwitchPreference;

public class HwCustFingerprintMainSettingsFragmentImpl extends HwCustFingerprintMainSettingsFragment implements OnPreferenceChangeListener {
    private static final String DB_FP_BROWSE_PICTURE = "fingerprint_gallery_slide";
    private static final String DB_FP_SHOW_NOTIFICATION = "fp_show_notification";
    private static final boolean FP_SHOW_NOTIFICATION_ON = SystemProperties.getBoolean("ro.config.fp_add_notification", false);
    private static final boolean HAS_FP_CUST_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation_plk", false);
    private static final boolean HAS_FP_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation", false);
    private static final String KEY_FP_BROWSE_PICTURE = "key_fp_browse_picture";
    private static final String KEY_FP_COMMENT = "fp_main_settings_comment";
    private static final String KEY_FP_FUNCTION_CATEGORY = "fp_touch_function_category";
    private static final String KEY_FP_SHOW_NOTIFICATION = "key_fp_show_notification";
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private Context mContext;

    public HwCustFingerprintMainSettingsFragmentImpl(FingerprintMainSettingsFragment fingerprintMainSettingsFragment) {
        super(fingerprintMainSettingsFragment);
        this.mContext = fingerprintMainSettingsFragment.getActivity();
    }

    public void addPreferencesFromResource() {
        PreferenceScreen root = this.mFingerprintMainSettingsFragment.getPreferenceScreen();
        if (FP_SHOW_NOTIFICATION_ON) {
            this.mFingerprintMainSettingsFragment.addPreferencesFromResource(2131230795);
        }
        if (HAS_FP_NAVIGATION) {
            PreferenceCategory functionCategory = (PreferenceCategory) root.findPreference(KEY_FP_FUNCTION_CATEGORY);
            Preference commentPreference = root.findPreference(KEY_FP_COMMENT);
            CustomSwitchPreference browsePicture = new CustomSwitchPreference(this.mContext);
            if (functionCategory != null && commentPreference != null && browsePicture != null) {
                browsePicture.setKey(KEY_FP_BROWSE_PICTURE);
                browsePicture.setTitle(2131628281);
                browsePicture.setSummary(2131628282);
                browsePicture.setPersistent(false);
                functionCategory.addPreference(browsePicture);
                browsePicture.setOrder(commentPreference.getOrder());
                commentPreference.setOrder(browsePicture.getOrder() + 1);
            } else {
                return;
            }
        }
        updateSwitchState();
    }

    public void updateSwitchState() {
        boolean z = true;
        if (FP_SHOW_NOTIFICATION_ON) {
            CustomSwitchPreference csf = findPreferenceAndSetListener(KEY_FP_SHOW_NOTIFICATION);
            if (csf != null) {
                boolean z2;
                if (Secure.getIntForUser(this.mContext.getContentResolver(), DB_FP_SHOW_NOTIFICATION, 1, ActivityManager.getCurrentUser()) == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                csf.setChecked(z2);
            }
        }
        if (HAS_FP_NAVIGATION) {
            CustomSwitchPreference browsePicture = findPreferenceAndSetListener(KEY_FP_BROWSE_PICTURE);
            if (browsePicture != null) {
                if (Secure.getInt(this.mContext.getContentResolver(), DB_FP_BROWSE_PICTURE, 1) != 1) {
                    z = false;
                }
                browsePicture.setChecked(z);
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        String key = preference.getKey();
        boolean isChecked = ((Boolean) newValue).booleanValue();
        ContentResolver contentResolver;
        String str;
        if (KEY_FP_SHOW_NOTIFICATION.equals(key)) {
            contentResolver = this.mContext.getContentResolver();
            str = DB_FP_SHOW_NOTIFICATION;
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, ActivityManager.getCurrentUser());
        } else if (KEY_FP_BROWSE_PICTURE.equals(key)) {
            contentResolver = this.mContext.getContentResolver();
            str = DB_FP_BROWSE_PICTURE;
            if (isChecked) {
                i = 1;
            }
            Secure.putInt(contentResolver, str, i);
        }
        return true;
    }

    private CustomSwitchPreference findPreferenceAndSetListener(String key) {
        CustomSwitchPreference pref = (CustomSwitchPreference) this.mFingerprintMainSettingsFragment.findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(this);
        }
        return pref;
    }

    public boolean hasCustomizeFingerprint() {
        return HAS_FP_CUST_NAVIGATION;
    }

    public void checkRemovePref(String key) {
        if ("true".equals(System.getString(this.mContext.getContentResolver(), "isDisableKeyGuard"))) {
            PreferenceScreen mPreferenceScreen = this.mFingerprintMainSettingsFragment.getPreferenceScreen();
            if (mPreferenceScreen != null) {
                Preference mPreference = this.mFingerprintMainSettingsFragment.findPreference(key);
                if (mPreference != null) {
                    mPreferenceScreen.removePreference(mPreference);
                }
            }
        }
    }
}
