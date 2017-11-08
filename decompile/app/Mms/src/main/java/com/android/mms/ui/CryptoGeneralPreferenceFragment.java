package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.ui.MsimSmsEncryptSetting;
import com.huawei.mms.ui.SmsEncryptSetting;
import com.huawei.mms.util.StatisticalHelper;

public class CryptoGeneralPreferenceFragment {

    public interface OnRestoreDefaultCryptoListener {
    }

    public void restoreDefaultCrypto(PreferenceCategory parent) {
        addSmsEncryptPref(parent);
    }

    public void addSmsEncryptPref(PreferenceCategory parent) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && parent != null) {
            Preference listDivideLineEnctypt = new Preference(parent.getContext());
            listDivideLineEnctypt.setLayoutResource(R.layout.listdivider);
            listDivideLineEnctypt.setOrder(Integer.MAX_VALUE);
            parent.addPreference(listDivideLineEnctypt);
            Preference smsEnctyptPref = new Preference(parent.getContext());
            smsEnctyptPref.setKey("pref_key_sms_encrypt");
            smsEnctyptPref.setPersistent(false);
            smsEnctyptPref.setTitle(R.string.sms_encrypt_setting_title);
            smsEnctyptPref.setWidgetLayoutResource(R.layout.mms_preference_widget_arrow);
            smsEnctyptPref.setOrder(Integer.MAX_VALUE);
            parent.addPreference(smsEnctyptPref);
        }
    }

    public void onPreferenceTreeClick(Context context, Preference preference) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            launchSmsEncryptSetting(context, preference);
        }
    }

    private void launchSmsEncryptSetting(Context context, Preference preference) {
        MLog.i("CryptoGeneralPreferenceFragment", "enter into sms enctypt settings");
        if ("pref_key_sms_encrypt".equals(preference.getKey())) {
            StatisticalHelper.incrementReportCount(context, 2167);
            if (MessageUtils.isMultiSimEnabled()) {
                context.startActivity(new Intent(context, MsimSmsEncryptSetting.class));
            } else {
                context.startActivity(new Intent(context, SmsEncryptSetting.class));
            }
        }
    }
}
