package com.huawei.hwid.c.b;

import android.content.Context;
import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.q;

/* compiled from: GetVIPForCurUserRequest */
public class c extends q {
    public static void a(Context context, String str, String str2, CloudRequestHandler cloudRequestHandler) {
        a.a("GetVIPForCurUserRequest", "enter sendGetCurUserRightsRequest()");
        com.huawei.hwid.core.model.http.a cVar = new c(context, str, null);
        cVar.a(context, cVar, str2, cloudRequestHandler);
    }

    public c(Context context, String str, Bundle bundle) {
        super(context, str, "1000000001", bundle);
    }

    public void a(Context context, com.huawei.hwid.core.model.http.a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        a.a("GetVIPForCurUserRequest", "enter GetVIPForCurUserRequest::execute()");
        i.a(context, aVar, str, a(context, aVar, new d(context, B(), cloudRequestHandler)));
    }
}
