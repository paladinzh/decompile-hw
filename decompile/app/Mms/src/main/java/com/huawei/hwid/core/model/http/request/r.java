package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.datatype.UserLoginInfo;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;
import java.util.ArrayList;

/* compiled from: GetUserInfoRequest */
class r extends c {
    private CloudRequestHandler b;
    private Context c;

    public r(Context context, CloudRequestHandler cloudRequestHandler) {
        super(context);
        this.b = cloudRequestHandler;
        this.c = context;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        if (!"com.huawei.hwid".equals(this.c.getPackageName())) {
            d.g("getUserInfo");
        }
        UserInfo userInfo = (UserInfo) bundle.getParcelable("userInfo");
        UserLoginInfo userLoginInfo = (UserLoginInfo) bundle.getParcelable("userLoginInfo");
        ArrayList parcelableArrayList = bundle.getParcelableArrayList("devicesInfo");
        ArrayList parcelableArrayList2 = bundle.getParcelableArrayList("accountsInfo");
        ArrayList parcelableArrayList3 = bundle.getParcelableArrayList("memberRights");
        Bundle bundle2 = new Bundle();
        bundle2.putParcelableArrayList("userAccountInfo", parcelableArrayList2);
        bundle2.putParcelableArrayList("deviceInfo", parcelableArrayList);
        bundle2.putParcelable("userInfo", userInfo);
        bundle2.putParcelable("userLoginInfo", userLoginInfo);
        bundle2.putParcelableArrayList("memberRights", parcelableArrayList3);
        this.b.onFinish(bundle2);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        if (!"com.huawei.hwid".equals(this.c.getPackageName())) {
            d.g("getUserInfo");
        }
        this.b.onError((ErrorStatus) bundle.getParcelable("requestError"));
    }
}
