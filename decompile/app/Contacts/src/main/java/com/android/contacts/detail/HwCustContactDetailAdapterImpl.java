package com.android.contacts.detail;

import android.content.Context;
import android.provider.Settings.System;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewCache;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;

public class HwCustContactDetailAdapterImpl extends HwCustContactDetailAdapter {
    private Context mContext;

    public HwCustContactDetailAdapterImpl(Context mContext) {
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

    public void setVisibility(TextView textView, int targetValue) {
        textView.setVisibility(targetValue);
    }

    public void setVisiblityForOtherEntry(DetailViewCache aViews, DetailViewEntry aEntry, ContactDetailFragment detailFragment) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired() && HwCustCommonConstants.VIBRATION_MIMETYPE.equals(aEntry.mimetype) && aViews.mIcon != null) {
            aViews.mIcon.setVisibility(8);
        }
    }

    public int getCustomLayoutIfNeeded(DetailViewEntry entry, int actualLayoutId) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired() && HwCustCommonConstants.VIBRATION_MIMETYPE.equals(entry.mimetype)) {
            return R.layout.detail_item_vibration;
        }
        return actualLayoutId;
    }

    public boolean isHideGeoInfoOfNoNameCall(boolean isNoNameCall) {
        return isNoNameCall ? isCustHideGeoInfo() : false;
    }

    public boolean isAllowSprintRedialInEmergencyMode(DetailViewEntry detailViewEntry) {
        if (!HwCustContactFeatureUtils.isSupportSprintEmergencyModeRedial()) {
            return true;
        }
        String dialNum;
        if (!IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging() || detailViewEntry.roamingData == null) {
            dialNum = PhoneNumberFormatter.parsePhoneNumber(detailViewEntry.data);
        } else {
            dialNum = PhoneNumberFormatter.parsePhoneNumber(detailViewEntry.roamingData);
        }
        return HwCustContactFeatureUtils.allowRedialEmergencyMode(dialNum, this.mContext);
    }
}
