package com.android.contacts;

public final class GroupMetaData {
    private String mAccountName;
    private String mAccountType;
    private String mDataSet;
    private boolean mDefaultGroup;
    private boolean mFavorites;
    private long mGroupId;
    private String mResPackage;
    private String mSync1;
    private String mSync4;
    private String mTitle;
    private int mTitleRes;

    public GroupMetaData(String accountName, String accountType, String dataSet, long groupId, String title, boolean defaultGroup, boolean favorites, String mSync1, String mSync4, int mTitleRes, String mResPackage) {
        this.mAccountName = accountName;
        this.mAccountType = accountType;
        this.mDataSet = dataSet;
        this.mGroupId = groupId;
        this.mTitle = title;
        this.mDefaultGroup = defaultGroup;
        this.mFavorites = favorites;
        this.mSync1 = mSync1;
        this.mSync4 = mSync4;
        this.mTitleRes = mTitleRes;
        this.mResPackage = mResPackage;
    }

    public String getAccountType() {
        return this.mAccountType;
    }

    public long getGroupId() {
        return this.mGroupId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public boolean isDefaultGroup() {
        return this.mDefaultGroup;
    }

    public boolean isFavorites() {
        return this.mFavorites;
    }

    public String getmSync4() {
        return this.mSync4;
    }

    public int getmTitleRes() {
        return this.mTitleRes;
    }

    public String getmResPackage() {
        return this.mResPackage;
    }
}
