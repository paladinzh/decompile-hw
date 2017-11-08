package com.android.contacts.hap.util;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import com.android.contacts.hap.editor.HAPSelectAccountDialogFragment;
import com.android.contacts.hap.util.HAPAccountListAdapter.AccountListFilter;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.AccountSelectionUtil;
import com.google.android.gms.R;
import java.util.List;

public class ManageContactsUtil {
    public static boolean handleImportFromSDCardRequest(Activity aContext, Fragment targetFragment) {
        List<AccountWithDataSet> lAccountList = AccountTypeManager.getInstance(aContext).getAccountsExcludeSim(true);
        int size = lAccountList.size();
        if (size > 1) {
            Bundle args = new Bundle();
            args.putInt("resourceId", R.string.import_from_sdcard);
            args.putBoolean("EXCLUDE_SIM", true);
            args.putInt("resId", R.string.import_from_sdcard);
            HAPSelectAccountDialogFragment.show(aContext.getFragmentManager(), targetFragment, R.string.Import_from_storage, AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, args, null, "accounts_chooser_tag_mcu");
            return false;
        }
        AccountWithDataSet accountWithDataSet;
        if (size == 1) {
            accountWithDataSet = (AccountWithDataSet) lAccountList.get(0);
        } else {
            accountWithDataSet = null;
        }
        AccountSelectionUtil.doImport(aContext, R.string.import_from_sdcard, accountWithDataSet);
        return true;
    }
}
