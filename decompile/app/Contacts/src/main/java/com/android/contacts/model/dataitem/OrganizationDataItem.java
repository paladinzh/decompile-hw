package com.android.contacts.model.dataitem;

import android.content.ContentValues;

public class OrganizationDataItem extends DataItem {
    OrganizationDataItem(ContentValues values) {
        super(values);
    }

    public String getCompany() {
        return getContentValues().getAsString("data1");
    }

    public String getTitle() {
        return getContentValues().getAsString("data4");
    }
}
