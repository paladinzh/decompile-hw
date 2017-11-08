package com.android.contacts.calllog;

import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class HwCustPhoneNumberHelperImpl extends HwCustPhoneNumberHelper {
    private String hwVoicemail = SystemProperties.get("ro.config.hw_voicemail_name_ex", "");

    public String getVoicemailTag() {
        String mcc_ncc = TelephonyManager.getDefault().getSimOperator();
        if (!(TextUtils.isEmpty(this.hwVoicemail) || TextUtils.isEmpty(mcc_ncc))) {
            String[] custMCCMNCValues = this.hwVoicemail.trim().split(";");
            for (String equals : custMCCMNCValues) {
                if (equals.equals(mcc_ncc)) {
                    return TelephonyManager.getDefault().getVoiceMailAlphaTag();
                }
            }
        }
        return "";
    }
}
