package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.android.provider.SettingsEx.Systemex;
import java.lang.ref.WeakReference;

public class HwCustSecuritySettingsHwBaseImpl extends HwCustSecuritySettingsHwBase implements OnPreferenceChangeListener {
    private static final String ACTION_PRIVACY_SETTINGS = "com.android.settings.PRIVACY_VERSION_SETTINGS";
    private static final String AUTOREG_PACKAGE_NAME = "com.huawei.ChnCmccAutoReg";
    private static final String DMCLIENT_PACKAGE_NAME = "com.huawei.android.dmclient";
    private static final int EVENT_ENABLE_SWITCH = 1;
    private static final String KEY_ASSISTED_GPS = "assisted_gps";
    private static final String KEY_ASSISTED_GPS_SETTINGS = "assisted_gps_settings";
    private static final String KEY_DEVICE_ADMIN_CATEGORY = "device_admin_category";
    private static final String KEY_ENCRYPTION_CATEGORY = "security_encryption_category";
    private static final String KEY_PHONE_FINDER = "phone_finder";
    private static final String KEY_SECURITY_ENCRYPTION = "security_encryption";
    private static final String KEY_SIM_LOCK = "sim_lock";
    private static final String KEY_TIME_SYNCHRONIZATION = "time_synchronization";
    private static final String KEY_TOGGLE_NANAGEMENT_PERMISSION = "toggle_management_permission";
    private static final int MAX_TOGGLE_TIME_INTERVAL = 1000;
    private static final int SHOW_MENU = 7;
    private static final String TAG = "HwCustSecuritySettingsHwBaseImpl";
    private static final int TIME_SYNCHRONIZATION_ENABLE = 1;
    private static final int TIME_SYNCHRONIZATION_UNENABLE = 0;
    private static final int UNLOCK = 0;
    private static final HwTelephonyManager mHwTelephonyManager = HwTelephonyManager.getDefault();
    private ApplicationInfo mAutoInfo;
    private ApplicationInfo mDmclientInfo;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwCustSecuritySettingsHwBaseImpl.this.mToggleManagementPermission.setEnabled(true);
                    return;
                default:
                    return;
            }
        }
    };
    private CustomSwitchPreference mToggleManagementPermission;
    private CustomSwitchPreference mToggleSecurityEncryption;

    static class DisableChanger extends AsyncTask<Object, Object, Object> {
        final WeakReference<SecuritySettingsHwBase> mActivity;
        final ApplicationInfo mInfo;
        final int mState;

        DisableChanger(SecuritySettingsHwBase activity, ApplicationInfo info, int state) {
            this.mActivity = new WeakReference(activity);
            this.mInfo = info;
            this.mState = state;
        }

        protected Object doInBackground(Object... params) {
            return null;
        }
    }

    public HwCustSecuritySettingsHwBaseImpl(SecuritySettingsHwBase securitySettingsHwBase) {
        super(securitySettingsHwBase);
    }

    public void updateCustPreference(Context context) {
        PreferenceScreen root = this.mSecuritySettingsHwBase.getPreferenceScreen();
        this.mToggleManagementPermission = new CustomSwitchPreference(context);
        this.mToggleManagementPermission.setKey(KEY_TOGGLE_NANAGEMENT_PERMISSION);
        this.mToggleManagementPermission.setTitle(2131629071);
        this.mToggleManagementPermission.setSummaryOff(2131629072);
        this.mToggleManagementPermission.setSummaryOn(2131629073);
        PreferenceCategory preferenceCategory = (PreferenceCategory) root.findPreference(KEY_DEVICE_ADMIN_CATEGORY);
        preferenceCategory.addPreference(this.mToggleManagementPermission);
        this.mToggleManagementPermission.setOnPreferenceChangeListener(this);
        if (this.mAutoInfo == null || this.mDmclientInfo == null) {
            preferenceCategory.removePreference(this.mToggleManagementPermission);
        } else {
            this.mToggleManagementPermission.setChecked(this.mAutoInfo.enabled);
        }
        if (HwCustSettingsUtils.IS_SPRINT) {
            Preference simLock = root.findPreference(KEY_SIM_LOCK);
            if (simLock != null) {
                root.removePreference(simLock);
            }
        }
        this.mSecuritySettingsHwBase.getPreferenceManager().inflateFromResource(context, 2131230860, root);
        PreferenceCategory encryptCategory = (PreferenceCategory) root.findPreference(KEY_ENCRYPTION_CATEGORY);
        if (showEncryptVersion()) {
            boolean z;
            this.mToggleSecurityEncryption = (CustomSwitchPreference) this.mSecuritySettingsHwBase.findPreference(KEY_SECURITY_ENCRYPTION);
            encryptCategory.addPreference(this.mToggleSecurityEncryption);
            if (root.findPreference(KEY_SIM_LOCK) != null) {
                if (root.findPreference(KEY_PHONE_FINDER) != null) {
                    encryptCategory.setOrder(root.findPreference(KEY_PHONE_FINDER).getOrder() + 1);
                } else {
                    encryptCategory.setOrder(root.findPreference(KEY_SIM_LOCK).getOrder() - 1);
                }
            }
            this.mToggleSecurityEncryption.setOnPreferenceChangeListener(this);
            CustomSwitchPreference customSwitchPreference = this.mToggleSecurityEncryption;
            if (Secure.getInt(context.getContentResolver(), "encrypt_version", 0) == 1) {
                z = true;
            } else {
                z = false;
            }
            customSwitchPreference.setChecked(z);
            return;
        }
        root.removePreference(encryptCategory);
    }

    public static boolean showEncryptVersion() {
        boolean isLockVersion = false;
        boolean isSwitchOpen = SystemProperties.getBoolean("ro.config.encrypt_version", false);
        if (mHwTelephonyManager != null) {
            isLockVersion = isCmdForECInfo(7, 0);
        }
        return isSwitchOpen ? isLockVersion : false;
    }

    public static boolean isCmdForECInfo(int event, int action) {
        Log.d(TAG, " cmdForECInfo supported: false|event:" + event + "|action:" + action);
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (KEY_TOGGLE_NANAGEMENT_PERMISSION.equals(key)) {
            Boolean valueObj = (Boolean) newValue;
            if (!(this.mAutoInfo == null || this.mDmclientInfo == null)) {
                this.mToggleManagementPermission.setEnabled(false);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 1000);
                if (valueObj.booleanValue()) {
                    if (!this.mAutoInfo.enabled) {
                        new DisableChanger(this.mSecuritySettingsHwBase, this.mAutoInfo, 0).execute(new Object[]{null});
                    }
                    if (!this.mDmclientInfo.enabled) {
                        new DisableChanger(this.mSecuritySettingsHwBase, this.mDmclientInfo, 0).execute(new Object[]{null});
                    }
                } else {
                    if (this.mAutoInfo.enabled) {
                        new DisableChanger(this.mSecuritySettingsHwBase, this.mAutoInfo, 3).execute(new Object[]{null});
                    }
                    if (this.mDmclientInfo.enabled) {
                        new DisableChanger(this.mSecuritySettingsHwBase, this.mDmclientInfo, 3).execute(new Object[]{null});
                    }
                }
            }
            return true;
        } else if (!KEY_SECURITY_ENCRYPTION.equals(key)) {
            return false;
        } else {
            handleSecurityEncryptionChange(newValue);
            return true;
        }
    }

    private void handleSecurityEncryptionChange(Object newValue) {
        Context context = this.mSecuritySettingsHwBase.getActivity();
        Intent intent = new Intent(ACTION_PRIVACY_SETTINGS);
        intent.addFlags(268435456);
        context.startActivity(intent);
    }

    public void enableOrDisableSimLock(Context context, Preference preference) {
        boolean isDisableSimLock = "true".equals(Systemex.getString(context.getContentResolver(), "isDisableSimLock"));
        Preference simLockPreferences = preference;
        int simState = TelephonyManager.getDefault().getSimState();
        if (simState == 1 || simState == 0 || simState == 6 || isDisableSimLock) {
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
    }
}
