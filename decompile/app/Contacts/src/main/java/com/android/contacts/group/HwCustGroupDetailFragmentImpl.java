package com.android.contacts.group;

import android.view.Menu;
import android.view.MenuItem;
import com.android.contacts.hap.HwCustCommonConstants;
import com.google.android.gms.R;

public class HwCustGroupDetailFragmentImpl extends HwCustGroupDetailFragment {
    public void customizeOptionsMenu(Menu renameGroup, boolean mIsReadOnly, String accountType) {
        if (HwCustCommonConstants.IS_AAB_ATT && HwCustCommonConstants.AAB_ACCOUNT_TYPE.equals(accountType) && mIsReadOnly && renameGroup != null) {
            MenuItem renameGroupItem = renameGroup.findItem(R.id.menu_rename_group);
            if (renameGroupItem != null) {
                renameGroupItem.setVisible(false);
            }
            MenuItem renameOverflowMenuItem = renameGroup.findItem(R.id.sub_menu_rename_group);
            if (renameOverflowMenuItem != null) {
                renameOverflowMenuItem.setVisible(false);
            }
        }
    }
}
