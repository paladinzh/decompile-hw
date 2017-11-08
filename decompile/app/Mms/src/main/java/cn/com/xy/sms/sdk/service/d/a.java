package cn.com.xy.sms.sdk.service.d;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.n;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class a {
    private static int a = 403;
    private static int b = 404;
    private static int c = 405;
    private static int d = 200;
    private static String e = "10";

    private static long a(int i, long j) {
        return System.currentTimeMillis() - DexUtil.getUpdateCycleByType(i, 604800000);
    }

    private static String a(int i) {
        JSONObject proviceAndSP = DuoquUtils.getSdkDoAction().getProviceAndSP(i);
        if (proviceAndSP != null) {
            try {
                String proviceCode = IccidInfoManager.getProviceCode((String) JsonUtil.getValueFromJsonObject(proviceAndSP, "provice"));
                return proviceCode == null ? null : proviceCode;
            } catch (Throwable th) {
            }
        } else {
            IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
            if (queryDeftIccidInfo != null) {
                return queryDeftIccidInfo.areaCode;
            }
        }
    }

    public static String a(String str) {
        String str2 = "false";
        if (str == null) {
            return null;
        }
        String b;
        try {
            b = n.b(str);
            if (StringUtils.isNull(b)) {
                JSONObject a = n.a(str);
                if (a != null) {
                    n.a(a.getString("phone"), a.getString("pubId"));
                    b = a.getString("pubId");
                    str2 = "false";
                    if (StringUtils.isNull(b)) {
                        i.a(new k(13, "phoneNum", str, "dbresoult", str2));
                        return null;
                    }
                    i.a(new k(13, "phoneNum", str, "dbresoult", str2));
                    return b;
                }
                str2 = "true";
                i.a(new k(13, "phoneNum", str, "dbresoult", str2));
                return null;
            }
            str2 = "false";
            i.a(new k(13, "phoneNum", str, "dbresoult", str2));
            return b;
        } catch (Throwable th) {
            Throwable th2 = th;
            b = str2;
            Throwable th3 = th2;
            i.a(new k(13, "phoneNum", str, "dbresoult", b));
        }
    }

    private static void a() {
        int i = 0;
        try {
            long a = a(31, 604800000);
            JSONArray a2 = n.a(a, e, true);
            int i2 = 0;
            while (!StringUtils.isNull(a2.toString()) && a2.length() != 0) {
                a(a2);
                a2 = n.a(a, e, true);
                i2++;
                if (i2 > 1000) {
                    break;
                }
                Thread.sleep(1);
            }
            n.a(true);
            long a3 = a(30, 604800000);
            JSONArray a4 = n.a(a3, e, false);
            while (!StringUtils.isNull(a4.toString()) && a4.length() != 0) {
                a(a4);
                a4 = n.a(a3, e, false);
                i++;
                if (i > 1000) {
                    break;
                }
                Thread.sleep(1);
            }
            n.a(true);
        } catch (Throwable th) {
        }
    }

    public static void a(String str, String str2) {
        if ("true".equalsIgnoreCase(str2)) {
            try {
                HashMap hashMap = new HashMap();
                hashMap.put("pubId", "");
                hashMap.put("phone", str);
                hashMap.put("querytime", "0");
                hashMap.put("queryflag", "0");
                n.a(hashMap);
            } catch (Exception e) {
            }
        }
        if (NetUtil.isEnhance() && NetUtil.checkAccessNetWork(1)) {
            IccidLocationUtil.queryAreaCode(true);
            a();
        }
    }

    private static void a(StringBuffer stringBuffer) {
        StringBuffer stringBuffer2 = new StringBuffer();
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer2 = stringBuffer2.append(a(0));
        stringBuffer3 = stringBuffer3.append(a(1));
        if (StringUtils.isNull(stringBuffer2.toString()) && StringUtils.isNull(stringBuffer3.toString())) {
            stringBuffer.append("CN");
        } else if (StringUtils.isNull(stringBuffer2.toString()) && !StringUtils.isNull(stringBuffer3.toString())) {
            stringBuffer.append(stringBuffer3.toString());
        } else if (StringUtils.isNull(stringBuffer3.toString()) && !StringUtils.isNull(stringBuffer2.toString())) {
            stringBuffer.append(stringBuffer2.toString());
        } else {
            stringBuffer.append(stringBuffer2.toString()).append(",").append(stringBuffer3.toString());
        }
    }

    private static void a(JSONArray jSONArray) {
        if (jSONArray.length() != 0) {
            try {
                StringBuffer stringBuffer = new StringBuffer();
                StringBuffer stringBuffer2 = new StringBuffer();
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer2 = stringBuffer2.append(a(0));
                stringBuffer3 = stringBuffer3.append(a(1));
                if (StringUtils.isNull(stringBuffer2.toString()) && StringUtils.isNull(stringBuffer3.toString())) {
                    stringBuffer.append("CN");
                } else if (StringUtils.isNull(stringBuffer2.toString()) && !StringUtils.isNull(stringBuffer3.toString())) {
                    stringBuffer.append(stringBuffer3.toString());
                } else if (StringUtils.isNull(stringBuffer3.toString()) && !StringUtils.isNull(stringBuffer2.toString())) {
                    stringBuffer.append(stringBuffer2.toString());
                } else {
                    stringBuffer.append(stringBuffer2.toString()).append(",").append(stringBuffer3.toString());
                }
                if (NetUtil.hasNewToken(null)) {
                    String a = j.a(stringBuffer.toString(), (Object) jSONArray);
                    if (!StringUtils.isNull(a)) {
                        NetUtil.requestNewTokenIfNeed(null);
                        NetUtil.executeNewServiceHttpRequest(NetUtil.URL_PUB_NUMBER, a, new b(a, NetUtil.getToken()), false, false, true, null);
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
