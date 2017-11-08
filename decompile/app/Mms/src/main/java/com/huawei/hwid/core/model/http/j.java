package com.huawei.hwid.core.model.http;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.membercenter.sdk.api.MemberServiceAPI.IQueryMemberStatusCallback;
import com.huawei.membercenter.sdk.api.model.MemberStatus;

/* compiled from: RequestManager */
final class j implements IQueryMemberStatusCallback {
    final /* synthetic */ Bundle a;
    final /* synthetic */ Context b;

    j(Bundle bundle, Context context) {
        this.a = bundle;
        this.b = context;
    }

    public void callback(String str, String str2, MemberStatus memberStatus) {
        a.b("RequestManager", "queryMemberStatus retCode = " + str);
        if (memberStatus != null) {
            if ("0".equals(str) || "300001".equals(str) || "L300001".equals(str)) {
                a.a("RequestManager", "member status is:" + memberStatus.getMemLevel());
                this.a.putInt("rightsID", memberStatus.getMemLevel());
                com.huawei.hwid.c.a.a(this.b, this.a);
            }
            return;
        }
        a.b("RequestManager", "member status is null err");
    }
}
