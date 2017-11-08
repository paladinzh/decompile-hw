package com.android.contacts.group;

public final class GroupListItem {
    private final String mAccountName;
    private final String mAccountType;
    private final String mDataSet;
    private final long mGroupId;
    private final boolean mGroupIsReadOnly;
    private final boolean mIsFirstGroupInAccount;
    private final boolean mIsPrivateGroup;
    private final int mMemberCount;
    private final String mTitle;

    public GroupListItem(String accountName, String accountType, String dataSet, long groupId, String title, boolean isFirstGroupInAccount, int memberCount, boolean groupIsReadOnly, boolean isPrivaterGroup) {
        this.mAccountName = accountName;
        this.mAccountType = accountType;
        this.mDataSet = dataSet;
        this.mGroupId = groupId;
        this.mTitle = title;
        this.mIsFirstGroupInAccount = isFirstGroupInAccount;
        this.mMemberCount = memberCount;
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

    public long getGroupId() {
        return this.mGroupId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public int getMemberCount() {
        return this.mMemberCount;
    }

    public boolean isFirstGroupInAccount() {
        return this.mIsFirstGroupInAccount;
    }

    public boolean isGroupReadOnly() {
        return this.mGroupIsReadOnly;
    }

    public boolean isPrivateGroup() {
        return this.mIsPrivateGroup;
    }
}
