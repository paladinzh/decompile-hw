package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;

/* compiled from: UIDVerifyPasswordRequest */
class ae extends c {
    private CloudRequestHandler b;

    public ae(Context context, CloudRequestHandler cloudRequestHandler) {
        super(context);
        this.b = cloudRequestHandler;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.onFinish(bundle);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        this.b.onError((ErrorStatus) bundle.getParcelable("requestError"));
    }
}
