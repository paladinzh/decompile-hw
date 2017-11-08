package com.android.contacts.model.dataitem;

import android.content.ContentValues;

public class ImDataItem extends DataItem {
    private final boolean mCreatedFromEmail;

    ImDataItem(ContentValues values) {
        super(values);
        this.mCreatedFromEmail = false;
    }

    private ImDataItem(ContentValues values, boolean createdFromEmail) {
        super(values);
        this.mCreatedFromEmail = createdFromEmail;
    }

    public static ImDataItem createFromEmail(EmailDataItem item) {
        ImDataItem im = new ImDataItem(new ContentValues(item.getContentValues()), true);
        im.setMimeType("vnd.android.cursor.item/im");
        return im;
    }

    public String getData() {
        if (this.mCreatedFromEmail) {
            return getContentValues().getAsString("data1");
        }
        return getContentValues().getAsString("data1");
    }

    public Integer getProtocol() {
        return getContentValues().getAsInteger("data5");
    }

    public boolean isProtocolValid() {
        return getProtocol() != null;
    }

    public String getCustomProtocol() {
        return getContentValues().getAsString("data6");
    }

    public int getChatCapability() {
        Integer result = getContentValues().getAsInteger("chat_capability");
        return result == null ? 0 : result.intValue();
    }

    public boolean isCreatedFromEmail() {
        return this.mCreatedFromEmail;
    }
}
