package com.android.mms.util;

import java.util.Arrays;

public class HwQuickActionContact {
    private String contactName;
    private long id;
    private byte[] mData;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContactName() {
        return this.contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setData(byte[] data) {
        this.mData = data.length != 0 ? Arrays.copyOf(data, data.length) : null;
    }

    public byte[] getData() {
        if (this.mData == null) {
            return new byte[0];
        }
        byte[] data = new byte[this.mData.length];
        System.arraycopy(this.mData, 0, data, 0, this.mData.length);
        return data;
    }
}
