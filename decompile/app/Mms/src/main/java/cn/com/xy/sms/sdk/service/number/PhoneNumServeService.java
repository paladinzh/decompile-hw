package cn.com.xy.sms.sdk.service.number;

import android.util.LruCache;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.db.entity.q;
import cn.com.xy.sms.sdk.db.entity.r;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class PhoneNumServeService {
    public static final int MENU_CALLBACK_DATA = 102;
    public static final int MENU_CALLBACK_ERROR = 100;
    public static final int MENU_CALLBACK_EXCEPTION = 101;
    private static final LruCache<String, JSONObject> a = new LruCache(100);
    private static long b = 0;

    static /* synthetic */ void a(long j, int i) {
        try {
            List a = r.a(j, i);
            List arrayList = new ArrayList();
            for (int i2 = 0; i2 < a.size(); i2++) {
                arrayList.add(((q) a.get(i2)).a);
            }
            new StringBuilder("backgroud update status:").append(i).append(", phones:").append(arrayList.toString());
            int size = a.size();
            if (size > 0) {
                int i3;
                double ceil = Math.ceil(Double.parseDouble(String.valueOf(size)) / 20.0d);
                if (ceil > 10.0d) {
                    i3 = 200;
                    ceil = 10.0d;
                } else {
                    i3 = size;
                }
                int i4 = 0;
                while (true) {
                    if ((((double) i4) < ceil ? 1 : null) != null) {
                        arrayList = a.subList(i4 * 20, ((double) (i4 + 1)) == ceil ? i3 : (i4 + 1) * 20);
                        if (arrayList != null && arrayList.size() > 0) {
                            NetUtil.executeServiceHttpRequest(NetUtil.GET_PHONE_MENU, j.c(arrayList), null, new j(arrayList));
                        }
                        i4++;
                    } else {
                        return;
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    private static void a(List<q> list) {
        if (list != null && list.size() > 0) {
            XyCallBack jVar = new j(list);
            NetUtil.executeServiceHttpRequest(NetUtil.GET_PHONE_MENU, j.c((List) list), null, jVar);
        }
    }

    static /* synthetic */ void a(List list, String str, SdkCallBack sdkCallBack) {
        XyCallBack hVar = new h(str, sdkCallBack);
        NetUtil.executeServiceHttpRequest(NetUtil.GET_PHONE_MENU, j.a(list, str), null, hVar);
    }

    private static void b() {
        if ((b + Constant.FIVE_MINUTES >= System.currentTimeMillis() ? 1 : null) == null) {
            b = System.currentTimeMillis();
            a.a.execute(new i());
        }
    }

    private static void b(long j, int i) {
        try {
            List a = r.a(j, i);
            List arrayList = new ArrayList();
            for (int i2 = 0; i2 < a.size(); i2++) {
                arrayList.add(((q) a.get(i2)).a);
            }
            new StringBuilder("backgroud update status:").append(i).append(", phones:").append(arrayList.toString());
            int size = a.size();
            if (size > 0) {
                int i3;
                double ceil = Math.ceil(Double.parseDouble(String.valueOf(size)) / 20.0d);
                if (ceil > 10.0d) {
                    i3 = 200;
                    ceil = 10.0d;
                } else {
                    i3 = size;
                }
                int i4 = 0;
                while (true) {
                    if ((((double) i4) < ceil ? 1 : null) != null) {
                        arrayList = a.subList(i4 * 20, ((double) (i4 + 1)) == ceil ? i3 : (i4 + 1) * 20);
                        if (arrayList != null && arrayList.size() > 0) {
                            NetUtil.executeServiceHttpRequest(NetUtil.GET_PHONE_MENU, j.c(arrayList), null, new j(arrayList));
                        }
                        i4++;
                    } else {
                        return;
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    private static void b(List<JSONObject> list, SdkCallBack sdkCallBack) {
        if (list != null && list.size() > 0) {
            JSONArray jSONArray = new JSONArray();
            for (int i = 0; i < list.size(); i++) {
                jSONArray.put(list.get(i));
            }
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(102), jSONArray);
        }
    }

    private static void b(List<String> list, String str, SdkCallBack sdkCallBack) {
        XyCallBack hVar = new h(str, sdkCallBack);
        NetUtil.executeServiceHttpRequest(NetUtil.GET_PHONE_MENU, j.a((List) list, str), null, hVar);
    }

    public static void baseQueryPhoneNumsServe(List<String> list, Map<String, String> map, SdkCallBack sdkCallBack) {
        if (list == null || list.size() == 0 || list.size() > 50) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(101), "exception params");
            return;
        }
        List arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            String phoneNumberNo86 = StringUtils.getPhoneNumberNo86((String) list.get(i));
            if (!arrayList.contains(phoneNumberNo86)) {
                arrayList.add(phoneNumberNo86);
            }
        }
        if (arrayList.size() != 0) {
            String valueByKey = StringUtils.getValueByKey(map, IccidInfoManager.CNUM);
            phoneNumberNo86 = StringUtils.getValueByKey(map, "code");
            String valueByKey2 = StringUtils.getValueByKey(map, IccidInfoManager.ICCID);
            int d = f.d(StringUtils.getValueByKey(map, "simIndex"));
            if (StringUtils.isNull(phoneNumberNo86)) {
                phoneNumberNo86 = IccidLocationUtil.getAreaCodeByCnumOrIccid(valueByKey, d, valueByKey2, (String) arrayList.get(0));
            }
            a.b.execute(new c(arrayList, phoneNumberNo86, sdkCallBack));
        }
    }

    public static Map<String, JSONObject> loadDBDataToCache(List<String> list, String str) {
        Map<String, JSONObject> map = null;
        try {
            map = r.a(list, str, a);
        } catch (Throwable th) {
        }
        return map;
    }

    public static void queryPhoneNumMenus(String str, Map<String, String> map, SdkCallBack sdkCallBack) {
        if (!StringUtils.isNull(str)) {
            String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str);
            String valueByKey = StringUtils.getValueByKey(map, IccidInfoManager.CNUM);
            String valueByKey2 = StringUtils.getValueByKey(map, "code");
            String valueByKey3 = StringUtils.getValueByKey(map, IccidInfoManager.ICCID);
            int d = f.d(StringUtils.getValueByKey(map, "simIndex"));
            if (StringUtils.isNull(valueByKey2)) {
                valueByKey2 = IccidLocationUtil.getAreaCodeByCnumOrIccid(valueByKey, d, valueByKey3, str);
            }
            valueByKey = valueByKey2;
            b();
            valueByKey3 = String.format("%s:%s", new Object[]{phoneNumberNo86, valueByKey});
            JSONObject jSONObject = (JSONObject) a.get(valueByKey3);
            if (jSONObject != null && jSONObject.toString().length() > 2) {
                new StringBuilder("get result from cache:").append(jSONObject.toString());
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(102), jSONObject);
                return;
            }
            a.b.execute(new e(phoneNumberNo86, valueByKey, valueByKey3, sdkCallBack));
        }
    }

    public static void queryPhoneNumsMenus(List<String> list, Map<String, String> map, SdkCallBack sdkCallBack) {
        if (list != null && list.size() != 0 && list.size() <= 50) {
            String valueByKey = StringUtils.getValueByKey(map, IccidInfoManager.CNUM);
            String valueByKey2 = StringUtils.getValueByKey(map, "code");
            String valueByKey3 = StringUtils.getValueByKey(map, IccidInfoManager.ICCID);
            int d = f.d(StringUtils.getValueByKey(map, "simIndex"));
            List arrayList = new ArrayList();
            List arrayList2 = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                String phoneNumberNo86 = StringUtils.getPhoneNumberNo86((String) list.get(i));
                if (!arrayList.contains(phoneNumberNo86)) {
                    arrayList.add(phoneNumberNo86);
                }
            }
            arrayList2.addAll(arrayList);
            List arrayList3 = new ArrayList();
            String areaCodeByCnumOrIccid = (StringUtils.isNull(valueByKey2) && arrayList.size() > 0) ? IccidLocationUtil.getAreaCodeByCnumOrIccid(valueByKey, d, valueByKey3, (String) arrayList.get(0)) : valueByKey2;
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                valueByKey = String.format("%s:%s", new Object[]{arrayList.get(i2), areaCodeByCnumOrIccid});
                JSONObject jSONObject = (JSONObject) a.get(valueByKey);
                if (jSONObject != null && jSONObject.toString().length() > 2) {
                    new StringBuilder("get result from cache,key:").append(valueByKey).append("result:").append(jSONObject.toString());
                    arrayList3.add(jSONObject);
                    arrayList2.remove(arrayList.get(i2));
                }
            }
            if (arrayList3.size() > 0) {
                b(arrayList3, sdkCallBack);
                new StringBuilder("tmpQueryNums:").append(arrayList2.toString());
            }
            if (arrayList2.size() > 0) {
                a.a.execute(new g(arrayList2, areaCodeByCnumOrIccid, sdkCallBack));
            }
            b();
        }
    }
}
