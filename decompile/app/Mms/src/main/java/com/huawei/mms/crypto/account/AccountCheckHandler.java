package com.huawei.mms.crypto.account;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.google.android.gms.location.LocationRequest;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.cspcommon.MLog;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

public class AccountCheckHandler implements CloudRequestHandler {
    private String mAccountName;
    private Handler mBindHandler;
    private Handler mMainHandler;
    private String mPwd;
    private int mRequestState;

    public Handler getMainHandler() {
        return this.mMainHandler;
    }

    public void setMainHandler(Handler mainHandler) {
        this.mMainHandler = mainHandler;
    }

    public String getPwd() {
        return this.mPwd;
    }

    public void setPwd(String pwd) {
        this.mPwd = pwd;
    }

    public String getAccountName() {
        return this.mAccountName;
    }

    public void setAccountName(String accountName) {
        this.mAccountName = accountName;
    }

    public AccountCheckHandler(int requestState, Handler bindHandler, Handler mainHandler) {
        this.mRequestState = requestState;
        this.mBindHandler = bindHandler;
        this.mMainHandler = mainHandler;
    }

    public void onError(ErrorStatus status) {
        MLog.i("AccountCheckHandler", "onError, the error status is: " + status.getErrorCode());
    }

    public void onFinish(Bundle bundle) {
        MLog.i("AccountCheckHandler", "AccountCheckHandler onFinish, the requestState is: " + this.mRequestState);
        switch (this.mRequestState) {
            case 0:
                if (this.mMainHandler != null) {
                    this.mMainHandler.sendMessageDelayed(Message.obtain(this.mMainHandler, 106), 100);
                    break;
                }
                break;
            case 1:
                MLog.i("AccountCheckHandler", "start to close the account bind");
                if (this.mBindHandler != null) {
                    this.mBindHandler.sendMessage(Message.obtain(this.mBindHandler, 2));
                    break;
                }
                break;
            case 2:
                if (this.mPwd != null && this.mAccountName != null) {
                    if (this.mMainHandler != null) {
                        this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, LocationRequest.PRIORITY_LOW_POWER));
                    }
                    MLog.i("AccountCheckHandler", "start to unbind the account");
                    if (this.mBindHandler != null) {
                        this.mBindHandler.sendMessage(Message.obtain(this.mBindHandler, 3, this));
                        break;
                    }
                }
                MLog.w("AccountCheckHandler", "onFinish, invalid params");
                return;
                break;
        }
    }
}
