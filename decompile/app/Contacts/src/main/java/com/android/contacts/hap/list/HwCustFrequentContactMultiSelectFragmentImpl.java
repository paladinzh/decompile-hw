package com.android.contacts.hap.list;

import android.content.Context;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustFrequentContactMultiSelectFragmentImpl extends HwCustFrequentContactMultiSelectFragment {
    protected static final String TAG = "HwCustFrequentContactMultiSelectFragmentImpl";

    public HwCustFrequentContactMultiSelectFragmentImpl(Context context) {
        super(context);
    }

    public boolean getEnableEmailContactInMms() {
        if ("true".equals(Systemex.getString(this.mContext.getContentResolver(), "enable_email_contact_in_mms"))) {
            return HwCustContactFeatureUtils.isBindOnlyNumberSwitch(this.mContext);
        }
        return false;
    }

    public int getNoPhoneNumbersOrEmailsTextId() {
        return R.string.contact_noPhoneNumbersOrEmails;
    }
}
