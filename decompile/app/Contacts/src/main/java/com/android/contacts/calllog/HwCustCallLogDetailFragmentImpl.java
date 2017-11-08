package com.android.contacts.calllog;

import android.content.Context;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustCallLogDetailFragmentImpl extends HwCustCallLogDetailFragment {
    public boolean checkAndInitCall(Context aContext, CharSequence aNumber) {
        if (aNumber == null) {
            return false;
        }
        return HwCustContactFeatureUtils.checkAndInitCall(aContext, aNumber.toString());
    }
}
