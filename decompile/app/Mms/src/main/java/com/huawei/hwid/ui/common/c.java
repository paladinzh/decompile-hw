package com.huawei.hwid.ui.common;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.manager.f;

/* compiled from: BaseActivity */
public class c extends com.huawei.hwid.core.helper.handler.c {
    final /* synthetic */ BaseActivity c;

    public c(BaseActivity baseActivity, Context context) {
        this.c = baseActivity;
        super(context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        if (bundle != null) {
            a.b("BaseActivity", "dispose Success msg ");
            if (this.c.e) {
                this.c.b();
            }
        }
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        if (bundle != null) {
            boolean z = bundle.getBoolean("isRequestSuccess", false);
            a.b("BaseActivity", "dispose onFail msg  isRequestSuccess:" + z);
            if (z && this.c.e) {
                this.c.b();
            } else if (!z) {
                Builder builder;
                ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
                this.c.b();
                if (errorStatus == null) {
                    builder = null;
                } else {
                    int errorCode = errorStatus.getErrorCode();
                    String errorReason = errorStatus.getErrorReason();
                    boolean z2 = bundle.getBoolean("finishActivity", false);
                    if (NearbyPoint.QUERY_PARAM_ERROR == errorCode) {
                        builder = j.a(this.a, m.a(this.c, "CS_notification"), errorReason, z2);
                    } else if (NearbyPoint.QUERY_RESULT_RECEIVE == errorCode) {
                        builder = j.a(this.a, m.a(this.c, "CS_server_unavailable_message"), m.a(this.c, "CS_server_unavailable_title"), z2);
                    } else if (NearbyPoint.GET_QUERY_URL_FAILURE != errorCode) {
                        builder = 70002044 != errorCode ? j.a(this.a, errorReason, z2) : j.b(this.a, errorReason, z2);
                    } else {
                        j.a(this.a, this.c.getString(m.a(this.c, "CS_account_change")), 1);
                        f.a(this.c).a(this.c, this.c.d(), null, new d(this));
                        builder = null;
                    }
                }
                this.c.g();
                if (builder != null) {
                    this.c.a(builder.show());
                }
            }
        }
    }
}
