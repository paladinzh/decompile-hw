package com.android.contacts.model.dataitem;

import android.content.ContentValues;

public class StructuredNameDataItem extends DataItem {
    public StructuredNameDataItem() {
        super(new ContentValues());
        getContentValues().put("mimetype", "vnd.android.cursor.item/name");
    }

    StructuredNameDataItem(ContentValues values) {
        super(values);
    }

    public String getDisplayName() {
        return getContentValues().getAsString("data1");
    }

    public void setDisplayName(String name) {
        getContentValues().put("data1", name);
    }

    public String getPhoneticGivenName() {
        return getContentValues().getAsString("data7");
    }

    public String getPhoneticMiddleName() {
        return getContentValues().getAsString("data8");
    }

    public String getPhoneticFamilyName() {
        return getContentValues().getAsString("data9");
    }

    public void setPhoneticFamilyName(String name) {
        getContentValues().put("data9", name);
    }

    public void setPhoneticMiddleName(String name) {
        getContentValues().put("data8", name);
    }

    public void setPhoneticGivenName(String name) {
        getContentValues().put("data7", name);
    }
}
