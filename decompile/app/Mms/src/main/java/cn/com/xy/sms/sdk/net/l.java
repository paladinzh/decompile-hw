package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.util.m;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.u;
import cn.com.xy.sms.util.w;
import java.net.HttpURLConnection;

/* compiled from: Unknown */
public final class l extends a {
    public static String a = null;
    public static String b = null;
    private static String c = "HTTP";
    private String d;

    public l(String str, String str2, String str3, String str4, boolean z, XyCallBack xyCallBack, Boolean bool) {
        super(str, null, str2, z, str4, xyCallBack, bool.booleanValue());
        this.d = str3;
    }

    public final void setHttpHeader(c cVar, boolean z, String str, HttpURLConnection httpURLConnection) {
        String stringParam;
        httpURLConnection.addRequestProperty("Content-Type", "text/xml;UTF-8");
        if (!StringUtils.isNull(this.d)) {
            httpURLConnection.addRequestProperty(IccidInfoManager.CNUM, this.d);
        }
        String str2 = b;
        if (z) {
            httpURLConnection.addRequestProperty("command", "2");
        } else {
            stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.HTTPTOKEN);
            httpURLConnection.addRequestProperty("command", "1");
            if (!StringUtils.isNull(stringParam)) {
                str2 = new StringBuilder(String.valueOf(str2)).append(stringParam).toString();
                httpURLConnection.addRequestProperty(NetUtil.REQ_QUERY_TOEKN, stringParam);
            }
        }
        str2 = m.a(a, str2);
        httpURLConnection.addRequestProperty("app-key", b);
        httpURLConnection.addRequestProperty("app-key-sign", str2);
        httpURLConnection.addRequestProperty("compress", "1");
        httpURLConnection.addRequestProperty("loginid", "");
        httpURLConnection.addRequestProperty("recordState", u.a());
        httpURLConnection.addRequestProperty("sdkversion", NetUtil.APPVERSION);
        httpURLConnection.addRequestProperty("abi", a.a());
        httpURLConnection.addRequestProperty("uiversion", DexUtil.getUIVersion());
        stringParam = w.d();
        String c = w.c();
        str2 = DexUtil.getOnLineConfigureData(4);
        if (StringUtils.isNull(str2)) {
            str2 = "bizport.cn/66dc91e8b78b1c284027a3eb1be0a70e";
        }
        httpURLConnection.addRequestProperty("ai", m.a(stringParam));
        httpURLConnection.addRequestProperty("ni", m.a(c));
        httpURLConnection.addRequestProperty("referer", str2);
        if (LogManager.debug) {
            httpURLConnection.getRequestProperties();
        }
        if (!StringUtils.isNull(str)) {
            httpURLConnection.addRequestProperty("cmd", str);
        }
        addHeadSign(httpURLConnection);
    }
}
