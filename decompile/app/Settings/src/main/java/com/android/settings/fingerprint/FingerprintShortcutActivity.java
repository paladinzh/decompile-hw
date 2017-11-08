package com.android.settings.fingerprint;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.TwoStatePreference;
import com.android.settings.MLog;
import com.android.settings.SettingsPreferenceFragment;

public class FingerprintShortcutActivity extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String[] PHONE_COLUMNS = new String[]{"display_name", "data1"};
    private final String KEY_DIY_APP = "fingerprint_diy_app_checkbox";
    private final String KEY_DIY_CONTACT = "fingerprint_diy_contact_checkbox";
    private String fpIDInfo = null;
    private Context mContext;
    private String mDisplayName;
    private Preference mDiyApp;
    private Preference mDiyContact;
    private TwoStatePreference mEnabledSwitch;
    private int mFpId;
    private HwCustFingerprintSettingsFragmentImpl mHwCustFingerprintSettingsFragmentImpl;
    private boolean mIsToFinish = true;
    private String mPhoneNumber;
    private Uri mUri;
    private Bundle mbundle = null;

    public void setmIsToFinish(boolean mIsToFinish) {
        this.mIsToFinish = mIsToFinish;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        this.mbundle = getIntent().getBundleExtra("fp_msg");
        if (this.mbundle == null) {
            finish();
            return;
        }
        this.mFpId = this.mbundle.getInt("fp_id");
        initPreference();
    }

    public void onResume() {
        super.onResume();
        this.fpIDInfo = Secure.getString(this.mContext.getContentResolver(), "FP_" + this.mFpId);
        this.mHwCustFingerprintSettingsFragmentImpl = new HwCustFingerprintSettingsFragmentImpl();
        String summary = this.mHwCustFingerprintSettingsFragmentImpl.queryNameFromdb(this.mContext, this.mFpId);
        if (summary == null || summary.isEmpty()) {
            this.mEnabledSwitch.setChecked(false);
            this.mDiyContact.setEnabled(false);
            this.mDiyApp.setEnabled(false);
            return;
        }
        this.mEnabledSwitch.setChecked(true);
        this.mDiyContact.setEnabled(true);
        this.mDiyApp.setEnabled(true);
    }

    public void onPause() {
        if (this.mIsToFinish) {
            setResult(101);
            finish();
        } else {
            this.mIsToFinish = true;
        }
        super.onPause();
        MLog.d("FingerprintShortcutActivity", "onPause ...... ");
    }

    public void initPreference() {
        addPreferencesFromResource(2131230789);
        this.mEnabledSwitch = (TwoStatePreference) findPreference("fp_onlylock_enable_switch");
        this.mEnabledSwitch.setOnPreferenceChangeListener(this);
        this.mDiyContact = findPreference("fingerprint_diy_contact_checkbox");
        this.mDiyContact.setOnPreferenceClickListener(this);
        this.mDiyApp = findPreference("fingerprint_diy_app_checkbox");
        this.mDiyApp.setOnPreferenceClickListener(this);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null) {
                this.mUri = data.getData();
                Cursor cursor = this.mContext.getContentResolver().query(this.mUri, PHONE_COLUMNS, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            this.mPhoneNumber = cursor.getString(1);
                            this.mDisplayName = cursor.getString(0);
                        }
                        cursor.close();
                    } catch (Exception e) {
                        MLog.e("FingerprintShortcutActivity", "Unable to get Phone message : " + e.getMessage());
                        this.mPhoneNumber = null;
                        this.mDisplayName = null;
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
                if (this.mPhoneNumber == null) {
                    MLog.d("FingerprintShortcutActivity", "Unable to get PhoneNumber");
                } else {
                    if (this.mDisplayName == null || this.mDisplayName.isEmpty()) {
                        MLog.d("FingerprintShortcutActivity", "Unable to get mDisplayName");
                        this.mDisplayName = this.mPhoneNumber;
                    }
                    String telValue = "tel:" + this.mUri.toString() + "_" + this.mPhoneNumber + "_" + this.mDisplayName;
                    try {
                        if (telValue.getBytes("UTF-8").length < 512) {
                            Secure.putString(this.mContext.getContentResolver(), "FP_" + this.mFpId, telValue);
                        }
                    } catch (Exception e2) {
                        MLog.e("FingerprintShortcutActivity", "UnsupportedEncodingException : " + e2.getMessage());
                    }
                }
            } else {
                MLog.d("FingerprintShortcutActivity", "PhoneData == null");
            }
        }
        if (requestCode == 2) {
            if (data != null) {
                String packageName = data.getStringExtra("extra.app.pakege");
                String className = data.getStringExtra("extra.app.class");
                if (packageName == null || packageName.isEmpty() || className == null || className.isEmpty()) {
                    MLog.d("FingerprintShortcutActivity", "Unable to get packageName or className");
                } else {
                    String appValue = "app:" + packageName + ";" + className;
                    try {
                        if (appValue.getBytes("UTF-8").length < 512) {
                            Secure.putString(this.mContext.getContentResolver(), "FP_" + this.mFpId, appValue);
                        }
                    } catch (Exception e22) {
                        MLog.e("FingerprintShortcutActivity", "UnsupportedEncodingException : " + e22.getMessage());
                    }
                }
            } else {
                MLog.d("FingerprintShortcutActivity", "AppData == null");
            }
        }
        if (resultCode == -1) {
            setResult(-1);
            finish();
        }
        if (resultCode == 101) {
            setResult(101);
            finish();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = false;
        if (preference instanceof TwoStatePreference) {
            isChecked = ((Boolean) newValue).booleanValue();
        }
        if (preference == this.mEnabledSwitch) {
            if (isChecked) {
                this.mEnabledSwitch.setChecked(true);
                this.mDiyContact.setEnabled(true);
                this.mDiyApp.setEnabled(true);
            } else {
                this.mContext.getContentResolver().delete(Secure.getUriFor("FP_" + this.mFpId), null, null);
                this.mEnabledSwitch.setChecked(false);
                this.mDiyContact.setEnabled(false);
                this.mDiyApp.setEnabled(false);
            }
        }
        return false;
    }

    public boolean onPreferenceClick(Preference preference) {
        Intent intent;
        if ("fingerprint_diy_contact_checkbox".equals(preference.getKey())) {
            intent = new Intent();
            intent.setAction("android.intent.action.PICK");
            intent.putExtra("FingerPrint_pause_finish", true);
            intent.setClassName("com.android.contacts", "com.android.contacts.activities.ContactSelectionActivity");
            intent.setType("vnd.android.cursor.dir/phone_v2");
            setmIsToFinish(false);
            startActivityForResult(intent, 1);
        } else if ("fingerprint_diy_app_checkbox".equals(preference.getKey())) {
            intent = new Intent();
            intent.setClass(getActivity(), ApplicationListPreference.class);
            intent.putExtra("fp_msg", this.mbundle);
            setmIsToFinish(false);
            startActivityForResult(intent, 2);
        }
        return true;
    }

    protected int getMetricsCategory() {
        return 49;
    }
}
