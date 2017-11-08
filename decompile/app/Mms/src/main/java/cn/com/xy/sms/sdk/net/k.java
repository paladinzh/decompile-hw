package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import java.net.HttpURLConnection;

/* compiled from: Unknown */
public final class k extends a {
    private int a = -1;

    public k(int i, String str, String str2, XyCallBack xyCallBack, boolean z) {
        super(str, null, str2, false, null, xyCallBack, true);
        this.a = i;
    }

    public final void setHttpHeader(c cVar, boolean z, String str, HttpURLConnection httpURLConnection) {
        super.setHttpHeader(cVar, z, str, httpURLConnection);
        httpURLConnection.addRequestProperty("sceneType", new StringBuilder(String.valueOf(this.a)).toString());
        httpURLConnection.addRequestProperty("reqVersion", "5.1.2");
        httpURLConnection.addRequestProperty("clientKey", l.b);
        httpURLConnection.addRequestProperty("client_key", "123456");
    }
}
