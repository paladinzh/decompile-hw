package com.huawei.hwid.api.common;

import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

class e implements CloudRequestHandler {
    final /* synthetic */ d a;

    e(d dVar) {
        this.a = dVar;
    }

    public void onFinish(Bundle bundle) {
        new f(this).start();
    }

    public void onError(ErrorStatus errorStatus) {
        com.huawei.hwid.core.d.b.e.d("CloudAccountImpl", "getUserInfo onError, ErrorCode: " + errorStatus.getErrorCode() + ", ErrorReason: " + errorStatus.getErrorReason());
        this.a.f.onError(errorStatus);
    }
}
