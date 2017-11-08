package com.android.mms.ui;

import android.app.ActionBar;
import android.content.Context;

public class HwCustRecipientListFragment {
    protected Context mContext;

    public HwCustRecipientListFragment(Context context) {
        this.mContext = context;
    }

    public boolean getIsTitleChangeWhenRecepientsChange() {
        return false;
    }

    public void setRecipientCountWithMax(ActionBar actionBar, int cnt) {
    }
}
