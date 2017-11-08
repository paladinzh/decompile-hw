package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.huawei.android.settings.OtgCustEx;

public class OtgEnabler implements OnPreferenceChangeListener {
    private Boolean isOtgFeatureEnabled = Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_otgFeature", false));
    private final Context mContext;
    private AlertDialog mDialog;
    private Preference mStatusPreference;
    private SwitchPreference mSwitchPreference;

    class OnClickEx implements OnClickListener {
        boolean otgEnable;

        public OnClickEx(boolean b) {
            this.otgEnable = b;
        }

        public void onClick(DialogInterface dialog, int dialogId) {
            if (-1 == dialogId && !Utils.isMonkeyRunning() && OtgEnabler.this.isOtgFeatureEnabled.booleanValue()) {
                Global.putInt(OtgEnabler.this.mContext.getContentResolver(), "otg_enabled", this.otgEnable ? 1 : 0);
                OtgEnabler.this.updateStatusText();
                OtgEnabler.this.updateSwitchStatus();
                OtgCustEx.writeOtgStatusToOeminfo(this.otgEnable ? "enable" : "disable");
                ((PowerManager) OtgEnabler.this.mContext.getSystemService("power")).reboot("otg_status_changed_reboot");
            }
        }
    }

    public OtgEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
    }

    public OtgEnabler(Context context, Preference preference) {
        this.mContext = context;
        this.mStatusPreference = preference;
    }

    public void updateStatusText() {
        int isEnabledOeminfor = 0;
        if (this.mStatusPreference != null) {
            int i;
            int isEnabled = Global.getInt(this.mContext.getContentResolver(), "otg_enabled", 0);
            if ("enable".equals(OtgCustEx.readOtgStatusFromOeminfo())) {
                isEnabledOeminfor = 1;
            }
            if (isEnabled != isEnabledOeminfor) {
                Global.putInt(this.mContext.getContentResolver(), "otg_enabled", isEnabledOeminfor);
            }
            Preference preference = this.mStatusPreference;
            if (isEnabled == 1) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            preference.setSummary(i);
        }
    }

    public void resume() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this);
        }
        updateStatusText();
        updateSwitchStatus();
    }

    public void pause() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
    }

    public void destroy() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = ((Boolean) newValue).booleanValue();
        if (preference == this.mSwitchPreference) {
            boolean z;
            if (1 == Global.getInt(this.mContext.getContentResolver(), "otg_enabled", 0)) {
                z = true;
            } else {
                z = false;
            }
            if (isChecked == z) {
                return true;
            }
            showDialog(isChecked);
        }
        return false;
    }

    public void updateSwitchStatus() {
        boolean z = true;
        if (this.mSwitchPreference != null) {
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (1 != Global.getInt(this.mContext.getContentResolver(), "otg_enabled", 0)) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    public void showDialog(boolean otgEnable) {
        this.mDialog = new Builder(this.mContext).setMessage(otgEnable ? 2131629302 : 2131629303).setPositiveButton(17039370, new OnClickEx(otgEnable)).setNegativeButton(17039360, new OnClickEx(otgEnable)).create();
        this.mDialog.show();
    }
}
