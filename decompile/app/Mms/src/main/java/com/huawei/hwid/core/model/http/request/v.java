package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.a.b;
import com.huawei.hwid.core.a.d;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;

/* compiled from: OpLogRequest */
public class v extends c {
    private Context b;

    public v(Context context) {
        super(context);
        this.b = context;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        a.e("OpLogRequest", "upload log success");
        b.a(this.b).a();
        d.a(0);
        d.a(this.b);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            a.d("OpLogRequest", "OpLogUploadHelper execute error:" + f.a(errorStatus.getErrorReason()));
        }
        d.a(0);
        d.a(this.b);
    }
}
