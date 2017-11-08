package com.android.contacts.list;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.util.HiCloudUtil;
import com.google.android.gms.R;

public class ContactListFilterView extends LinearLayout {
    private static final String TAG = ContactListFilterView.class.getSimpleName();
    private TextView mAccountName;
    private TextView mAccountType;
    private TextView mContactsCount;
    private Context mContext;
    private ContactListFilter mFilter;
    private boolean mIsChecked;
    private RadioButton mRadioButton;
    private boolean mSingleAccount;

    public ContactListFilterView(Context context) {
        super(context);
        this.mContext = context;
    }

    public ContactListFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void setContactListFilter(ContactListFilter filter) {
        this.mFilter = filter;
    }

    public void setSingleAccount(boolean flag) {
        this.mSingleAccount = flag;
    }

    public void setActivated(boolean activated) {
        this.mIsChecked = activated;
    }

    public void bindView(AccountTypeManager accountTypes) {
        this.mAccountType = (TextView) findViewById(R.id.accountType);
        this.mContactsCount = (TextView) findViewById(R.id.contacts_count);
        this.mRadioButton = (RadioButton) findViewById(R.id.radioButton);
        this.mRadioButton.setChecked(this.mIsChecked);
        this.mAccountName = (TextView) findViewById(R.id.accountName);
        if (this.mFilter == null) {
            this.mAccountType.setText(R.string.contactsList);
            return;
        }
        switch (this.mFilter.filterType) {
            case -6:
                this.mAccountType.setText(R.string.list_filter_single);
                this.mAccountName.setText("");
                break;
            case -5:
                this.mAccountType.setText(R.string.list_filter_phones);
                this.mAccountName.setText("");
                break;
            case -4:
                this.mAccountType.setText(R.string.list_filter_all_starred);
                this.mAccountName.setText("");
                break;
            case -3:
                this.mAccountType.setText(R.string.list_filter_customize);
                this.mAccountName.setText("");
                this.mContactsCount.setVisibility(8);
                break;
            case -2:
                this.mAccountType.setText(R.string.list_filter_all_accounts);
                if (this.mFilter.totalRawContactsCountInAllAccounts >= 0) {
                    StringBuffer displayCounts = new StringBuffer();
                    if (this.mFilter.totalContactsCountInAllAccounts >= 0) {
                        displayCounts.append(this.mContext.getString(R.string.contact_total_counts_and_after_autojoin_count, new Object[]{Integer.valueOf(this.mFilter.totalRawContactsCountInAllAccounts), Integer.valueOf(this.mFilter.totalContactsCountInAllAccounts)}));
                    } else {
                        displayCounts.append(this.mContext.getString(R.string.contact_total_counts_only, new Object[]{Integer.valueOf(this.mFilter.totalRawContactsCountInAllAccounts)}));
                    }
                    this.mContactsCount.setVisibility(0);
                    this.mContactsCount.setText(displayCounts.toString());
                }
                this.mAccountName.setVisibility(8);
                break;
            case 0:
                AccountType accountType = accountTypes.getAccountType(this.mFilter.accountType, this.mFilter.dataSet);
                boolean isDefaultAccount = CommonUtilMethods.isLocalDefaultAccount(this.mFilter.accountType);
                this.mAccountType.setText(accountType.getDisplayLabel(getContext()));
                if (isDefaultAccount) {
                    if (HiCloudUtil.getHicloudAccountState(this.mContext) == 1) {
                        this.mAccountType.setText(CommonUtilMethods.getHiCloudAccountLogOnSyncStateDisplayString(this.mContext, HiCloudUtil.isHicloudSyncStateEnabled(this.mContext)));
                        this.mAccountName.setVisibility(0);
                        this.mAccountName.setText(HiCloudUtil.getHiCloudAccountName());
                    } else {
                        this.mAccountName.setVisibility(8);
                    }
                } else if ("com.android.huawei.sim".equalsIgnoreCase(accountType.accountType) || "com.android.huawei.secondsim".equalsIgnoreCase(accountType.accountType) || TextUtils.isEmpty(this.mFilter.accountName)) {
                    this.mAccountName.setVisibility(8);
                } else {
                    this.mAccountName.setVisibility(0);
                    this.mAccountName.setText(this.mFilter.accountName);
                }
                if (!CommonUtilMethods.isSimAccount(accountType.accountType)) {
                    if (this.mFilter.totalRawContactsCount < 0) {
                        this.mContactsCount.setVisibility(8);
                        break;
                    }
                    StringBuffer displayCountsByAccount = new StringBuffer();
                    if (!this.mIsChecked || this.mFilter.totalContactsCount < 0) {
                        displayCountsByAccount.append(this.mContext.getString(R.string.contact_total_counts_only, new Object[]{Integer.valueOf(this.mFilter.totalRawContactsCount)}));
                    } else {
                        displayCountsByAccount.append(this.mContext.getString(R.string.contact_total_counts_and_after_autojoin_count, new Object[]{Integer.valueOf(this.mFilter.totalRawContactsCount), Integer.valueOf(this.mFilter.totalContactsCount)}));
                    }
                    this.mContactsCount.setVisibility(0);
                    this.mContactsCount.setText(displayCountsByAccount.toString());
                    break;
                }
                int usedCount = this.mFilter.totalRawContactsCount;
                int capacity = this.mFilter.totalContactsCount;
                this.mContactsCount.setVisibility(0);
                this.mContactsCount.setText(this.mContext.getString(R.string.contacts_sim_count_and_capacity, new Object[]{Integer.valueOf(usedCount), Integer.valueOf(capacity)}));
                break;
                break;
        }
    }
}
