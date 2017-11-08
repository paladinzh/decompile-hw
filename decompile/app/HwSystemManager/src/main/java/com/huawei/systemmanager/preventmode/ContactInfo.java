package com.huawei.systemmanager.preventmode;

import java.io.Serializable;

public class ContactInfo implements Serializable {
    private static final long serialVersionUID = -5053412967314724078L;
    private String mName;
    private String mPhone;

    public ContactInfo(String name, String phone) {
        this.mName = name;
        this.mPhone = phone;
    }

    public String getmName() {
        return this.mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPhone() {
        return this.mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }
}
