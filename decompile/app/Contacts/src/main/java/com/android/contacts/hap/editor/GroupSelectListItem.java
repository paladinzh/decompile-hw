package com.android.contacts.hap.editor;

import com.android.contacts.model.account.AccountWithDataSet;

public class GroupSelectListItem {
    private final String mAccountName;
    private final String mAccountType;
    private final String mDataSet;
    private final long mGroupId;
    private final boolean mGroupIsReadOnly;
    private boolean mIsChecked;
    private final boolean mIsFirstGroupInAccount;
    private final boolean mIsPrivateGroup;
    private final String mTitle;

    public GroupSelectListItem(String accountName, String accountType, String dataSet, long groupId, String title, boolean isFirstGroupInAccount, boolean checked, boolean groupIsReadOnly, boolean isPrivaterGroup) {
        this.mAccountName = accountName;
        this.mAccountType = accountType;
        this.mDataSet = dataSet;
        this.mGroupId = groupId;
        this.mTitle = title;
        this.mIsFirstGroupInAccount = isFirstGroupInAccount;
        this.mIsChecked = checked;
        this.mGroupIsReadOnly = groupIsReadOnly;
        this.mIsPrivateGroup = isPrivaterGroup;
    }

    public String getAccountName() {
        return this.mAccountName;
    }

    public String getAccountType() {
        return this.mAccountType;
    }

    public String getDataSet() {
        return this.mDataSet;
    }

    public AccountWithDataSet getAccountWithDataSet() {
        return new AccountWithDataSet(this.mAccountName, this.mAccountType, this.mDataSet);
    }

    public long getGroupId() {
        return this.mGroupId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public boolean isChecked() {
        return this.mIsChecked;
    }

    public boolean isFirstGroupInAccount() {
        return this.mIsFirstGroupInAccount;
    }

    public String toString() {
        return this.mTitle;
    }
}
