package com.huawei.cspcommon.util;

import com.huawei.cspcommon.util.HanziToPinyin.Token;
import java.util.ArrayList;

public class TokensWithName {
    private String mTargetName;
    private ArrayList<Token> mTokens = new ArrayList();

    public void setName(String name) {
        this.mTargetName = name;
    }

    public String getName() {
        return this.mTargetName;
    }

    public ArrayList<Token> getTokens() {
        return this.mTokens;
    }
}
