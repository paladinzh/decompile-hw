package cn.com.xy.sms.sdk.util;

import android.content.Context;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.t;
import cn.com.xy.sms.sdk.db.entity.u;
import cn.com.xy.sms.sdk.db.entity.v;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class w {
    private static String a(Context context) {
        return SysParamEntityManager.getStringParam(context, "LastSceneCountActionUpdate");
    }

    public static String a(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        List<t> a = u.a(str);
        stringBuffer.append("<SceneStat>");
        if (a.size() <= 0) {
            return null;
        }
        for (t tVar : a) {
            t tVar2;
            stringBuffer.append("t1;");
            String str2 = tVar2.b;
            stringBuffer.append(new StringBuilder(String.valueOf(str2)).append(";").toString());
            stringBuffer.append(new StringBuilder(String.valueOf(StringUtils.getMD5(IccidLocationUtil.getICCID(Constant.getContext())))).append(";").toString());
            List b = u.b(str2);
            for (int i = 0; i < b.size(); i++) {
                tVar2 = (t) b.get(i);
                if (i != 0) {
                    stringBuffer.append("&amp;");
                }
                String str3 = tVar2.a;
                stringBuffer.append(new StringBuilder(String.valueOf(str3)).append(",").toString());
                stringBuffer.append(tVar2.c + ",");
                stringBuffer.append(tVar2.d + ",");
                JSONArray a2 = v.a(str3, str2);
                if (a2 != null) {
                    int i2 = 0;
                    while (i2 < a2.length()) {
                        try {
                            JSONObject jSONObject = a2.getJSONObject(i2);
                            if (i2 != 0) {
                                stringBuffer.append("#");
                            }
                            Object string = jSONObject.getString("action_code");
                            if (StringUtils.isNull(string)) {
                                string = jSONObject.getString("action_type");
                            }
                            stringBuffer.append(new StringBuilder(String.valueOf(string)).append("=").toString());
                            stringBuffer.append(jSONObject.getString("times"));
                            i2++;
                        } catch (Throwable th) {
                        }
                    }
                }
            }
            stringBuffer.append("\n");
        }
        stringBuffer.append("</SceneStat>");
        return stringBuffer.toString();
    }

    public static void a() {
        int i = 1;
        String currentTimeString = DateUtils.getCurrentTimeString("yyyyMMdd");
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "LastSceneCountActionUpdate");
        if (stringParam != null) {
            i = DateUtils.compareDateString(currentTimeString, DateUtils.addDays(stringParam, "yyyyMMdd", 1), "yyyyMMdd");
        }
        if (i != 0) {
            try {
                String a = a(currentTimeString);
                if (!StringUtils.isNull(a)) {
                    XyCallBack xVar = new x(currentTimeString);
                    if (NetUtil.isEnhance()) {
                        NetUtil.executeLoginBeforeHttpRequest(a, "990005", xVar, NetUtil.STATSERVICE_URL, true);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    private static void b(String str) {
        SysParamEntityManager.setParam("LastSceneCountActionUpdate", str);
    }
}
