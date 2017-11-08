package com.android.mms.ui;

import android.content.Context;
import com.android.mms.util.HwCustUiUtils;

public class HwCustMessageListViewImpl extends HwCustMessageListView {
    public HwCustMessageListViewImpl(Context context) {
        super(context);
    }

    public String updateForwardSubject(String aFwdSubject, String aMsgSubject) {
        return HwCustUiUtils.updateForwardSubject(aFwdSubject, aMsgSubject);
    }
}
