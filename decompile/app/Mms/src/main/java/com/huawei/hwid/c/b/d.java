package com.huawei.hwid.c.b;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.datatype.TmemberRight;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;
import com.huawei.hwid.core.model.http.request.k;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: GetVIPForCurUserRequest */
class d extends c {
    CloudRequestHandler b;
    String c;
    private Context d;

    d(Context context, String str, CloudRequestHandler cloudRequestHandler) {
        super(context);
        this.d = context;
        this.c = str;
        this.b = cloudRequestHandler;
    }

    public void onSuccess(Bundle bundle) {
        boolean z;
        a.a("GetVIPForCurUserRequest", "enter GetAllDeviceRightsCallback::onFinish()");
        ArrayList parcelableArrayList = bundle.getParcelableArrayList("memberRights");
        UserInfo userInfo = (UserInfo) bundle.getParcelable("userInfo");
        if (userInfo != null) {
            com.huawei.hwid.core.b.a.a(this.a).b("last_head_picture_url", e.b(this.d, userInfo.getHeadPictureURL()));
        }
        Iterator it = parcelableArrayList.iterator();
        while (it.hasNext()) {
            TmemberRight tmemberRight = (TmemberRight) it.next();
            if (a(tmemberRight.a(), tmemberRight.b())) {
                com.huawei.hwid.c.a.a.a(this.d, this.c, tmemberRight.c(), tmemberRight.d());
                k.a(this.a.getApplicationContext(), tmemberRight.c(), this.b);
                z = true;
                break;
            }
        }
        z = false;
        a.b("GetVIPForCurUserRequest", "isFind is " + z);
        if (!z) {
            a.a("GetVIPForCurUserRequest", "not found right for this user, is not vip user!");
            com.huawei.hwid.c.a.a.a(this.d, this.c, -1, "");
        }
        if (this.b != null) {
            this.b.onFinish(bundle);
        }
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        a.d("GetVIPForCurUserRequest", "GetDevMemberCallBack execute error:" + f.a(errorStatus.getErrorReason()));
        if (this.b != null) {
            this.b.onError(errorStatus);
        }
    }

    private boolean a(String str, String str2) {
        String d = q.d(this.d);
        String f = q.f(this.d);
        if (!TextUtils.isEmpty(str)) {
            if (str.equalsIgnoreCase(d) || str.equalsIgnoreCase(f)) {
                return true;
            }
        }
        if (!TextUtils.isEmpty(str2)) {
            if (str2.equalsIgnoreCase(d) || str2.equalsIgnoreCase(f)) {
                return true;
            }
        }
        return false;
    }
}
