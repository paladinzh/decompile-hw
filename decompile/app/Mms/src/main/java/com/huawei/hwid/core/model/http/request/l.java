package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;

/* compiled from: GetResourceRequest */
class l extends c {
    private Context b;
    private CloudRequestHandler c;

    public l(Context context, CloudRequestHandler cloudRequestHandler) {
        super(context);
        this.b = context;
        this.c = cloudRequestHandler;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        a.e("GetResourceRequest", "GetUserRightBaseUrlCallBack execute success");
        com.huawei.hwid.c.a.a.a(this.b, "curUserQueryRightUrl", bundle.getString("ResourceContent"));
        if (this.c != null) {
            this.c.onFinish(bundle);
        }
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        com.huawei.hwid.c.a.a.a(this.b);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        a.d("GetResourceRequest", "GetUserRightBaseUrlCallBack execute error:" + errorStatus.getErrorReason(), new Exception(errorStatus.getErrorReason()));
        if (this.c != null) {
            this.c.onError(errorStatus);
        }
    }
}
