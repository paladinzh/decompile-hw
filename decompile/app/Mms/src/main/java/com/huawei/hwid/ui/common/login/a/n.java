package com.huawei.hwid.ui.common.login.a;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.c;

/* compiled from: PwdDialogFragment */
class n extends c {
    final /* synthetic */ a b;

    public n(a aVar, Context context) {
        this.b = aVar;
        BaseActivity baseActivity = (BaseActivity) context;
        baseActivity.getClass();
        super(baseActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        a.b("PwdDialogFragment", "onSuccess");
        super.onSuccess(bundle);
        this.b.a(this.b.c, true);
        if (this.b.a != null) {
            this.b.a.a(this.b.a(bundle));
        }
        this.b.o.a(d.a());
        com.huawei.hwid.core.a.d.a(this.b.o, this.b.getActivity());
    }

    public void onFail(Bundle bundle) {
        this.b.a(this.b.c, true);
        boolean z = bundle.getBoolean("isRequestSuccess", false);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            if (z) {
                if (errorStatus.getErrorCode() == 70002003 || errorStatus.getErrorCode() == 70001201) {
                    this.b.b.setError(this.b.getString(m.a(this.b.getActivity(), "CS_error_login_pwd_message")));
                    this.b.b.selectAll();
                } else if (errorStatus.getErrorCode() == 70002058) {
                    this.b.d();
                }
            }
            this.b.o.a(d.a());
            this.b.o.c(String.valueOf(errorStatus.getErrorCode()));
            this.b.o.d(errorStatus.getErrorReason());
            com.huawei.hwid.core.a.d.a(this.b.o, this.b.getActivity());
        }
        super.onFail(bundle);
    }
}
