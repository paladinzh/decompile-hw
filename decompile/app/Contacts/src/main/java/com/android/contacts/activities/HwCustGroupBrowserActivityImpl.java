package com.android.contacts.activities;

import android.content.Context;
import android.view.Menu;
import com.android.contacts.hap.HwCustCommonConstants;
import com.google.android.gms.R;

public class HwCustGroupBrowserActivityImpl extends HwCustGroupBrowserActivity {
    public void customizeContextMenu(Menu renameGroup, boolean mIsReadOnly, String accountType, Context context) {
        if (HwCustCommonConstants.IS_AAB_ATT && HwCustCommonConstants.AAB_ACCOUNT_TYPE.equals(accountType) && mIsReadOnly && renameGroup != null) {
            String renameString = context.getResources().getString(R.string.contacts_group_rename);
            for (int i = 0; i < renameGroup.size(); i++) {
                if (renameGroup.getItem(i).getTitle().equals(renameString)) {
                    renameGroup.getItem(i).setVisible(false);
                    return;
                }
            }
        }
    }
}
