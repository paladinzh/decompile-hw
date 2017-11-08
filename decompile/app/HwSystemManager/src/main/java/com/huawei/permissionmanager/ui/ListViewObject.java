package com.huawei.permissionmanager.ui;

import android.content.Context;

/* compiled from: Permission */
class ListViewObject {
    private boolean mIsTagObject;
    private int mTagTextCode;

    public ListViewObject() {
        this.mIsTagObject = false;
        this.mTagTextCode = 0;
    }

    public ListViewObject(boolean isTag, int tagTextCode) {
        this.mIsTagObject = isTag;
        this.mTagTextCode = tagTextCode;
    }

    public ListViewObject(ListViewObject listViewObj) {
        this.mIsTagObject = listViewObj.mIsTagObject;
        this.mTagTextCode = listViewObj.mTagTextCode;
    }

    public boolean isTag() {
        return this.mIsTagObject;
    }

    public String getTagText(Context context) {
        return context.getString(this.mTagTextCode);
    }
}
