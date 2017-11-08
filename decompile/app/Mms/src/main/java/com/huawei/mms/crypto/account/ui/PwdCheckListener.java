package com.huawei.mms.crypto.account.ui;

public interface PwdCheckListener {
    void onForgetPwd();

    void onPwdVerifyStart(String str);

    void onPwdVerifyStart(String str, String str2);
}
