package com.android.settings.fingerprint;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.hardware.fingerprint.Fingerprint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.MLog;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import java.util.HashMap;
import java.util.List;

public class HwCustFingerprintSettingsFragmentImpl extends HwCustFingerprintSettingsFragment implements OnPreferenceChangeListener {
    public static final String APP_PATTERN = "app";
    private static final int FP_MAX_VAULE = 512;
    private static final boolean FP_VIBRATION_CONFIG = SystemProperties.getBoolean("ro.config.fp_vibration", false);
    private static final String KEY_FP_VIBRATION = "key_fp_vibration";
    private static final String KEY_FP_VIBRATION_CAT = "fp_vibration_cat";
    private static final String KEY_FP_VIBRATION_PREF = "fp_vibration_pref";
    private static final String[] PHONE_COLUMNS = new String[]{"display_name", "data1"};
    private static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 0;
    private static final int PHONE_NUMBER_COLUMN_INDEX = 1;
    private static final int SET_FP_SUMMARY = 1;
    private static final long SET_FP_SUMMARY_DELAY = 50;
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    public static final String TAG = "HwCustFingerprintSettingsFragmentImpl";
    public static final String TEL_PATTERN = "tel";
    private String FP_SETTING_START_SHOT_CUT = "ro.config.fp_launch_app";
    private Context mContext;
    private List<Fingerprint> mFingerprints;
    private HashMap<Integer, String> mFpMap = new HashMap();
    private SetSummaryThread mSetSummaryThread;
    private Thread mThread;
    private final Object mUserLock = new Object();
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (HwCustFingerprintSettingsFragmentImpl.this.mUserLock) {
                        for (int i = 0; i < HwCustFingerprintSettingsFragmentImpl.this.mFingerprints.size(); i++) {
                            try {
                                int fpMapId = ((Fingerprint) HwCustFingerprintSettingsFragmentImpl.this.mFingerprints.get(i)).getFingerId();
                                ((HighlightPreference) HwCustFingerprintSettingsFragmentImpl.this.mFingerprintSettingsFragment.findPreference(FingerprintUtils.generateFpPrefKey(fpMapId))).setSummary((CharSequence) (String) HwCustFingerprintSettingsFragmentImpl.this.mFpMap.get(Integer.valueOf(fpMapId)));
                            } catch (Exception e) {
                                MLog.e(HwCustFingerprintSettingsFragmentImpl.TAG, "Unable to get pref : " + e.getMessage());
                            }
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    };

    class SetSummaryThread implements Runnable {
        SetSummaryThread() {
        }

        public void run() {
            synchronized (HwCustFingerprintSettingsFragmentImpl.this.mUserLock) {
                HwCustFingerprintSettingsFragmentImpl.this.mFpMap.clear();
                for (int i = 0; i < HwCustFingerprintSettingsFragmentImpl.this.mFingerprints.size(); i++) {
                    int fingerprintId = ((Fingerprint) HwCustFingerprintSettingsFragmentImpl.this.mFingerprints.get(i)).getFingerId();
                    HwCustFingerprintSettingsFragmentImpl.this.mFpMap.put(Integer.valueOf(fingerprintId), HwCustFingerprintSettingsFragmentImpl.this.queryFpSummary(HwCustFingerprintSettingsFragmentImpl.this.mContext, fingerprintId));
                }
            }
            Message msg = HwCustFingerprintSettingsFragmentImpl.this.myHandler.obtainMessage();
            msg.what = 1;
            HwCustFingerprintSettingsFragmentImpl.this.myHandler.sendMessageDelayed(msg, HwCustFingerprintSettingsFragmentImpl.SET_FP_SUMMARY_DELAY);
        }
    }

    public HwCustFingerprintSettingsFragmentImpl(FingerprintSettingsFragment fingerprintSettingsFragment) {
        super(fingerprintSettingsFragment);
        this.mContext = fingerprintSettingsFragment.getActivity();
    }

    public void updateStatus() {
        PreferenceCategory fp_vibration_cat = null;
        if (FP_VIBRATION_CONFIG) {
            CustomSwitchPreference customSwitchPreference;
            PreferenceScreen root = this.mFingerprintSettingsFragment.getPreferenceScreen();
            if (root != null) {
                fp_vibration_cat = (PreferenceCategory) root.findPreference(KEY_FP_VIBRATION_CAT);
            }
            if (fp_vibration_cat != null) {
                customSwitchPreference = (CustomSwitchPreference) fp_vibration_cat.findPreference(KEY_FP_VIBRATION_PREF);
            } else {
                customSwitchPreference = null;
            }
            if (fp_vibration_cat != null && customSwitchPreference != null) {
                fp_vibration_cat.setTitle(2131629296);
                customSwitchPreference.setTitle(2131629297);
                customSwitchPreference.setSummary(2131629298);
                customSwitchPreference.setChecked(switchStatus());
                customSwitchPreference.setOnPreferenceChangeListener(this);
            }
        }
    }

    private boolean switchStatus() {
        return System.getInt(this.mContext.getContentResolver(), KEY_FP_VIBRATION, 1) == 1;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        boolean isChecked = ((Boolean) newValue).booleanValue();
        if (KEY_FP_VIBRATION_PREF.equals(key)) {
            System.putInt(this.mContext.getContentResolver(), KEY_FP_VIBRATION, isChecked ? 1 : 0);
        }
        return true;
    }

    public boolean isShowFingerprintVibration() {
        return FP_VIBRATION_CONFIG;
    }

    public boolean fingerPrintShotcut() {
        return SystemProperties.getBoolean(this.FP_SETTING_START_SHOT_CUT, false);
    }

    public String queryFpSummary(Context mContext, int mFpId) {
        String fpSummary = queryNameFromdb(mContext, mFpId);
        if (fpSummary == null || fpSummary.isEmpty()) {
            return mContext.getResources().getString(2131629285);
        }
        return fpSummary;
    }

    public void setFpSummary(Context context, List<Fingerprint> fingerprints) {
        synchronized (this.mUserLock) {
            this.mFingerprints = fingerprints;
        }
        this.mFingerprints = fingerprints;
        if (this.mContext == null) {
            this.mContext = context;
        }
        this.mSetSummaryThread = new SetSummaryThread();
        this.mThread = new Thread(this.mSetSummaryThread);
        this.mThread.start();
    }

    public String queryNameFromdb(Context mContext, int fpId) {
        String fpIDInfo = Secure.getString(mContext.getContentResolver(), "FP_" + fpId);
        if (fpIDInfo == null || fpIDInfo.isEmpty()) {
            MLog.w(TAG, "Unable to get fpIDInfo ,the fingerprintId =  " + fpId);
            return null;
        } else if (fpIDInfo.split(":", 2).length < 2) {
            return null;
        } else {
            String appOrTel = fpIDInfo.split(":", 2)[0];
            if (appOrTel.equalsIgnoreCase(APP_PATTERN)) {
                String appName = queryAppName(mContext, fpIDInfo.split(":", 2)[1]);
                if (appName == null || appName.isEmpty()) {
                    return null;
                }
                return mContext.getResources().getString(2131629277, new Object[]{appName});
            }
            if (appOrTel.equalsIgnoreCase(TEL_PATTERN)) {
                String telInfo = fpIDInfo.split(":", 2)[1];
                if (telInfo.split("\\_").length < 3) {
                    return null;
                }
                String phoneName = queryTelName(mContext, telInfo);
                if (!(phoneName == null || phoneName.isEmpty())) {
                    return mContext.getResources().getString(2131629278, new Object[]{phoneName});
                }
            }
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String queryTelName(Context mContext, String telInfo) {
        String str = null;
        String str2 = null;
        Uri telUri = Uri.parse(telInfo.split("\\_", 3)[0]);
        String dbPhoneName = telInfo.split("\\_", 3)[2];
        Cursor cursor = mContext.getContentResolver().query(telUri, PHONE_COLUMNS, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    str2 = cursor.getString(1);
                    str = cursor.getString(0);
                }
                cursor.close();
            } catch (Exception e) {
                MLog.e(TAG, "Unable to get Phone message : " + e.getMessage());
            } catch (Throwable th) {
                cursor.close();
            }
        }
        try {
            if (("tel:" + telUri.toString() + "_" + str2 + "_" + str).getBytes("UTF-8").length >= FP_MAX_VAULE && dbPhoneName != null) {
                return dbPhoneName;
            }
            if (str != null) {
                if (!str.isEmpty()) {
                    return str;
                }
            }
            MLog.e(TAG, "displayName is null");
            return str2;
        } catch (Exception e2) {
            MLog.e(TAG, "UnsupportedEncodingException : " + e2.getMessage());
            return null;
        }
    }

    private String queryAppName(Context mContext, String appInfo) {
        if (appInfo.split("\\;").length < 2) {
            return null;
        }
        String appName;
        String packageName = appInfo.split("\\;")[0];
        String className = appInfo.split("\\;")[1];
        PackageManager pm = mContext.getPackageManager();
        try {
            Intent it = new Intent("android.intent.action.MAIN");
            it.addCategory("android.intent.category.LAUNCHER");
            it.setComponent(new ComponentName(packageName, className));
            appName = ((ResolveInfo) pm.queryIntentActivities(it, 128).get(0)).activityInfo.loadLabel(pm).toString();
        } catch (Exception e) {
            MLog.e(TAG, "Unable to get appName" + e.getMessage());
            appName = null;
        }
        return appName;
    }
}
