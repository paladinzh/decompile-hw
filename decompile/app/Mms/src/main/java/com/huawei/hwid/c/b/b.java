package com.huawei.hwid.c.b;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;

/* compiled from: GetDevMemberRequest */
class b extends c {
    private CloudRequestHandler b;

    public b(Context context, CloudRequestHandler cloudRequestHandler) {
        super(context);
        this.a = context;
        this.b = cloudRequestHandler;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        a.e("GetDevMemberRequest", "GetDevMemberCallBack execute success");
        Object string = bundle.getString("userID");
        if (TextUtils.isEmpty(string)) {
            a.a("GetDevMemberRequest", "userId is null, maybe error!!");
            if (this.b != null) {
                this.b.onError(new ErrorStatus(16, "when get bindUserId, rsp is empty"));
                return;
            }
            return;
        }
        if (ThemeUtil.SET_NULL_STR.equals(string)) {
            a.a("GetDevMemberRequest", "no user bind this devices!");
        } else {
            a.a("GetDevMemberRequest", "some user bind this devices!");
        }
        com.huawei.hwid.c.a.a.b(this.a, string, bundle.getInt("rightsID"), bundle.getString("vipExpiredDate"));
        if (ThemeUtil.SET_NULL_STR.equals(string)) {
            d.m(this.a);
            d.b(this.a, 10011);
        }
        if (this.b != null) {
            this.b.onFinish(bundle);
        }
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        a.d("GetDevMemberRequest", "GetDevMemberCallBack execute error:" + f.a(errorStatus.getErrorReason()) + ", desc: " + f.a(errorStatus.getErrorReason()));
        if (this.b != null) {
            this.b.onError(errorStatus);
        }
    }
}
