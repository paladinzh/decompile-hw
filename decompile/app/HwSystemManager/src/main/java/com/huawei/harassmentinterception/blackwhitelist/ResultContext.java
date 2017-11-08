package com.huawei.harassmentinterception.blackwhitelist;

import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import java.util.ArrayList;
import java.util.List;

public class ResultContext {
    private List<ContactInfo> mContacts;
    private boolean mIsExist;
    private int mResult;

    public List<ContactInfo> getContacts() {
        return this.mContacts;
    }

    public boolean isExist() {
        return this.mIsExist;
    }

    public ResultContext(int result, boolean isExist, List<ContactInfo> contacts) {
        this.mResult = result;
        this.mIsExist = isExist;
        this.mContacts = contacts;
    }

    public ResultContext(int result, boolean isExist) {
        this.mResult = result;
        this.mIsExist = isExist;
        this.mContacts = new ArrayList();
    }
}
