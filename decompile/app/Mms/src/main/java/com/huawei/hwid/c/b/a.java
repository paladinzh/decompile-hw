package com.huawei.hwid.c.b;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.datatype.n;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.e;
import com.huawei.hwid.core.model.http.i;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: GetDevMemberRequest */
public class a extends com.huawei.hwid.core.model.http.a {
    String h = "";
    String i = "";
    String j = "";
    String k = "";
    String l = "";
    private String m = (d() + "/IUserDeviceMng/getDevMember");
    private n n = null;
    private String o = ThemeUtil.SET_NULL_STR;
    private int p = -1;
    private String q = "";

    protected String f() {
        if (this.n.a()) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("ver=").append("10000").append("&sc=").append(this.n.d()).append("&emID=").append(this.n.e()).append("&dvT=").append(this.h).append("&dvID=").append(this.i).append("&tmT=").append(this.k).append("&C=").append(this.n.c()).append("&app=").append(this.l);
            if (!(TextUtils.isEmpty(this.j) || "NULL".equals(this.j))) {
                stringBuffer.append("&").append("dvID2=").append(this.j);
            }
            Bundle bundle = new Bundle();
            bundle.putString("sc", this.n.d());
            bundle.putString("emID", this.n.e());
            bundle.putString("dvT", this.h);
            bundle.putString("dvID", this.i);
            bundle.putString("tmT", this.k);
            bundle.putString("C", this.n.c());
            bundle.putString("app", this.l);
            if (!(TextUtils.isEmpty(this.j) || "NULL".equals(this.j))) {
                bundle.putString("dvID2", this.j);
            }
            com.huawei.hwid.core.c.b.a.a("GetDevMemberRequest", "urlEncode=" + f.a(bundle));
            return stringBuffer.toString();
        }
        com.huawei.hwid.core.c.b.a.d("GetDevMemberRequest", "when call GetDevMemberRequest, vip is not support!");
        return "";
    }

    protected void b(String str) {
        String str2;
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("&");
            HashMap hashMap = new HashMap();
            Object obj = "";
            int length = split.length;
            int i = 0;
            while (i < length) {
                Object obj2;
                str2 = split[i];
                com.huawei.hwid.core.c.b.a.e("GetDevMemberRequest", "infolist item:" + f.a(str2));
                String[] split2 = str2.split("=");
                if (split2.length <= 1) {
                    obj2 = obj;
                } else {
                    hashMap.put(split2[0], split2[1]);
                    obj2 = split2[0];
                }
                i++;
                obj = obj2;
            }
            if (hashMap.containsKey("resultCode")) {
                this.b = Integer.valueOf((String) hashMap.get("resultCode")).intValue();
            }
            if (this.b != 0) {
                this.c = this.b;
                this.d = (String) hashMap.get(obj);
                com.huawei.hwid.core.c.b.a.e("GetDevMemberRequest", "mErrorCode:" + this.c + ",mErrorDesc:" + f.a(this.d));
            } else {
                this.o = (String) hashMap.get("userID");
                if (n.g()) {
                    str2 = (String) hashMap.get("rightsID");
                    try {
                        this.p = Integer.parseInt(str2);
                    } catch (Exception e) {
                        com.huawei.hwid.core.c.b.a.d("TGC", "pares mRightsID:" + str2 + ", err:" + e.toString());
                    }
                    this.q = (String) hashMap.get("expiredDate");
                }
            }
        }
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("userID", this.o);
        if (n.g()) {
            h.putInt("rightsID", this.p);
            h.putString("vipExpiredDate", this.q);
        }
        return h;
    }

    public a(Context context, Bundle bundle) {
        this.h = q.a(context);
        this.k = q.a();
        this.n = new n(context);
        this.l = context.getPackageName();
        this.i = q.d(context);
        this.j = q.f(context);
        a(e.URLType);
        d(3);
    }

    public static void a(Context context, CloudRequestHandler cloudRequestHandler) {
        com.huawei.hwid.core.c.b.a.a("GetDevMemberRequest", "enter sendGetDevMemberRequest");
        try {
            com.huawei.hwid.core.model.http.a aVar = new a(context, null);
            i.a(context, aVar, null, aVar.a(context, aVar, new b(context, cloudRequestHandler)));
        } catch (Throwable e) {
            com.huawei.hwid.core.c.b.a.d("GetDevMemberRequest", e.toString(), e);
        }
    }

    public String g() {
        return this.m;
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        com.huawei.hwid.core.c.b.a.d("GetDevMemberRequest", "unSupport GetDevMemberRequest::pack()");
        return null;
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        com.huawei.hwid.core.c.b.a.d("GetDevMemberRequest", "unSupport GetDevMemberRequest::unPack()");
    }
}
