package com.android.mms.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import com.android.mms.HwCustCommonConfig;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsConfig;
import com.android.mms.data.ContactList;
import com.google.android.gms.R;
import java.util.Arrays;

public class HwCustRecipientListFragmentImpl extends HwCustRecipientListFragment {
    private boolean isRcsEnable = HwCustCommonConfig.isRCSSwitchOn();

    public HwCustRecipientListFragmentImpl(Context context) {
        super(context);
    }

    public boolean getIsTitleChangeWhenRecepientsChange() {
        return HwCustMmsConfigImpl.getIsTitleChangeWhenRecepientsChange();
    }

    public void setRecipientCountWithMax(ActionBar actionBar, int cnt) {
        int maxCnt = MmsConfig.getRecipientLimit();
        actionBar.setSubtitle(this.mContext.getResources().getQuantityString(R.plurals.recipient_count_with_max, cnt, new Object[]{Integer.valueOf(cnt), Integer.valueOf(maxCnt)}));
    }

    public ContactList getContactList(Intent intent, ContactList contactList) {
        if (!this.isRcsEnable || intent == null) {
            return contactList;
        }
        String[] address = intent.getStringArrayExtra("recipients");
        if (address == null || address.length == 0) {
            return contactList;
        }
        return ContactList.getByNumbers(Arrays.asList(address), false);
    }
}
