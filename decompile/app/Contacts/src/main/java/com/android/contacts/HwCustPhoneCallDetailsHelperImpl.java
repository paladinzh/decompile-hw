package com.android.contacts;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;

public class HwCustPhoneCallDetailsHelperImpl extends HwCustPhoneCallDetailsHelper {
    private Context mContext;
    private boolean mHideGeoInfo41 = false;
    private boolean mShowVMNumberInCalllog = SystemProperties.getBoolean("ro.config.show_vm_num_calllog", false);

    public HwCustPhoneCallDetailsHelperImpl(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    public boolean isCustHideGeoInfo() {
        boolean result = false;
        String mcc_ncc = TelephonyManager.getDefault().getSimOperator();
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int mSwitchDualCardSlot = 0;
            try {
                mSwitchDualCardSlot = TelephonyManagerEx.getDefault4GSlotId();
            } catch (NoExtAPIException e) {
                Log.e("HwCustContactDetailAdapter", "TelephonyManagerEx.getDefault4GSlotId()->NoExtAPIException!");
            }
            mcc_ncc = MSimTelephonyManager.getDefault().getSimOperator(mSwitchDualCardSlot);
        }
        String configString = System.getString(this.mContext.getContentResolver(), "hw_hide_call_geo_info");
        if ("true".equals(configString)) {
            return true;
        }
        if (!TextUtils.isEmpty(configString) && !TextUtils.isEmpty(mcc_ncc)) {
            String[] custValues = configString.trim().split(";");
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (mcc_ncc.startsWith(custValues[i]) || mcc_ncc.equalsIgnoreCase(custValues[i])) {
                    result = true;
                    break;
                }
                i++;
            }
        }
        return result;
    }

    public boolean isShowVMNumInCalllog(boolean isVoiceMailNumber) {
        return isVoiceMailNumber ? this.mShowVMNumberInCalllog : false;
    }

    public void checkCallTypeFeaturesVisibility(PhoneCallDetailsViews views, PhoneCallDetails details) {
        if (HwCustContactFeatureUtils.isSupportCallFeatureIcon() && views.hdcallIcon != null) {
            views.hdcallIcon.setVisibility(details.getCallTypeFeatures() == 1 ? 0 : 8);
        }
    }

    public void updateCustSetting() {
        this.mHideGeoInfo41 = isCustHideGeoInfo();
    }

    public boolean isCustHideGeoInfo41() {
        return this.mHideGeoInfo41;
    }
}
