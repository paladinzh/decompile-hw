package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;

/* compiled from: FindpwdByHwIdActivity */
class d extends c {
    final /* synthetic */ FindpwdByHwIdActivity b;

    public d(FindpwdByHwIdActivity findpwdByHwIdActivity, Context context) {
        this.b = findpwdByHwIdActivity;
        super(findpwdByHwIdActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        if ("0".equals(bundle.getString("isAccountExist"))) {
            this.b.b();
            this.b.a.setError(this.b.getString(m.a(this.b, "CS_username_not_exist")));
            this.b.a.requestFocus();
            this.b.a.selectAll();
            this.b.b();
            return;
        }
        this.b.a(bundle);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                if (70001201 == errorStatus.getErrorCode() || 70002001 == errorStatus.getErrorCode()) {
                    this.b.a.setError(this.b.getString(m.a(this.b, "CS_username_not_exist")));
                    this.b.a.requestFocus();
                    this.b.a.selectAll();
                }
            }
        }
        this.b.b();
        super.onFail(bundle);
    }
}
