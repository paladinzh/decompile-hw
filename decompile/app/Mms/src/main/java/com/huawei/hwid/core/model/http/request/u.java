package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.i;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: OpLogRequest */
public class u extends a {
    private String h = (d() + "/IUserInfoMng/opLog");
    private String i;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        return this.i;
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
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

    public u(String str) {
        d(1);
        this.i = str;
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        i.a(context.getApplicationContext(), aVar, null, a(context, aVar, new v(context)));
    }
}
