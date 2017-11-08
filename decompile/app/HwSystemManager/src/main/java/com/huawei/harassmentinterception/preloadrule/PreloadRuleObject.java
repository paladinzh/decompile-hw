package com.huawei.harassmentinterception.preloadrule;

import android.text.TextUtils;

public class PreloadRuleObject {
    private String mKey;
    private int mSubType;
    private int mType;

    public PreloadRuleObject(int nType, int nSubType, String strKey) {
        this.mType = nType;
        this.mSubType = nSubType;
        this.mKey = strKey;
    }

    public boolean isMatchWhiteList(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber) || this.mType != 0) {
            return false;
        }
        switch (this.mSubType) {
            case 0:
                if (phoneNumber.startsWith(this.mKey)) {
                    return true;
                }
                break;
            case 1:
                if (phoneNumber.equals(this.mKey)) {
                    return true;
                }
                break;
        }
        return false;
    }
}
