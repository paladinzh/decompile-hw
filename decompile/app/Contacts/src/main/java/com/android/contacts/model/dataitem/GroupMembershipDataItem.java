package com.android.contacts.model.dataitem;

import android.content.ContentValues;

public class GroupMembershipDataItem extends DataItem {
    GroupMembershipDataItem(ContentValues values) {
        super(values);
    }

    public Long getGroupRowId() {
        return getContentValues().getAsLong("data1");
    }
}
