package com.android.contacts.util;

import com.android.contacts.ContactsApplication;
import com.huawei.cust.HwCustUtils;

public class EncryptCallUtils {
    private static HwCustEncryptCallUtils mCust = null;
    private static boolean sNeedInit = true;

    public static HwCustEncryptCallUtils getCust() {
        if (mCust == null) {
            mCust = (HwCustEncryptCallUtils) HwCustUtils.createObj(HwCustEncryptCallUtils.class, new Object[0]);
        }
        if (mCust == null) {
            mCust = new HwCustEncryptCallUtils();
        }
        if (sNeedInit) {
            sNeedInit = false;
            mCust.init(ContactsApplication.getContext());
        }
        return mCust;
    }
}
