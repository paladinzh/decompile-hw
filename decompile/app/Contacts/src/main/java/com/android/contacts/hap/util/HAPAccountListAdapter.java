package com.android.contacts.hap.util;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HiCloudUtil;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class HAPAccountListAdapter extends BaseAdapter {
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
        ACCOUNTS_COPY_ALLOWED,
        ACCOUNTS_WRITABLE_EXCLUDE_CURRENT
    }

    static class ViewHolder {
        ImageView icon;
        RadioButton radioButton;
        TextView text1;
        TextView text2;

        ViewHolder() {
        }
    }

    public HAPAccountListAdapter(Context context, AccountListFilter accountListFilter, AccountWithDataSet currentAccount, boolean aIsDropdownList) {
        this.mContext = context;
        this.mAccountTypes = AccountTypeManager.getInstance(context);
        this.mAccounts = getAccounts(accountListFilter, 0, currentAccount);
        this.mInflater = LayoutInflater.from(context);
        this.mCurrentAccount = currentAccount;
        this.mIsDropdownList = aIsDropdownList;
    }

    public HAPAccountListAdapter(Context context, AccountListFilter aAccountListFilter, AccountWithDataSet aCurrentAccount, Bundle aBundle) {
        this.mContext = context;
        this.mAccountTypes = AccountTypeManager.getInstance(context);
        if (aBundle.getBoolean("EXCLUDE_SIM", false)) {
            this.mAccounts = this.mAccountTypes.getAccountsExcludeSim(false);
        } else if (aBundle.getBoolean("EXCLUDE_SIM1", false)) {
            if (SimUtility.isSimReady(1)) {
                this.mAccounts = this.mAccountTypes.getAccountsExcludeSim1(false);
            } else {
                this.mAccounts = this.mAccountTypes.getAccountsExcludeBothSim(false);
            }
        } else if (!aBundle.getBoolean("EXCLUDE_SIM2", false)) {
            this.mAccounts = getAccounts(aAccountListFilter, 0, aCurrentAccount);
        } else if (SimUtility.isSimReady(0)) {
            this.mAccounts = this.mAccountTypes.getAccountsExcludeSim2(false);
        } else {
            this.mAccounts = this.mAccountTypes.getAccountsExcludeBothSim(false);
        }
        if (aCurrentAccount != null && this.mAccounts.remove(aCurrentAccount)) {
            this.mAccounts.add(0, aCurrentAccount);
        }
        this.mInflater = LayoutInflater.from(context);
        this.mCurrentAccount = aCurrentAccount;
        this.mIsDropdownList = false;
    }

    public HAPAccountListAdapter(Context context, AccountListFilter accountListFilter, AccountWithDataSet currentAccount, int excludeSim) {
        this.mContext = context;
        this.mAccountTypes = AccountTypeManager.getInstance(context);
        this.mAccounts = getAccounts(accountListFilter, excludeSim, currentAccount);
        if (!(excludeSim == 0 || currentAccount == null || this.mAccounts.isEmpty() || ((AccountWithDataSet) this.mAccounts.get(0)).equals(currentAccount) || !this.mAccounts.remove(currentAccount))) {
            this.mAccounts.add(0, currentAccount);
        }
        this.mInflater = LayoutInflater.from(context);
        this.mCurrentAccount = currentAccount;
        this.mIsDropdownList = false;
    }

    private List<AccountWithDataSet> getAccounts(AccountListFilter accountListFilter, int excludeSim, AccountWithDataSet currentAccount) {
        boolean z = true;
        if (accountListFilter == AccountListFilter.ACCOUNTS_GROUP_WRITABLE) {
            return new ArrayList(this.mAccountTypes.getGroupWritableAccounts());
        }
        boolean isSim1Ready;
        boolean isSim2Ready;
        ArrayList<AccountWithDataSet> accountsList;
        ArrayList<AccountWithDataSet> tempAccountList;
        if (accountListFilter == AccountListFilter.ACCOUNTS_WRITABLE_EXCLUDE_CURRENT) {
            if (SimFactoryManager.isDualSim()) {
                isSim1Ready = SimUtility.isSimReady(0);
                isSim2Ready = SimUtility.isSimReady(1);
                if (!isSim1Ready && !isSim2Ready) {
                    accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim(true);
                } else if (!isSim1Ready) {
                    accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim1(true);
                } else if (isSim2Ready) {
                    accountsList = (ArrayList) this.mAccountTypes.getAccounts(true);
                } else {
                    accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim2(true);
                }
            } else if (SimUtility.isSimReady(-1)) {
                accountsList = (ArrayList) this.mAccountTypes.getAccounts(true);
            } else {
                accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim(true);
            }
            tempAccountList = new ArrayList();
            tempAccountList.addAll(accountsList);
            if (tempAccountList.contains(currentAccount)) {
                tempAccountList.remove(currentAccount);
            }
            return tempAccountList;
        } else if (accountListFilter == AccountListFilter.ACCOUNTS_COPY_ALLOWED) {
            if (SimFactoryManager.isDualSim()) {
                isSim1Ready = SimUtility.isSimReady(0);
                isSim2Ready = SimUtility.isSimReady(1);
                if (!isSim1Ready && !isSim2Ready) {
                    accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim(false);
                } else if (!isSim1Ready) {
                    accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim1(false);
                } else if (isSim2Ready) {
                    accountsList = (ArrayList) this.mAccountTypes.getAccounts(false);
                } else {
                    accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim2(false);
                }
            } else if (SimUtility.isSimReady(-1)) {
                accountsList = (ArrayList) this.mAccountTypes.getAccounts(false);
            } else {
                accountsList = (ArrayList) this.mAccountTypes.getAccountsExcludeSim(false);
            }
            tempAccountList = new ArrayList();
            tempAccountList.addAll(accountsList);
            if (this.mAccountTypes.getAccounts(true).size() == 1) {
                tempAccountList.remove(this.mAccountTypes.getAccounts(true).get(0));
            }
            if (currentAccount != null) {
                tempAccountList.remove(currentAccount);
                tempAccountList.add(0, currentAccount);
            }
            return tempAccountList;
        } else if (excludeSim == 1) {
            r8 = this.mAccountTypes;
            if (accountListFilter != AccountListFilter.ACCOUNTS_CONTACT_WRITABLE) {
                z = false;
            }
            return new ArrayList(r8.getAccountsExcludeSim(z));
        } else if (excludeSim == 2) {
            r8 = this.mAccountTypes;
            if (accountListFilter != AccountListFilter.ACCOUNTS_CONTACT_WRITABLE) {
                z = false;
            }
            return new ArrayList(r8.getAccountsExcludeSim1(z));
        } else if (excludeSim == 3) {
            r8 = this.mAccountTypes;
            if (accountListFilter != AccountListFilter.ACCOUNTS_CONTACT_WRITABLE) {
                z = false;
            }
            return new ArrayList(r8.getAccountsExcludeSim2(z));
        } else {
            r8 = this.mAccountTypes;
            if (accountListFilter != AccountListFilter.ACCOUNTS_CONTACT_WRITABLE) {
                z = false;
            }
            return new ArrayList(r8.getAccounts(z));
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = this.mInflater.inflate(R.layout.account_selector_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text1 = (TextView) convertView.findViewById(16908308);
            viewHolder.text2 = (TextView) convertView.findViewById(16908309);
            viewHolder.icon = (ImageView) convertView.findViewById(16908294);
            viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radio_button);
            convertView.setTag(viewHolder);
        }
        viewHolder.radioButton.setClickable(false);
        if (this.mIsDropdownList) {
            viewHolder.radioButton.setVisibility(0);
        } else {
            viewHolder.radioButton.setVisibility(8);
        }
        AccountWithDataSet account = (AccountWithDataSet) this.mAccounts.get(position);
        if (account.equals(this.mCurrentAccount)) {
            viewHolder.radioButton.setChecked(true);
        } else {
            viewHolder.radioButton.setChecked(false);
        }
        AccountType accountType = this.mAccountTypes.getAccountType(account.type, account.dataSet);
        if (viewHolder.text1 != null) {
            viewHolder.text1.setText(accountType.getDisplayLabel(this.mContext));
        }
        if (CommonUtilMethods.isLocalDefaultAccount(account.type)) {
            if (HiCloudUtil.getHicloudAccountState(this.mContext) == 1) {
                if (viewHolder.text1 != null) {
                    viewHolder.text1.setText(CommonUtilMethods.getHiCloudAccountLogOnSyncStateDisplayString(this.mContext, HiCloudUtil.isHicloudSyncStateEnabled(this.mContext)));
                }
                if (viewHolder.text2 != null) {
                    viewHolder.text2.setVisibility(0);
                    viewHolder.text2.setText(HiCloudUtil.getHiCloudAccountName());
                    viewHolder.text2.setEllipsize(TruncateAt.MIDDLE);
                }
            } else if (viewHolder.text2 != null) {
                viewHolder.text2.setVisibility(8);
            }
        } else if (CommonUtilMethods.isSimAccount(account.type)) {
            if (viewHolder.text2 != null) {
                viewHolder.text2.setVisibility(8);
            }
        } else if (viewHolder.text2 != null) {
            viewHolder.text2.setVisibility(0);
            viewHolder.text2.setText(account.name);
            viewHolder.text2.setEllipsize(TruncateAt.MIDDLE);
        }
        if (viewHolder.icon != null) {
            viewHolder.icon.setImageDrawable(accountType.getDisplayIcon(this.mContext));
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
