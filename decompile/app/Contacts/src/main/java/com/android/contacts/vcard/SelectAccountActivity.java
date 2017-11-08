package com.android.contacts.vcard;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.AccountSelectionUtil.AccountSelectedListener;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.List;

public class SelectAccountActivity extends Activity {
    private AccountSelectedListener mAccountSelectionListener;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            SelectAccountActivity.this.finish();
        }

        public void onCancel(DialogInterface dialog) {
            SelectAccountActivity.this.finish();
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        List<AccountWithDataSet> accountList = AccountTypeManager.getInstance(this).getAccountsExcludeSim(true);
        if (accountList.size() == 0) {
            HwLog.w("SelectAccountActivity", "Account does not exist");
            finish();
        } else if (accountList.size() == 1) {
            AccountWithDataSet account = (AccountWithDataSet) accountList.get(0);
            Intent intent = new Intent();
            intent.putExtra("account_name", account.name);
            intent.putExtra("account_type", account.type);
            intent.putExtra("data_set", account.dataSet);
            setResult(-1, intent);
            finish();
        } else {
            if (HwLog.HWFLOW) {
                HwLog.i("SelectAccountActivity", "The number of available accounts: " + accountList.size());
            }
            this.mAccountSelectionListener = new AccountSelectedListener(this, accountList, R.string.import_from_sdcard) {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    AccountWithDataSet account = (AccountWithDataSet) this.mAccountList.get(which);
                    Intent intent = new Intent();
                    intent.putExtra("account_name", account.name);
                    intent.putExtra("account_type", account.type);
                    intent.putExtra("data_set", account.dataSet);
                    SelectAccountActivity.this.setResult(-1, intent);
                    SelectAccountActivity.this.finish();
                }
            };
            showDialog(R.string.import_from_sdcard);
        }
    }

    protected Dialog onCreateDialog(int resId, Bundle bundle) {
        switch (resId) {
            case R.string.import_from_sdcard:
                if (this.mAccountSelectionListener != null) {
                    return AccountSelectionUtil.getSelectAccountDialog(this, resId, this.mAccountSelectionListener, new CancelListener(), true);
                }
                throw new NullPointerException("mAccountSelectionListener must not be null.");
            default:
                return super.onCreateDialog(resId, bundle);
        }
    }
}
