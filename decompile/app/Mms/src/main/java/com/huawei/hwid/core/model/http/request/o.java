package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;
import java.util.ArrayList;

/* compiled from: GetSMSCountryRequest */
class o extends c {
    public o(Context context) {
        super(context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        a.b("GetSMSCountryRequest", "GetCountryList execute success");
        ArrayList parcelableArrayList = bundle.getParcelableArrayList("smsCountryList");
        if (parcelableArrayList != null && !parcelableArrayList.isEmpty()) {
            k.a(parcelableArrayList);
        }
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            a.d("GetSMSCountryRequest", "GetCountryList execute error:" + f.a(errorStatus.getErrorReason()));
        }
    }
}
