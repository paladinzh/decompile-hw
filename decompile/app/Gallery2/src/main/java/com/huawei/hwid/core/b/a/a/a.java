package com.huawei.hwid.core.b.a.a;

import android.content.Context;
import android.os.Bundle;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.b.a.d;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.p;
import com.huawei.hwid.core.datatype.ChildrenInfo;
import com.huawei.hwid.core.datatype.DeviceInfo;
import com.huawei.hwid.core.datatype.TmemberRight;
import com.huawei.hwid.core.datatype.UserAccountInfo;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.datatype.UserLoginInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.b;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class a extends com.huawei.hwid.core.b.a.a {
    private String A = (d() + "/IUserInfoMng/getUserInfo");
    private String B;
    private String C;
    private String D;
    private String E;
    int h;
    boolean i;
    boolean j;
    boolean k;
    boolean l;
    boolean m;
    boolean n;
    DeviceInfo o;
    UserAccountInfo p;
    TmemberRight q;
    ChildrenInfo r;
    private String s;
    private String t;
    private UserInfo u;
    private UserLoginInfo v;
    private ArrayList<DeviceInfo> w;
    private ArrayList<UserAccountInfo> x;
    private ArrayList<TmemberRight> y;
    private ArrayList<ChildrenInfo> z;

    static class a extends b {
        private CloudRequestHandler a;
        private Context c;

        public a(Context context, CloudRequestHandler cloudRequestHandler) {
            super(context);
            this.a = cloudRequestHandler;
            this.c = context;
        }

        public void a(Bundle bundle) {
            super.a(bundle);
            if (!"com.huawei.hwid".equals(this.c.getPackageName())) {
                com.huawei.hwid.core.d.b.e("getUserInfo");
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
            this.a.onFinish(bundle2);
        }

        public void b(Bundle bundle) {
            super.b(bundle);
            if (!"com.huawei.hwid".equals(this.c.getPackageName())) {
                com.huawei.hwid.core.d.b.e("getUserInfo");
            }
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus == null) {
                this.a.onError(new ErrorStatus(32, "ErrorStatus is null"));
            } else {
                this.a.onError(errorStatus);
            }
        }
    }

    public a() {
        b(true);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = p.a(byteArrayOutputStream);
            a.startDocument(XmlUtils.INPUT_ENCODING, Boolean.valueOf(true));
            a.startTag(null, "GetUserInfoReq");
            p.a(a, "version", "10002");
            p.a(a, "userID", this.s);
            p.a(a, "queryRangeFlag", this.t);
            a.endTag(null, "GetUserInfoReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString(XmlUtils.INPUT_ENCODING);
            Bundle bundle = new Bundle();
            bundle.putString("version", "10002");
            bundle.putString("userID", this.s);
            bundle.putString("queryRangeFlag", this.t);
            e.b("GetUserInfoRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.d("GetUserInfoRequest", e.getMessage());
            }
        }
    }

    private void a(String str, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if ("userID".equals(str) && !this.m) {
            this.s = xmlPullParser.nextText();
        } else if ("userInfo".equals(str)) {
            this.u = new UserInfo();
            this.i = true;
        } else if (this.i) {
            UserInfo.getUserInfoIntag(xmlPullParser, this.u, str);
        } else if ("userLoginInfo".equals(str)) {
            this.v = new UserLoginInfo();
            this.j = true;
        } else if (this.j) {
            UserLoginInfo.getUserLoginInfoInTag(xmlPullParser, this.v, str);
        } else if ("deviceIDList".equals(str)) {
            this.w = new ArrayList();
            this.k = true;
        } else if ("DeviceInfo".equals(str)) {
            this.o = new DeviceInfo();
        } else if (this.k) {
            DeviceInfo.getDeviceInfoInTag(xmlPullParser, this.o, str);
        } else if ("userAcctInfoList".equals(str)) {
            this.x = new ArrayList();
            this.l = true;
        } else {
            b(str, xmlPullParser);
        }
    }

    private void b(String str, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if ("userAcctInfo".equals(str)) {
            this.p = new UserAccountInfo();
        } else if (this.l) {
            UserAccountInfo.getUserAccInfoInTag(xmlPullParser, this.p, str);
        } else if ("memberRightList".equals(str)) {
            this.y = new ArrayList();
            this.m = true;
        } else if ("memberRight".equals(str)) {
            this.q = new TmemberRight();
        } else if (this.m) {
            TmemberRight.a(xmlPullParser, this.q, str);
        } else if ("childrenList".equals(str)) {
            this.z = new ArrayList();
            this.n = true;
        } else {
            ChildrenInfo childrenInfo = this.r;
            if ("children".equals(str)) {
                this.r = new ChildrenInfo();
            } else if (this.n) {
                ChildrenInfo.a(xmlPullParser, this.r, str);
            }
        }
    }

    private void c(String str, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if ("userInfo".equals(str)) {
            this.i = false;
        } else if ("userLoginInfo".equals(str)) {
            this.j = false;
        } else if ("deviceIDList".equals(str)) {
            this.k = false;
        } else if ("DeviceInfo".equals(str)) {
            this.w.add(this.o);
        } else if ("userAcctInfo".equals(str)) {
            this.x.add(this.p);
        } else if ("userAcctInfoList".equals(str)) {
            this.l = false;
        } else if ("memberRight".equals(str)) {
            this.y.add(this.q);
        } else if ("memberRightList".equals(str)) {
            this.m = false;
        } else if ("children".equals(str)) {
            this.z.add(this.r);
        } else if ("result".equals(str)) {
            A();
        }
    }

    private void A() {
        if (this.x != null) {
            for (int i = 0; i < this.x.size(); i++) {
                UserAccountInfo userAccountInfo = (UserAccountInfo) this.x.get(i);
                if (userAccountInfo != null && "5".equals(userAccountInfo.getAccountType())) {
                    this.B = userAccountInfo.getAccountState();
                    this.C = userAccountInfo.getUserAccount();
                } else if (userAccountInfo != null && "6".equals(userAccountInfo.getAccountType())) {
                    this.E = userAccountInfo.getAccountState();
                    this.D = userAccountInfo.getUserAccount();
                }
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = p.a(str.getBytes(XmlUtils.INPUT_ENCODING));
        this.h = a.getEventType();
        this.i = false;
        this.j = false;
        this.k = false;
        this.l = false;
        this.m = false;
        this.n = false;
        this.o = null;
        this.p = null;
        this.q = null;
        this.r = null;
        while (1 != this.h) {
            String name = a.getName();
            switch (this.h) {
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b != 0) {
                        if (!"errorCode".equals(name)) {
                            if (!"errorDesc".equals(name)) {
                                break;
                            }
                            this.d = a.nextText();
                            break;
                        }
                        this.c = Integer.valueOf(a.nextText()).intValue();
                        break;
                    }
                    a(name, a);
                    break;
                case 3:
                    c(name, a);
                    break;
                default:
                    break;
            }
            this.h = a.next();
        }
    }

    public String g() {
        return this.A;
    }

    public void e(String str) {
        this.s = str;
    }

    public void f(String str) {
        this.t = str;
    }

    public UserInfo u() {
        return this.u;
    }

    public UserLoginInfo v() {
        return this.v;
    }

    public ArrayList<DeviceInfo> w() {
        return this.w;
    }

    public ArrayList<UserAccountInfo> x() {
        return this.x;
    }

    public ArrayList<TmemberRight> y() {
        return this.y;
    }

    public ArrayList<ChildrenInfo> z() {
        return this.z;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putParcelableArrayList("accountsInfo", x());
        h.putParcelableArrayList("devicesInfo", w());
        h.putParcelableArrayList("memberRights", y());
        h.putParcelableArrayList("childrenInfo", z());
        h.putParcelable("userInfo", u());
        h.putParcelable("userLoginInfo", v());
        return h;
    }

    public a(Context context, String str, String str2, Bundle bundle) {
        b(true);
        e(str);
        f(str2);
        a(true);
    }

    public void a(Context context, com.huawei.hwid.core.b.a.a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        d.a(context, aVar, str, a(context, aVar, new a(context, cloudRequestHandler)));
    }
}
