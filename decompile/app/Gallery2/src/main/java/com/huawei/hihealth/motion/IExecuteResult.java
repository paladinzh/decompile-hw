package com.huawei.hihealth.motion;

public interface IExecuteResult {
    void onFailed(Object obj);

    void onServiceException(Object obj);

    void onSuccess(Object obj);
}
