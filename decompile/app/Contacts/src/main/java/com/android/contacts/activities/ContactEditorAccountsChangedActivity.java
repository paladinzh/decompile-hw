package com.android.contacts.activities;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.android.contacts.editor.ContactEditorUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HiColudAccountsListAdapter;
import com.android.contacts.util.HiColudAccountsListAdapter.AccountListFilter;
import com.google.android.gms.R;
import java.util.List;

public class ContactEditorAccountsChangedActivity extends Activity {
    final OnDismissListener dismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            ContactEditorAccountsChangedActivity.this.finishActivity();
        }
    };
    private HiColudAccountsListAdapter mAccountListAdapter;
    private final OnClickListener mAddAccountClickListener = new OnClickListener() {
        public void onClick(View v) {
            ContactEditorAccountsChangedActivity.this.mDialog.dismiss();
            ContactEditorAccountsChangedActivity.this.startActivity(ContactEditorAccountsChangedActivity.this.mEditorUtils.createAddWritableAccountIntentExcludePhoneAndSIM());
        }
    };
    private Context mContext;
    private Dialog mDialog;
    DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
            if (arg1 == -2) {
                ContactEditorAccountsChangedActivity.this.mDialog.dismiss();
            }
        }
    };
    private ContactEditorUtils mEditorUtils;
    private boolean mIsExcludeSim1;
    private boolean mIsExcludeSim2;
    final DialogInterface.OnClickListener mSingelChoiceListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int position) {
            if (ContactEditorAccountsChangedActivity.this.mAccountListAdapter != null) {
                dialog.dismiss();
                AccountWithDataSet account = ContactEditorAccountsChangedActivity.this.mAccountListAdapter.getItem(position);
                if (account != null && "com.android.huawei.sim".equals(account.type) && !SimFactoryManager.isSIM1CardPresent()) {
                    return;
                }
                if (account == null || !"com.android.huawei.secondsim".equals(account.type) || SimFactoryManager.isSIM2CardPresent()) {
                    if (position == ContactEditorAccountsChangedActivity.this.mAccountListAdapter.getCount() - 1 && !EmuiFeatureManager.isSuperSaverMode()) {
                        Intent accountIntent = ContactEditorAccountsChangedActivity.this.mEditorUtils.createAddWritableAccountIntentExcludePhoneAndSIM();
                        if (CommonUtilMethods.isActivityAvailable(ContactEditorAccountsChangedActivity.this.mContext, accountIntent)) {
                            ContactEditorAccountsChangedActivity.this.startActivity(accountIntent);
                        } else {
                            Toast.makeText(ContactEditorAccountsChangedActivity.this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
                        }
                    } else if (account != null) {
                        ContactEditorAccountsChangedActivity.this.saveAccountAndReturnResult(account);
                    }
                }
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        overridePendingTransition(0, 0);
        this.mContext = this;
        this.mEditorUtils = ContactEditorUtils.getInstance(this);
        this.mIsExcludeSim1 = this.mEditorUtils.isExcludeSim1();
        this.mIsExcludeSim2 = this.mEditorUtils.isExcludeSim2();
        if (this.mEditorUtils.isExcludeSim()) {
            List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(this).getAccountsExcludeSim(true);
        } else if (this.mIsExcludeSim1 && !this.mIsExcludeSim2) {
            r0 = AccountTypeManager.getInstance(this).getAccountsExcludeSim1(true);
        } else if (!this.mIsExcludeSim1 && this.mIsExcludeSim2) {
            r0 = AccountTypeManager.getInstance(this).getAccountsExcludeSim2(true);
        } else if (this.mIsExcludeSim1 && this.mIsExcludeSim2) {
            r0 = AccountTypeManager.getInstance(this).getAccountsExcludeBothSim(true);
        } else {
            r0 = AccountTypeManager.getInstance(this).getAccounts(true);
        }
        showDialog(R.id.contact_editor_select_account);
    }

    private void saveAccountAndReturnResult(AccountWithDataSet account) {
        this.mEditorUtils.saveDefaultAndAllAccounts(account);
        Intent intent = new Intent();
        if (EmuiFeatureManager.isAndroidMVersion()) {
            intent.putExtra("android.provider.extra.ACCOUNT", account);
        } else {
            intent.putExtra("com.android.contacts.extra.ACCOUNT", account);
        }
        setResult(-1, intent);
        finishActivity();
    }

    protected Dialog onCreateDialog(int id) {
        if (id == R.id.contact_editor_select_account) {
            if (this.mEditorUtils.isExcludeSim()) {
                this.mAccountListAdapter = new HiColudAccountsListAdapter(this, AccountListFilter.ACCOUNTS_EXCLUDE_SIM, false);
            } else if (this.mIsExcludeSim1 && !this.mIsExcludeSim2) {
                this.mAccountListAdapter = new HiColudAccountsListAdapter(this, AccountListFilter.ACCOUNTS_EXCLUDE_SIM1, false);
            } else if (!this.mIsExcludeSim1 && this.mIsExcludeSim2) {
                this.mAccountListAdapter = new HiColudAccountsListAdapter(this, AccountListFilter.ACCOUNTS_EXCLUDE_SIM2, false);
            } else if (this.mIsExcludeSim1 && this.mIsExcludeSim2) {
                this.mAccountListAdapter = new HiColudAccountsListAdapter(this, AccountListFilter.ACCOUNTS_EXCLUDE_BOTH_SIM, false);
            } else {
                this.mAccountListAdapter = new HiColudAccountsListAdapter(this, AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, false);
            }
            this.mDialog = new Builder(this).setTitle(getString(R.string.contact_editor_account_selection_title)).setAdapter(this.mAccountListAdapter, this.mSingelChoiceListener).setNegativeButton(getString(R.string.cancel_label), this.mDialogListener).create();
            this.mDialog.setOnDismissListener(this.dismissListener);
        }
        return this.mDialog;
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(0, 0);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }
}
