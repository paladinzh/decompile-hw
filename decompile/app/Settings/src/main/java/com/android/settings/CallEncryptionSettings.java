package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.IPowerManager.Stub;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.HwTelephonyManager;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class CallEncryptionSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private SwitchPreference mCallEncryptionSwitchPrefs;
    private AlertDialog mDialog;
    private boolean mPositiveResult;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230750);
        this.mCallEncryptionSwitchPrefs = (SwitchPreference) findPreference("call_encryption_switch");
        updateCallEncryptionSwitchPrefs();
    }

    public void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        if (!"call_encryption_switch".equals(key) || Utils.isMonkeyRunning()) {
            return false;
        }
        showConfirmDialog();
        return true;
    }

    private void updateCallEncryptionSwitchPrefs() {
        if (this.mCallEncryptionSwitchPrefs != null) {
            boolean isCallEncryptionOn = Secure.getInt(getContentResolver(), "encrypt_version", 0) == 1;
            this.mCallEncryptionSwitchPrefs.setOnPreferenceChangeListener(null);
            this.mCallEncryptionSwitchPrefs.setChecked(isCallEncryptionOn);
            this.mCallEncryptionSwitchPrefs.setOnPreferenceChangeListener(this);
        }
    }

    private void dismissDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    public static boolean shouldDisplay() {
        boolean isChineseVersion = !SettingsExtUtils.isGlobalVersion();
        boolean supportCallEncryption = SystemProperties.getBoolean("persist.sys.cdma_encryption", false);
        boolean supportCallEncryptionAdd = SystemProperties.getBoolean("ro.config.support_encrypt", false);
        boolean isSimCardPresent = false;
        if (isChineseVersion && supportCallEncryption && supportCallEncryptionAdd) {
            isSimCardPresent = SettingsExtUtils.isSimCardPresent();
        }
        boolean z = (isChineseVersion && supportCallEncryption && supportCallEncryptionAdd) ? isSimCardPresent : false;
        Log.d("CallEncryptionSettings", "shouldDisplay:" + z + "|isSimCardPresent:" + isSimCardPresent + "|supportCallEncryption:" + supportCallEncryption + "|supportCallEncryptionAdd:" + supportCallEncryptionAdd + "|isChineseVersion:" + isChineseVersion);
        return z;
    }

    public static boolean cmdForECInfo(int event, int action) {
        boolean success = false;
        byte[] bt = new byte[]{(byte) action};
        try {
            success = ((Boolean) HwTelephonyManager.getDefault().getClass().getDeclaredMethod("cmdForECInfo", new Class[]{Integer.TYPE, Integer.TYPE, byte[].class}).invoke(HwTelephonyManager.getDefault(), new Object[]{Integer.valueOf(event), Integer.valueOf(action), bt})).booleanValue();
        } catch (NoSuchMethodException e) {
            Log.d("CallEncryptionSettings", "NoSuchMethodException" + e.toString());
        } catch (IllegalAccessException e2) {
            Log.d("CallEncryptionSettings", "IllegalAccessException" + e2.toString());
        } catch (IllegalArgumentException e3) {
            Log.d("CallEncryptionSettings", "IllegalArgumentException" + e3.toString());
        } catch (InvocationTargetException e4) {
            Log.d("CallEncryptionSettings", "InvocationTargetException" + e4.toString());
        } catch (Exception e5) {
            Log.d("CallEncryptionSettings", "Exception" + e5.toString());
        }
        Log.d("CallEncryptionSettings", "cmdForECInfo return success:" + success + "|event:" + event + "|action:" + action);
        return success;
    }

    private void showConfirmDialog() {
        int positiveButtonResId;
        dismissDialog();
        boolean isCallEncryptionOn = Secure.getInt(getContentResolver(), "encrypt_version", 0) == 1;
        Builder builder = new Builder(getContext());
        if (isCallEncryptionOn) {
            builder.setMessage(2131628860);
            positiveButtonResId = 2131628862;
        } else {
            builder.setMessage(2131628859);
            positiveButtonResId = 2131628861;
        }
        builder.setPositiveButton(positiveButtonResId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CallEncryptionSettings.this.handlePositiveOnClick();
            }
        });
        builder.setNegativeButton(17039360, null);
        builder.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                CallEncryptionSettings.this.handleOnDismiss();
            }
        });
        this.mDialog = builder.create();
        this.mDialog.getWindow().setType(2003);
        this.mDialog.show();
    }

    private void handlePositiveOnClick() {
        int i = 0;
        if (shouldDisplay()) {
            boolean isCallEncryptionOn;
            int i2;
            if (Secure.getInt(getContentResolver(), "encrypt_version", 0) == 1) {
                isCallEncryptionOn = true;
            } else {
                isCallEncryptionOn = false;
            }
            if (isCallEncryptionOn) {
                i2 = 0;
            } else {
                i2 = 1;
            }
            if (cmdForECInfo(8, i2)) {
                this.mPositiveResult = true;
                ContentResolver contentResolver = getContentResolver();
                String str = "encrypt_version";
                if (!isCallEncryptionOn) {
                    i = 1;
                }
                Secure.putInt(contentResolver, str, i);
                try {
                    Stub.asInterface(ServiceManager.getService("power")).reboot(false, "huawei_reboot", false);
                } catch (Exception e) {
                    Log.e("CallEncryptionSettings", "Exception" + e.toString());
                }
            }
        }
    }

    private void handleOnDismiss() {
        if (!this.mPositiveResult) {
            updateCallEncryptionSwitchPrefs();
        }
        this.mPositiveResult = false;
    }
}
