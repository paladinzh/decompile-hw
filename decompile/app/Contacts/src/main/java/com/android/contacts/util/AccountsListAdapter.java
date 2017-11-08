package com.android.contacts.util;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class AccountsListAdapter extends BaseAdapter {
    private final AccountTypeManager mAccountTypes;
    private final List<AccountWithDataSet> mAccounts;
    private final Context mContext;
    private AccountWithDataSet mCurrentAccount;
    private final LayoutInflater mInflater;
    private final boolean mIsDropdownList;

    public enum AccountListFilter {
        ALL_ACCOUNTS,
        ACCOUNTS_CONTACT_WRITABLE,
        ACCOUNTS_GROUP_WRITABLE,
        ACCOUNTS_EXCLUDE_SIM,
        ACCOUNTS_EXCLUDE_SIM1,
        ACCOUNTS_EXCLUDE_SIM2,
        ACCOUNTS_EXCLUDE_BOTH_SIM
    }

    static class ViewHolder {
        TextView text1;
        TextView text2;

        ViewHolder() {
        }
    }

    public AccountsListAdapter(Context context, AccountListFilter accountListFilter, AccountWithDataSet currentAccount) {
        this.mContext = context;
        this.mAccountTypes = AccountTypeManager.getInstance(context);
        this.mAccounts = getAccounts(accountListFilter);
        this.mInflater = LayoutInflater.from(context);
        this.mCurrentAccount = currentAccount;
        this.mIsDropdownList = false;
    }

    public AccountsListAdapter(Context context, AccountListFilter accountListFilter, AccountWithDataSet currentAccount, boolean aIsDropdownlist) {
        this.mContext = context;
        this.mAccountTypes = AccountTypeManager.getInstance(context);
        this.mAccounts = getAccounts(accountListFilter);
        this.mInflater = LayoutInflater.from(context);
        this.mCurrentAccount = currentAccount;
        this.mIsDropdownList = aIsDropdownlist;
    }

    private List<AccountWithDataSet> getAccounts(AccountListFilter accountListFilter) {
        boolean z = true;
        if (accountListFilter == AccountListFilter.ACCOUNTS_GROUP_WRITABLE) {
            return new ArrayList(this.mAccountTypes.getGroupWritableAccounts());
        }
        if (accountListFilter == AccountListFilter.ACCOUNTS_EXCLUDE_SIM) {
            return new ArrayList(this.mAccountTypes.getAccountsExcludeSim(true));
        }
        if (accountListFilter == AccountListFilter.ACCOUNTS_EXCLUDE_SIM1) {
            return new ArrayList(this.mAccountTypes.getAccountsExcludeSim1(true));
        }
        if (accountListFilter == AccountListFilter.ACCOUNTS_EXCLUDE_SIM2) {
            return new ArrayList(this.mAccountTypes.getAccountsExcludeSim2(true));
        }
        if (accountListFilter == AccountListFilter.ACCOUNTS_EXCLUDE_BOTH_SIM) {
            return new ArrayList(this.mAccountTypes.getAccountsExcludeBothSim(true));
        }
        AccountTypeManager accountTypeManager = this.mAccountTypes;
        if (accountListFilter != AccountListFilter.ACCOUNTS_CONTACT_WRITABLE) {
            z = false;
        }
        return new ArrayList(accountTypeManager.getAccounts(z));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            if (this.mIsDropdownList) {
                convertView = this.mInflater.inflate(R.layout.contact_editor_account_list_item, parent, false);
            } else {
                convertView = this.mInflater.inflate(R.layout.account_selector_list_item, parent, false);
            }
            viewHolder = new ViewHolder();
            viewHolder.text1 = (TextView) convertView.findViewById(16908308);
            viewHolder.text2 = (TextView) convertView.findViewById(16908309);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AccountWithDataSet account = (AccountWithDataSet) this.mAccounts.get(position);
        viewHolder.text1.setText(this.mAccountTypes.getAccountType(account.type, account.dataSet).getDisplayLabel(this.mContext));
        if (CommonUtilMethods.isLocalDefaultAccount(account.type)) {
            if (HiCloudUtil.getHicloudAccountState(this.mContext) == 1) {
                viewHolder.text1.setText(CommonUtilMethods.getHiCloudAccountLogOnSyncStateDisplayString(this.mContext, HiCloudUtil.isHicloudSyncStateEnabled(this.mContext)));
                viewHolder.text2.setVisibility(0);
                viewHolder.text2.setText(HiCloudUtil.getHiCloudAccountName());
            } else {
                viewHolder.text2.setVisibility(8);
            }
        } else if (CommonUtilMethods.isSimAccount(account.type)) {
            viewHolder.text2.setVisibility(8);
        } else {
            viewHolder.text2.setVisibility(0);
            viewHolder.text2.setText(account.name);
        }
        if (!this.mIsDropdownList) {
            viewHolder.text2.setEllipsize(TruncateAt.MIDDLE);
        }
        return convertView;
    }

    public int getCount() {
        return this.mAccounts.size();
    }

    public AccountWithDataSet getItem(int position) {
        return (AccountWithDataSet) this.mAccounts.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void setCurrentAccount(AccountWithDataSet currentAccount) {
        this.mCurrentAccount = currentAccount;
    }
}
