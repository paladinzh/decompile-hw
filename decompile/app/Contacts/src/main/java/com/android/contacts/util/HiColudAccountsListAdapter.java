package com.android.contacts.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class HiColudAccountsListAdapter extends BaseAdapter {
    private final AccountTypeManager mAccountTypes;
    private final List<AccountWithDataSet> mAccounts;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final boolean mShowHiCloudAccountLogOn = false;

    public enum AccountListFilter {
        ALL_ACCOUNTS,
        ACCOUNTS_CONTACT_WRITABLE,
        ACCOUNTS_GROUP_WRITABLE,
        ACCOUNTS_EXCLUDE_SIM,
        ACCOUNTS_EXCLUDE_SIM1,
        ACCOUNTS_EXCLUDE_SIM2,
        ACCOUNTS_EXCLUDE_BOTH_SIM
    }

    static class TextViewHolder {
        TextView text1;
        TextView text2;

        TextViewHolder() {
        }
    }

    public HiColudAccountsListAdapter(Context context, AccountListFilter accountListFilter, boolean showHiCloundAccountLogOn) {
        this.mContext = context;
        this.mAccountTypes = AccountTypeManager.getInstance(context);
        this.mAccounts = getAccounts(accountListFilter);
        this.mInflater = LayoutInflater.from(context);
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
        TextViewHolder textViewHolder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.contact_hiclould_account_list_item, parent, false);
            textViewHolder = new TextViewHolder();
            textViewHolder.text1 = (TextView) convertView.findViewById(16908308);
            textViewHolder.text2 = (TextView) convertView.findViewById(16908309);
            convertView.setTag(textViewHolder);
        } else {
            textViewHolder = (TextViewHolder) convertView.getTag();
        }
        if (position != getCount() - 1 || EmuiFeatureManager.isSuperSaverMode()) {
            AccountWithDataSet account = getItem(position);
            if (account == null) {
                return convertView;
            }
            textViewHolder.text1.setText(this.mAccountTypes.getAccountType(account.type, account.dataSet).getDisplayLabel(this.mContext));
            if (CommonUtilMethods.isLocalDefaultAccount(account.type, account.name)) {
                if (HiCloudUtil.getHicloudAccountState(this.mContext) == 1) {
                    textViewHolder.text1.setText(CommonUtilMethods.getHiCloudAccountLogOnSyncStateDisplayString(this.mContext, HiCloudUtil.isHicloudSyncStateEnabled(this.mContext)));
                    textViewHolder.text2.setText(HiCloudUtil.getHiCloudAccountName());
                    textViewHolder.text2.setVisibility(0);
                } else {
                    textViewHolder.text2.setVisibility(8);
                }
            } else if (CommonUtilMethods.isSimAccount(account.type)) {
                textViewHolder.text2.setVisibility(8);
            } else {
                textViewHolder.text2.setVisibility(0);
                textViewHolder.text2.setText(account.name);
            }
        } else {
            textViewHolder.text1.setText(R.string.add_new_account);
            textViewHolder.text2.setVisibility(8);
        }
        return convertView;
    }

    public int getCount() {
        if (this.mShowHiCloudAccountLogOn) {
            return this.mAccounts.size() + 2;
        }
        if (EmuiFeatureManager.isSuperSaverMode()) {
            return this.mAccounts.size();
        }
        return this.mAccounts.size() + 1;
    }

    public AccountWithDataSet getItem(int position) {
        if (this.mShowHiCloudAccountLogOn) {
            if (position == 0 || position == getCount() - 1) {
                return null;
            }
            return (AccountWithDataSet) this.mAccounts.get(position - 1);
        } else if (EmuiFeatureManager.isSuperSaverMode()) {
            return (AccountWithDataSet) this.mAccounts.get(position);
        } else {
            if (position == getCount() - 1) {
                return null;
            }
            return (AccountWithDataSet) this.mAccounts.get(position);
        }
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemViewType(int position) {
        if (!this.mShowHiCloudAccountLogOn) {
            return (EmuiFeatureManager.isSuperSaverMode() || position != getCount() - 1) ? 0 : 1;
        } else {
            if (position == 0) {
                return 2;
            }
            return position == getCount() + -1 ? 1 : 0;
        }
    }

    public int getViewTypeCount() {
        if (this.mShowHiCloudAccountLogOn) {
            return 3;
        }
        if (EmuiFeatureManager.isSuperSaverMode()) {
            return 1;
        }
        return 2;
    }
}
