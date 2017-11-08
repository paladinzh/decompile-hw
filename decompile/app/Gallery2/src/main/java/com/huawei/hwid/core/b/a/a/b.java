package com.huawei.hwid.core.b.a.a;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.b.a.d;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.h;
import com.huawei.hwid.core.d.p;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class b extends com.huawei.hwid.core.b.a.a {
    private String h = (d() + "/IUserInfoMng/opLog");
    private String i;

    public static class a extends com.huawei.hwid.core.helper.handler.b {
        private Context a;

        public a(Context context) {
            super(context);
            this.a = context;
        }

        public void a(Bundle bundle) {
            super.a(bundle);
            e.e("OpLogRequest", "upload log success");
            com.huawei.hwid.core.a.a.a(this.a).a();
            c.a(0);
            c.a(this.a);
        }

        public void b(Bundle bundle) {
            super.b(bundle);
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                e.d("OpLogRequest", "OpLogUploadHelper execute error:" + f.a(errorStatus.getErrorReason()));
            }
            c.a(0);
            c.a(this.a);
        }
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        return this.i;
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = p.a(str.getBytes(XmlUtils.INPUT_ENCODING));
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            String name = a.getName();
            switch (eventType) {
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
                    break;
                default:
                    break;
            }
        }
    }

    public String g() {
        return this.h;
    }

    public Bundle h() {
        return super.h();
    }

    public b(String str) {
        d(1);
        this.i = str;
    }

    public void a(Context context, com.huawei.hwid.core.b.a.a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        if (aVar.r() <= 0) {
            str = com.huawei.hwid.core.d.b.n(context);
            e.a("OpLogRequest", "OpLogRequest currName" + f.a(str, true));
            if (TextUtils.isEmpty(str)) {
                aVar.c(h.a(context));
            } else {
                e.a("OpLogRequest", "has alreacdy accountName logined in");
            }
        }
        d.a(context.getApplicationContext(), aVar, str, a(context, aVar, new a(context)));
    }
}
