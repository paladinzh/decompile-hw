package com.huawei.a.a;

import android.content.Context;
import android.os.Bundle;
import com.huawei.membercenter.sdk.api.MemberServiceAPI;
import com.huawei.membercenter.sdk.api.MemberServiceAPI.IActiveMemberCallback;

/* compiled from: MemberUtil */
public class a {
    public static void a(Context context, IActiveMemberCallback iActiveMemberCallback, Bundle bundle) {
        com.huawei.hwid.core.c.b.a.a("VipUtil", "activate Member");
        MemberServiceAPI.activeMember(bundle, context, iActiveMemberCallback);
    }
}
