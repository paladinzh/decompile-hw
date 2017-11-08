package com.huawei.systemmanager.secpatch.common;

import java.util.ArrayList;
import java.util.List;

public class SecurityPatchInfoBean {
    private List<SecPatchItem> mSecPatchList = new ArrayList();
    private String mSecPatchPver;

    public void setSecPatchPver(String secPatchPver) {
        this.mSecPatchPver = secPatchPver;
    }

    public String getSecPatchPver() {
        return this.mSecPatchPver;
    }

    public void addSecPatchList(SecPatchItem secPatchItem) {
        this.mSecPatchList.add(secPatchItem);
    }

    public List<SecPatchItem> getSecPatchList() {
        return this.mSecPatchList;
    }

    public int getSecPatchNum() {
        return this.mSecPatchList.size();
    }
}
