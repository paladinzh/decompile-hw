package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.DeviceInfo;
import com.huawei.hwid.core.datatype.TmemberRight;
import com.huawei.hwid.core.datatype.UserAccountInfo;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.datatype.UserLoginInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.i;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetUserInfoRequest */
public class q extends a {
    private String h;
    private String i;
    private UserInfo j;
    private UserLoginInfo k;
    private ArrayList l;
    private ArrayList m;
    private ArrayList n;
    private String o = (d() + "/IUserInfoMng/getUserInfo");
    private String p;
    private String q;
    private String r;
    private String s;

    public q() {
        b(true);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetUserInfoReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "userID", this.h);
            t.a(a, "queryRangeFlag", this.i);
            a.endTag(null, "GetUserInfoReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("userID", this.h);
            bundle.putString("queryRangeFlag", this.i);
            com.huawei.hwid.core.c.b.a.b("GetUserInfoRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetUserInfoRequest", e.toString());
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        int eventType = a.getEventType();
        Object obj = null;
        Object obj2 = null;
        Object obj3 = null;
        Object obj4 = null;
        Object obj5 = null;
        DeviceInfo deviceInfo = null;
        UserAccountInfo userAccountInfo = null;
        TmemberRight tmemberRight = null;
        while (1 != eventType) {
            TmemberRight tmemberRight2;
            UserAccountInfo userAccountInfo2;
            DeviceInfo deviceInfo2;
            Object obj6;
            Object obj7;
            Object obj8;
            String name = a.getName();
            DeviceInfo deviceInfo3;
            switch (eventType) {
                case 0:
                    tmemberRight2 = tmemberRight;
                    userAccountInfo2 = userAccountInfo;
                    deviceInfo2 = deviceInfo;
                    obj6 = obj5;
                    obj7 = obj4;
                    obj5 = obj3;
                    obj4 = obj2;
                    obj3 = obj;
                    break;
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b == 0) {
                        if (!"userID".equals(name) || obj5 != null) {
                            if (!"userInfo".equals(name)) {
                                if (obj == null) {
                                    if (!"userLoginInfo".equals(name)) {
                                        if (obj2 == null) {
                                            if (!"deviceIDList".equals(name)) {
                                                if (!"DeviceInfo".equals(name)) {
                                                    if (obj3 == null) {
                                                        if (!"userAcctInfoList".equals(name)) {
                                                            if (!"userAcctInfo".equals(name)) {
                                                                if (obj4 == null) {
                                                                    if (!"memberRightList".equals(name)) {
                                                                        if (!"memberRight".equals(name)) {
                                                                            if (obj5 != null) {
                                                                                com.huawei.hwid.core.helper.a.a.a(a, tmemberRight, name);
                                                                                tmemberRight2 = tmemberRight;
                                                                                userAccountInfo2 = userAccountInfo;
                                                                                deviceInfo2 = deviceInfo;
                                                                                obj6 = obj5;
                                                                                obj7 = obj4;
                                                                                obj5 = obj3;
                                                                                obj4 = obj2;
                                                                                obj3 = obj;
                                                                                break;
                                                                            }
                                                                            tmemberRight2 = tmemberRight;
                                                                            userAccountInfo2 = userAccountInfo;
                                                                            deviceInfo2 = deviceInfo;
                                                                            obj6 = obj5;
                                                                            obj7 = obj4;
                                                                            obj5 = obj3;
                                                                            obj4 = obj2;
                                                                            obj3 = obj;
                                                                            break;
                                                                        }
                                                                        tmemberRight2 = new TmemberRight();
                                                                        userAccountInfo2 = userAccountInfo;
                                                                        deviceInfo2 = deviceInfo;
                                                                        obj6 = obj5;
                                                                        obj7 = obj4;
                                                                        obj5 = obj3;
                                                                        obj4 = obj2;
                                                                        obj3 = obj;
                                                                        break;
                                                                    }
                                                                    this.n = new ArrayList();
                                                                    userAccountInfo2 = userAccountInfo;
                                                                    obj5 = obj3;
                                                                    int i = 1;
                                                                    obj3 = obj;
                                                                    tmemberRight2 = tmemberRight;
                                                                    deviceInfo2 = deviceInfo;
                                                                    obj7 = obj4;
                                                                    obj4 = obj2;
                                                                    break;
                                                                }
                                                                com.huawei.hwid.core.helper.a.a.a(a, userAccountInfo, name);
                                                                tmemberRight2 = tmemberRight;
                                                                userAccountInfo2 = userAccountInfo;
                                                                deviceInfo2 = deviceInfo;
                                                                obj6 = obj5;
                                                                obj7 = obj4;
                                                                obj5 = obj3;
                                                                obj4 = obj2;
                                                                obj3 = obj;
                                                                break;
                                                            }
                                                            userAccountInfo2 = new UserAccountInfo();
                                                            obj6 = obj5;
                                                            tmemberRight2 = tmemberRight;
                                                            obj5 = obj3;
                                                            deviceInfo2 = deviceInfo;
                                                            obj3 = obj;
                                                            obj7 = obj4;
                                                            obj4 = obj2;
                                                            break;
                                                        }
                                                        this.m = new ArrayList();
                                                        userAccountInfo2 = userAccountInfo;
                                                        obj4 = obj2;
                                                        obj6 = obj5;
                                                        obj5 = obj3;
                                                        obj3 = obj;
                                                        deviceInfo3 = deviceInfo;
                                                        int i2 = 1;
                                                        tmemberRight2 = tmemberRight;
                                                        deviceInfo2 = deviceInfo3;
                                                        break;
                                                    }
                                                    com.huawei.hwid.core.helper.a.a.a(a, deviceInfo, name);
                                                    tmemberRight2 = tmemberRight;
                                                    userAccountInfo2 = userAccountInfo;
                                                    deviceInfo2 = deviceInfo;
                                                    obj6 = obj5;
                                                    obj7 = obj4;
                                                    obj5 = obj3;
                                                    obj4 = obj2;
                                                    obj3 = obj;
                                                    break;
                                                }
                                                userAccountInfo2 = userAccountInfo;
                                                obj7 = obj4;
                                                obj6 = obj5;
                                                obj4 = obj2;
                                                obj5 = obj3;
                                                obj3 = obj;
                                                deviceInfo3 = new DeviceInfo();
                                                tmemberRight2 = tmemberRight;
                                                deviceInfo2 = deviceInfo3;
                                                break;
                                            }
                                            this.l = new ArrayList();
                                            userAccountInfo2 = userAccountInfo;
                                            obj3 = obj;
                                            obj6 = obj5;
                                            int i3 = 1;
                                            tmemberRight2 = tmemberRight;
                                            deviceInfo2 = deviceInfo;
                                            obj7 = obj4;
                                            obj4 = obj2;
                                            break;
                                        }
                                        com.huawei.hwid.core.helper.a.a.a(a, this.k, name);
                                        tmemberRight2 = tmemberRight;
                                        userAccountInfo2 = userAccountInfo;
                                        deviceInfo2 = deviceInfo;
                                        obj6 = obj5;
                                        obj7 = obj4;
                                        obj5 = obj3;
                                        obj4 = obj2;
                                        obj3 = obj;
                                        break;
                                    }
                                    this.k = new UserLoginInfo();
                                    userAccountInfo2 = userAccountInfo;
                                    obj6 = obj5;
                                    obj5 = obj3;
                                    obj3 = obj;
                                    obj8 = obj4;
                                    int i4 = 1;
                                    tmemberRight2 = tmemberRight;
                                    deviceInfo2 = deviceInfo;
                                    obj7 = obj8;
                                    break;
                                }
                                com.huawei.hwid.core.helper.a.a.a(a, this.j, name);
                                tmemberRight2 = tmemberRight;
                                userAccountInfo2 = userAccountInfo;
                                deviceInfo2 = deviceInfo;
                                obj6 = obj5;
                                obj7 = obj4;
                                obj5 = obj3;
                                obj4 = obj2;
                                obj3 = obj;
                                break;
                            }
                            this.j = new UserInfo();
                            userAccountInfo2 = userAccountInfo;
                            obj6 = obj5;
                            obj5 = obj3;
                            int i5 = 1;
                            tmemberRight2 = tmemberRight;
                            deviceInfo2 = deviceInfo;
                            obj7 = obj4;
                            obj4 = obj2;
                            break;
                        }
                        this.h = a.nextText();
                        tmemberRight2 = tmemberRight;
                        userAccountInfo2 = userAccountInfo;
                        deviceInfo2 = deviceInfo;
                        obj6 = obj5;
                        obj7 = obj4;
                        obj5 = obj3;
                        obj4 = obj2;
                        obj3 = obj;
                        break;
                    } else if (!"errorCode".equals(name)) {
                        if (!"errorDesc".equals(name)) {
                            tmemberRight2 = tmemberRight;
                            userAccountInfo2 = userAccountInfo;
                            deviceInfo2 = deviceInfo;
                            obj6 = obj5;
                            obj7 = obj4;
                            obj5 = obj3;
                            obj4 = obj2;
                            obj3 = obj;
                            break;
                        }
                        this.d = a.nextText();
                        tmemberRight2 = tmemberRight;
                        userAccountInfo2 = userAccountInfo;
                        deviceInfo2 = deviceInfo;
                        obj6 = obj5;
                        obj7 = obj4;
                        obj5 = obj3;
                        obj4 = obj2;
                        obj3 = obj;
                        break;
                    } else {
                        this.c = Integer.valueOf(a.nextText()).intValue();
                        tmemberRight2 = tmemberRight;
                        userAccountInfo2 = userAccountInfo;
                        deviceInfo2 = deviceInfo;
                        obj6 = obj5;
                        obj7 = obj4;
                        obj5 = obj3;
                        obj4 = obj2;
                        obj3 = obj;
                        break;
                    }
                    break;
                case 3:
                    if (!"userInfo".equals(name)) {
                        if (!"userLoginInfo".equals(name)) {
                            if (!"deviceIDList".equals(name)) {
                                if (!"DeviceInfo".equals(name)) {
                                    if (!"userAcctInfo".equals(name)) {
                                        if (!"userAcctInfoList".equals(name)) {
                                            if (!"memberRight".equals(name)) {
                                                if ("memberRightList".equals(name)) {
                                                    userAccountInfo2 = userAccountInfo;
                                                    obj5 = obj3;
                                                    obj6 = null;
                                                    obj3 = obj;
                                                    tmemberRight2 = tmemberRight;
                                                    deviceInfo2 = deviceInfo;
                                                    obj7 = obj4;
                                                    obj4 = obj2;
                                                    break;
                                                } else if (!"result".equals(name)) {
                                                    tmemberRight2 = tmemberRight;
                                                    userAccountInfo2 = userAccountInfo;
                                                    deviceInfo2 = deviceInfo;
                                                    obj6 = obj5;
                                                    obj7 = obj4;
                                                    obj5 = obj3;
                                                    obj4 = obj2;
                                                    obj3 = obj;
                                                    break;
                                                } else if (this.m != null) {
                                                    for (int i6 = 0; i6 < this.m.size(); i6++) {
                                                        UserAccountInfo userAccountInfo3 = (UserAccountInfo) this.m.get(i6);
                                                        if (userAccountInfo3 != null && "5".equals(userAccountInfo3.getAccountType())) {
                                                            this.p = userAccountInfo3.getAccountState();
                                                            this.q = userAccountInfo3.getUserAccount();
                                                        } else if (userAccountInfo3 != null && "6".equals(userAccountInfo3.getAccountType())) {
                                                            this.s = userAccountInfo3.getAccountState();
                                                            this.r = userAccountInfo3.getUserAccount();
                                                        }
                                                    }
                                                    tmemberRight2 = tmemberRight;
                                                    userAccountInfo2 = userAccountInfo;
                                                    deviceInfo2 = deviceInfo;
                                                    obj6 = obj5;
                                                    obj7 = obj4;
                                                    obj5 = obj3;
                                                    obj4 = obj2;
                                                    obj3 = obj;
                                                    break;
                                                } else {
                                                    return;
                                                }
                                            }
                                            this.n.add(tmemberRight);
                                            tmemberRight2 = tmemberRight;
                                            userAccountInfo2 = userAccountInfo;
                                            deviceInfo2 = deviceInfo;
                                            obj6 = obj5;
                                            obj7 = obj4;
                                            obj5 = obj3;
                                            obj4 = obj2;
                                            obj3 = obj;
                                            break;
                                        }
                                        userAccountInfo2 = userAccountInfo;
                                        obj4 = obj2;
                                        obj6 = obj5;
                                        obj5 = obj3;
                                        obj3 = obj;
                                        deviceInfo3 = deviceInfo;
                                        obj7 = null;
                                        tmemberRight2 = tmemberRight;
                                        deviceInfo2 = deviceInfo3;
                                        break;
                                    }
                                    this.m.add(userAccountInfo);
                                    tmemberRight2 = tmemberRight;
                                    userAccountInfo2 = userAccountInfo;
                                    deviceInfo2 = deviceInfo;
                                    obj6 = obj5;
                                    obj7 = obj4;
                                    obj5 = obj3;
                                    obj4 = obj2;
                                    obj3 = obj;
                                    break;
                                }
                                this.l.add(deviceInfo);
                                tmemberRight2 = tmemberRight;
                                userAccountInfo2 = userAccountInfo;
                                deviceInfo2 = deviceInfo;
                                obj6 = obj5;
                                obj7 = obj4;
                                obj5 = obj3;
                                obj4 = obj2;
                                obj3 = obj;
                                break;
                            }
                            userAccountInfo2 = userAccountInfo;
                            obj3 = obj;
                            obj6 = obj5;
                            obj5 = null;
                            tmemberRight2 = tmemberRight;
                            deviceInfo2 = deviceInfo;
                            obj7 = obj4;
                            obj4 = obj2;
                            break;
                        }
                        userAccountInfo2 = userAccountInfo;
                        obj6 = obj5;
                        obj5 = obj3;
                        obj3 = obj;
                        obj8 = obj4;
                        obj4 = null;
                        tmemberRight2 = tmemberRight;
                        deviceInfo2 = deviceInfo;
                        obj7 = obj8;
                        break;
                    }
                    userAccountInfo2 = userAccountInfo;
                    obj6 = obj5;
                    obj5 = obj3;
                    obj3 = null;
                    tmemberRight2 = tmemberRight;
                    deviceInfo2 = deviceInfo;
                    obj7 = obj4;
                    obj4 = obj2;
                    break;
                default:
                    tmemberRight2 = tmemberRight;
                    userAccountInfo2 = userAccountInfo;
                    deviceInfo2 = deviceInfo;
                    obj6 = obj5;
                    obj7 = obj4;
                    obj5 = obj3;
                    obj4 = obj2;
                    obj3 = obj;
                    break;
            }
            obj = obj3;
            obj3 = obj5;
            obj5 = obj6;
            userAccountInfo = userAccountInfo2;
            obj8 = obj7;
            deviceInfo = deviceInfo2;
            tmemberRight = tmemberRight2;
            eventType = a.next();
            obj2 = obj4;
            obj4 = obj8;
        }
    }

    public String g() {
        return this.o;
    }

    public void f(String str) {
        this.h = str;
    }

    public void g(String str) {
        this.i = str;
    }

    public UserInfo w() {
        return this.j;
    }

    public UserLoginInfo x() {
        return this.k;
    }

    public ArrayList y() {
        return this.l;
    }

    public ArrayList z() {
        return this.m;
    }

    public ArrayList A() {
        return this.n;
    }

    public String B() {
        return this.h;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putParcelableArrayList("accountsInfo", z());
        h.putParcelableArrayList("devicesInfo", y());
        h.putParcelableArrayList("memberRights", A());
        h.putParcelable("userInfo", w());
        h.putParcelable("userLoginInfo", x());
        return h;
    }

    public q(Context context, String str, String str2, Bundle bundle) {
        b(true);
        f(str);
        g(str2);
        a(true);
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        i.a(context, aVar, str, a(context, aVar, new r(context, cloudRequestHandler)));
    }
}
