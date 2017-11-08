package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.EmailInfo;
import com.huawei.hwid.core.datatype.PhoneNumInfo;
import com.huawei.hwid.core.datatype.UserAccountInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetUserAcctInfoRequest */
public class p extends a {
    private String h;
    private String i;
    private String j = (d() + "/IUserInfoMng/getUserAcctInfo");
    private String k;
    private String l;
    private String m = "7";
    private ArrayList n;
    private ArrayList o = new ArrayList();
    private ArrayList p = new ArrayList();
    private String q;
    private String r;
    private String s;
    private String t;
    private Context u;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XmlSerializer a = t.a(byteArrayOutputStream);
        a.startDocument("UTF-8", Boolean.valueOf(true));
        a.startTag(null, "GetUserAcctInfoReq");
        t.a(a, NumberInfo.VERSION_KEY, "10000");
        t.a(a, "accountType", this.k);
        t.a(a, "userAccount", this.l);
        t.a(a, "reqClientType", this.m);
        t.a(a, "plmn", this.i);
        a.endTag(null, "GetUserAcctInfoReq");
        a.endDocument();
        String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
        Bundle bundle = new Bundle();
        bundle.putString(NumberInfo.VERSION_KEY, "10000");
        bundle.putString("accountType", this.k);
        bundle.putString("userAccount", f.c(this.l));
        bundle.putString("reqClientType", this.m);
        bundle.putString("plmn", this.i);
        com.huawei.hwid.core.c.b.a.b("GetUserAcctInfoRequest", "packedString:" + f.a(bundle));
        return byteArrayOutputStream2;
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        UserAccountInfo userAccountInfo = null;
        Object obj = null;
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            UserAccountInfo userAccountInfo2;
            Object obj2;
            String name = a.getName();
            int i;
            switch (eventType) {
                case 0:
                    userAccountInfo2 = userAccountInfo;
                    obj2 = obj;
                    break;
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b == 0) {
                        if (!"userAcctInfoList".equals(name)) {
                            if (!"userAcctInfo".equals(name)) {
                                if (obj == null) {
                                    if (!"userID".equals(name)) {
                                        userAccountInfo2 = userAccountInfo;
                                        obj2 = obj;
                                        break;
                                    }
                                    this.h = a.nextText();
                                    userAccountInfo2 = userAccountInfo;
                                    obj2 = obj;
                                    break;
                                }
                                com.huawei.hwid.core.helper.a.a.a(a, userAccountInfo, name);
                                userAccountInfo2 = userAccountInfo;
                                obj2 = obj;
                                break;
                            }
                            userAccountInfo2 = new UserAccountInfo();
                            obj2 = obj;
                            break;
                        }
                        this.n = new ArrayList();
                        userAccountInfo2 = userAccountInfo;
                        i = 1;
                        break;
                    } else if (!"errorCode".equals(name)) {
                        if (!"errorDesc".equals(name)) {
                            userAccountInfo2 = userAccountInfo;
                            obj2 = obj;
                            break;
                        }
                        this.d = a.nextText();
                        userAccountInfo2 = userAccountInfo;
                        obj2 = obj;
                        break;
                    } else {
                        this.c = Integer.valueOf(a.nextText()).intValue();
                        userAccountInfo2 = userAccountInfo;
                        obj2 = obj;
                        break;
                    }
                case 3:
                    if (!"userAcctInfo".equals(name)) {
                        if ("userAcctInfoList".equals(name)) {
                            userAccountInfo2 = userAccountInfo;
                            obj2 = null;
                            break;
                        } else if (!"result".equals(name)) {
                            userAccountInfo2 = userAccountInfo;
                            obj2 = obj;
                            break;
                        } else if (this.n != null) {
                            for (i = 0; i < this.n.size(); i++) {
                                int i2;
                                userAccountInfo2 = (UserAccountInfo) this.n.get(i);
                                if (userAccountInfo2 != null) {
                                    if ("5".equals(userAccountInfo2.getAccountType()) || "1".equals(userAccountInfo2.getAccountType())) {
                                        this.q = userAccountInfo2.getAccountState();
                                        this.r = userAccountInfo2.getUserAccount();
                                        EmailInfo emailInfo = new EmailInfo(this.r, this.q);
                                        if (this.o.isEmpty()) {
                                            this.o.add(emailInfo);
                                        } else {
                                            for (i2 = 0; i2 < this.o.size(); i2++) {
                                                if (!((EmailInfo) this.o.get(i2)).a().equals(emailInfo.a())) {
                                                    this.o.add(emailInfo);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (userAccountInfo2 != null) {
                                    if ("6".equals(userAccountInfo2.getAccountType()) || "2".equals(userAccountInfo2.getAccountType())) {
                                        this.t = userAccountInfo2.getAccountState();
                                        this.s = userAccountInfo2.getUserAccount();
                                        PhoneNumInfo phoneNumInfo = new PhoneNumInfo(this.u, this.s, this.t);
                                        if (this.p.isEmpty()) {
                                            this.p.add(phoneNumInfo);
                                        } else {
                                            for (i2 = 0; i2 < this.p.size(); i2++) {
                                                if (!((PhoneNumInfo) this.p.get(i2)).b().equals(phoneNumInfo.b())) {
                                                    this.p.add(phoneNumInfo);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            userAccountInfo2 = userAccountInfo;
                            obj2 = obj;
                            break;
                        } else {
                            return;
                        }
                    }
                    this.n.add(userAccountInfo);
                    userAccountInfo2 = userAccountInfo;
                    obj2 = obj;
                    break;
                    break;
                default:
                    userAccountInfo2 = userAccountInfo;
                    obj2 = obj;
                    break;
            }
            userAccountInfo = userAccountInfo2;
            obj = obj2;
        }
    }

    public String g() {
        return this.j;
    }

    public void f(String str) {
        this.k = str;
    }

    public void g(String str) {
        this.l = str;
    }

    public void h(String str) {
        this.i = str;
    }

    public ArrayList w() {
        return this.p;
    }

    public ArrayList x() {
        return this.o;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putParcelableArrayList("securityEmail", x());
        h.putParcelableArrayList("securityPhone", w());
        h.putParcelableArrayList("accountsInfo", this.n);
        h.putString("userID", this.h);
        return h;
    }

    public p(Context context, String str, int i) {
        this.u = context;
        g(str);
        h(q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL));
        c(i);
        f(d.b(str));
        a(true);
    }

    public p(Context context, String str) {
        this.u = context;
        g(str);
        h(q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL));
        f(d.b(str));
        a(true);
    }
}
