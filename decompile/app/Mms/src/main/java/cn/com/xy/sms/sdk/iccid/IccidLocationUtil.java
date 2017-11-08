package cn.com.xy.sms.sdk.iccid;

import android.content.Context;
import android.telephony.TelephonyManager;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
public class IccidLocationUtil {
    private static final String a = "iccid";
    private static long b;
    private static final HashMap<String, String[]> c = new HashMap();
    private static boolean d = false;
    private static Map<String, Long> e = Collections.synchronizedMap(new HashMap());

    private static IccidInfo a(IccidInfo iccidInfo, boolean z, boolean z2) {
        if (!z) {
            return iccidInfo;
        }
        String str;
        String str2;
        String str3 = "";
        if (iccidInfo == null) {
            str = null;
            str2 = str3;
            str3 = null;
        } else {
            if (!StringUtils.isNull(iccidInfo.cnum)) {
                if ("10000".equals(iccidInfo.num.trim()) || "10010".equals(iccidInfo.num.trim()) || "10086".equals(iccidInfo.num.trim())) {
                    str3 = iccidInfo.cnum;
                }
            }
            str2 = iccidInfo.iccid;
            str = str2;
            str2 = str3;
            str3 = iccidInfo.num;
        }
        queryIccid(str2, str, str3, z2, true);
        return null;
    }

    private static String a() {
        String str = null;
        for (Entry value : c.entrySet()) {
            String[] strArr = (String[]) value.getValue();
            String str2 = strArr[0];
            String str3 = strArr[3];
            if (StringUtils.isNull(str3)) {
                if (StringUtils.isNull(str2)) {
                    return null;
                }
                if (str != null && !str.equals(str2)) {
                    return null;
                }
                str = str2;
            } else if (str != null && !str.equals(str3)) {
                return null;
            } else {
                str = str3;
            }
        }
        return str;
    }

    private static String a(int i) {
        Set<Entry> entrySet = c.entrySet();
        String valueOf = String.valueOf(i);
        for (Entry value : entrySet) {
            String[] strArr = (String[]) value.getValue();
            String str = strArr[3];
            if (valueOf.equals(strArr[5])) {
                return str;
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void a(Context context, IccidInfo iccidInfo, boolean z) {
        String str = null;
        boolean z2 = false;
        if (iccidInfo != null && !StringUtils.isNull(iccidInfo.iccid)) {
            String str2;
            String str3;
            long updateCycleByType = DexUtil.getUpdateCycleByType(16, 4838400000L);
            if (!(iccidInfo == null || StringUtils.isNull(iccidInfo.provinces) || !iccidInfo.provinces.equals("未知"))) {
            }
            if (StringUtils.isNull(iccidInfo.provinces)) {
                if (iccidInfo.updateTime != 0) {
                }
                z2 = true;
                if (z2) {
                    str2 = "";
                    if (iccidInfo != null) {
                        str3 = str2;
                        str2 = null;
                    } else {
                        if (!StringUtils.isNull(iccidInfo.cnum)) {
                            if ("10000".equals(iccidInfo.num.trim()) || "10010".equals(iccidInfo.num.trim()) || "10086".equals(iccidInfo.num.trim())) {
                                str2 = iccidInfo.cnum;
                            }
                        }
                        str = iccidInfo.iccid;
                        str3 = str2;
                        str2 = iccidInfo.num;
                    }
                    queryIccid(str3, str, str2, z, true);
                }
            }
            if (!StringUtils.isNull(iccidInfo.provinces)) {
                if (!(iccidInfo.updateTime >= System.currentTimeMillis() - updateCycleByType)) {
                    z2 = true;
                }
            }
            if (z2) {
                str2 = "";
                if (iccidInfo != null) {
                    if (StringUtils.isNull(iccidInfo.cnum)) {
                        if ("10000".equals(iccidInfo.num.trim())) {
                        }
                        str2 = iccidInfo.cnum;
                    }
                    str = iccidInfo.iccid;
                    str3 = str2;
                    str2 = iccidInfo.num;
                } else {
                    str3 = str2;
                    str2 = null;
                }
                queryIccid(str3, str, str2, z, true);
            }
        }
    }

    private static void a(String str, long j) {
        if (!StringUtils.isNull(str)) {
            e.put(str, Long.valueOf(j));
        }
    }

    private static void a(String str, IccidInfo iccidInfo) {
        try {
            if (!StringUtils.isNull(str)) {
                if (!StringUtils.isNull(str)) {
                    Long l = (Long) e.get(str);
                    if (l != null) {
                        if ((DexUtil.getUpdateCycleByType(4, 30000) + l.longValue() <= System.currentTimeMillis() ? 1 : null) == null) {
                            int i = 1;
                            if (r0 == null) {
                                if (iccidInfo == null) {
                                    a.e.execute(new a(iccidInfo));
                                } else {
                                    queryIccid(null, str, null, true, true);
                                }
                            }
                        }
                    }
                }
                Object obj = null;
                if (obj == null) {
                    if (iccidInfo == null) {
                        queryIccid(null, str, null, true, true);
                    } else {
                        a.e.execute(new a(iccidInfo));
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    private static void a(String str, cn.com.xy.sms.sdk.db.entity.a aVar, String str2, String str3, boolean z, boolean z2) {
        int i = 0;
        if (!StringUtils.isNull(str)) {
            if (aVar != null) {
                if (aVar.g <= System.currentTimeMillis() - DexUtil.getUpdateCycleByType(0, 7776000000L)) {
                    i = 1;
                }
                if (i == 0) {
                    return;
                }
            }
            queryIccid(str, str2, str3, true, true);
        }
    }

    private static boolean a(String str) {
        if (StringUtils.isNull(str)) {
            return false;
        }
        Long l = (Long) e.get(str);
        if (l == null) {
            return false;
        }
        return !(((DexUtil.getUpdateCycleByType(4, 30000) + l.longValue()) > System.currentTimeMillis() ? 1 : ((DexUtil.getUpdateCycleByType(4, 30000) + l.longValue()) == System.currentTimeMillis() ? 0 : -1)) <= 0);
    }

    private static boolean a(String str, String str2, String str3) {
        return (StringUtils.isNull(str2) || !str2.equals(str) || StringUtils.isNull(str3)) ? false : true;
    }

    private static void b(String str) {
        if (!StringUtils.isNull(str)) {
            int i = (c.get(str) == null || StringUtils.isNull(((String[]) c.get(str))[0])) ? 1 : 0;
            if (i != 0) {
                loadIccidInfoToCacheAndQueryNetIccidInfo(str);
            }
        }
    }

    private static void b(String str, String str2, String str3, boolean z) {
        try {
            NetUtil.requestTokenIfNeed(str2);
            XyCallBack bVar = new b(str, str2);
            String a = j.a(str, str2, str3);
            if (!StringUtils.isNull(a)) {
                NetUtil.executeAllNetHttpRequest(a, "990005", bVar, z, false, NetUtil.REQ_QUERY_LOCATION, false);
            }
        } catch (Throwable th) {
        }
    }

    private static cn.com.xy.sms.sdk.db.entity.a c(String str) {
        String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str);
        if (StringUtils.isNull(phoneNumberNo86)) {
            return null;
        }
        phoneNumberNo86 = StringUtils.getSubString(phoneNumberNo86);
        cn.com.xy.sms.sdk.db.entity.a a = cn.com.xy.sms.sdk.db.entity.a.a.a(phoneNumberNo86);
        if (a != null) {
            return a;
        }
        a = cn.com.xy.sms.sdk.db.entity.a.a.b(phoneNumberNo86);
        if (a != null) {
            cn.com.xy.sms.sdk.db.entity.a.a.a(phoneNumberNo86, a);
        }
        return a;
    }

    public static void changeIccidAreaCode(boolean z) {
        if (z || !d) {
            d = true;
            c.clear();
            String iccidBySimIndex = DuoquUtils.getSdkDoAction().getIccidBySimIndex(0);
            String iccidBySimIndex2 = DuoquUtils.getSdkDoAction().getIccidBySimIndex(1);
            if (!StringUtils.isNull(iccidBySimIndex)) {
                loadIccidInfoToCacheAndQueryNetIccidInfo(iccidBySimIndex);
            }
            if (!StringUtils.isNull(iccidBySimIndex2)) {
                loadIccidInfoToCacheAndQueryNetIccidInfo(iccidBySimIndex2);
            }
        }
    }

    private static String d(String str) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        String[] strArr = (String[]) c.get(str);
        if (strArr == null) {
            return null;
        }
        String str2 = strArr[0];
        return !StringUtils.isNull(str2) ? str2 : null;
    }

    private static String e(String str) {
        int i = 0;
        if (c.isEmpty() || StringUtils.isNull(str)) {
            return null;
        }
        try {
            int i2 = 0;
            String str2 = null;
            for (Entry value : c.entrySet()) {
                String[] strArr = (String[]) value.getValue();
                String str3 = strArr[0];
                String str4 = strArr[2];
                String str5 = strArr[3];
                if (a(str, strArr[4], str5)) {
                    int i3 = !str5.equals(str2) ? i : i + 1;
                    i2++;
                    str2 = str5;
                    i = i3;
                } else if (StringUtils.isNull(str5) && a(str, str4, str3)) {
                    if (str3.equals(str2)) {
                        i++;
                    }
                    i2++;
                    str2 = str3;
                }
            }
            if (i2 != 1) {
                if (i2 == c.size()) {
                    if (i + 1 != c.size()) {
                    }
                }
                return null;
            }
            return str2;
        } catch (Throwable th) {
        }
    }

    public static String getAreaCodeByCnumOrIccid(String str, int i, String str2, String str3) {
        String str4 = null;
        try {
            if (!StringUtils.isNull(str2)) {
                Object obj = (c.get(str2) == null || StringUtils.isNull(((String[]) c.get(str2))[0])) ? 1 : null;
                if (obj != null) {
                    loadIccidInfoToCacheAndQueryNetIccidInfo(str2);
                }
            }
            String userAreaCode = getUserAreaCode(str2, i);
            if (!StringUtils.isNull(userAreaCode)) {
                return userAreaCode;
            }
            cn.com.xy.sms.sdk.db.entity.a aVar;
            userAreaCode = StringUtils.getPhoneNumberNo86(str);
            if (StringUtils.isNull(userAreaCode)) {
                aVar = null;
            } else {
                String subString = StringUtils.getSubString(userAreaCode);
                cn.com.xy.sms.sdk.db.entity.a a = cn.com.xy.sms.sdk.db.entity.a.a.a(subString);
                if (a == null) {
                    a = cn.com.xy.sms.sdk.db.entity.a.a.b(subString);
                    if (a != null) {
                        cn.com.xy.sms.sdk.db.entity.a.a.a(subString, a);
                    }
                }
                aVar = a;
            }
            if (!StringUtils.isNull(str)) {
                if (aVar != null) {
                    if ((aVar.g > System.currentTimeMillis() - DexUtil.getUpdateCycleByType(0, 7776000000L) ? 1 : null) == null) {
                    }
                }
                queryIccid(str, str2, str3, true, true);
            }
            if (aVar == null || StringUtils.isNull(aVar.c)) {
                changeIccidAreaCode(false);
                String[] strArr;
                if (StringUtils.isNull(str2)) {
                    userAreaCode = String.valueOf(getOperatorByNum(str3));
                    if (ThemeUtil.SET_NULL_STR.equals(userAreaCode)) {
                        String str5 = null;
                        for (Entry value : c.entrySet()) {
                            strArr = (String[]) value.getValue();
                            String str6 = strArr[0];
                            userAreaCode = strArr[3];
                            if (!StringUtils.isNull(userAreaCode)) {
                                if (str5 != null) {
                                    if (str5.equals(userAreaCode)) {
                                    }
                                }
                                str5 = userAreaCode;
                            } else if (!StringUtils.isNull(str6)) {
                                if (str5 != null) {
                                    if (str5.equals(str6)) {
                                    }
                                }
                                str5 = str6;
                            }
                            userAreaCode = null;
                        }
                        userAreaCode = str5;
                        if (!StringUtils.isNull(userAreaCode)) {
                            return userAreaCode;
                        }
                        userAreaCode = getAreaCodeByDefaultCard();
                        if (!StringUtils.isNull(userAreaCode)) {
                            return userAreaCode;
                        }
                    }
                    userAreaCode = e(userAreaCode);
                    if (!StringUtils.isNull(userAreaCode)) {
                        return userAreaCode;
                    }
                }
                if (!StringUtils.isNull(str2)) {
                    strArr = (String[]) c.get(str2);
                    if (strArr != null) {
                        userAreaCode = strArr[0];
                        if (!StringUtils.isNull(userAreaCode)) {
                            str4 = userAreaCode;
                        }
                    }
                }
                if (!StringUtils.isNull(str4)) {
                    return str4;
                }
                return "CN";
            }
            userAreaCode = aVar.c;
            return aVar.c;
        } catch (Throwable th) {
        }
    }

    public static String getAreaCodeByDefaultCard() {
        for (Entry value : c.entrySet()) {
            String[] strArr = (String[]) value.getValue();
            String str = strArr[0];
            String str2 = strArr[3];
            if ("1".equals(strArr[6])) {
                return !StringUtils.isNull(str2) ? str2 : str;
            }
        }
        return null;
    }

    public static String getICCID(Context context) {
        try {
            IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(context);
            if (queryDeftIccidInfo != null) {
                if (!StringUtils.isNull(queryDeftIccidInfo.iccid)) {
                    return queryDeftIccidInfo.iccid;
                }
            }
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (!StringUtils.isNull(telephonyManager.getSimSerialNumber())) {
                return telephonyManager.getSimSerialNumber();
            }
        } catch (Throwable th) {
        }
        return "";
    }

    public static HashMap<String, String[]> getIccidAreaCodeMap() {
        return c;
    }

    public static String getIccidAreaCodeMapValueByIndex(String str, int i) {
        if (!StringUtils.isNull(str)) {
            HashMap hashMap = c;
            if (!c.isEmpty()) {
                String[] strArr = (String[]) c.get(str);
                return (strArr != null && strArr.length > i) ? strArr[i] : null;
            }
        }
        return null;
    }

    public static String[] getIccidInfoArr(String str) {
        return (String[]) c.get(str);
    }

    public static int getOperatorByICCID(String str) {
        if (str != null && str.length() > 6) {
            String substring = str.substring(4, 6);
            if (substring.equals("00") || substring.equals("02") || substring.equals("05") || substring.equals("08")) {
                return 1;
            }
            if (substring.equals("01")) {
                return 2;
            }
            if (substring.equals("03")) {
                return 3;
            }
        }
        return -2;
    }

    public static int getOperatorByNum(String str) {
        return !StringUtils.isNull(str) ? "10086,1008611,1008601".indexOf(str) == -1 ? "10010,10011".indexOf(str) == -1 ? "10000,10001".indexOf(str) == -1 ? -1 : 3 : 2 : 1 : -1;
    }

    public static String getOperatorNum(String str, String str2) {
        return !StringUtils.isNull(str2) ? !"移动".equals(str2) ? !"联通".equals(str2) ? !"电信".equals(str2) ? Constant.ACTION_PARSE : NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR : "2" : "1" : String.valueOf(getOperatorByICCID(str));
    }

    public static String getProvince() {
        IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
        return queryDeftIccidInfo == null ? "" : queryDeftIccidInfo.provinces;
    }

    public static String getUserAreaCode(String str, int i) {
        String iccidAreaCodeMapValueByIndex = getIccidAreaCodeMapValueByIndex(str, 3);
        if (!StringUtils.isNull(iccidAreaCodeMapValueByIndex)) {
            return iccidAreaCodeMapValueByIndex;
        }
        if (i < 0) {
            return null;
        }
        Set<Entry> entrySet = c.entrySet();
        String valueOf = String.valueOf(i);
        for (Entry value : entrySet) {
            String[] strArr = (String[]) value.getValue();
            String str2 = strArr[3];
            if (valueOf.equals(strArr[5])) {
                iccidAreaCodeMapValueByIndex = str2;
                break;
            }
        }
        iccidAreaCodeMapValueByIndex = null;
        return iccidAreaCodeMapValueByIndex;
    }

    public static int getUserOperatorNum(String[] strArr) {
        int i = -2;
        if (strArr.length > 4 && !StringUtils.isNull(strArr[4])) {
            try {
                i = Integer.parseInt(strArr[4]);
            } catch (Throwable th) {
            }
        }
        return i;
    }

    public static void loadIccidInfoToCacheAndQueryNetIccidInfo(String str) {
        IccidInfo queryIccidInfo = IccidInfoManager.queryIccidInfo(str, Constant.getContext());
        if (queryIccidInfo == null) {
            putIccidAreaCodeToCache(str, null, null, null, null, -1, 0);
        } else {
            putIccidAreaCodeToCache(str, queryIccidInfo.areaCode, queryIccidInfo.operator, queryIccidInfo.userAreacode, queryIccidInfo.userOperator, queryIccidInfo.simIndex, queryIccidInfo.deft);
        }
        try {
            if (!StringUtils.isNull(str)) {
                if (!StringUtils.isNull(str)) {
                    Long l = (Long) e.get(str);
                    if (l != null) {
                        if ((l.longValue() + DexUtil.getUpdateCycleByType(4, 30000) <= System.currentTimeMillis() ? 1 : null) == null) {
                            int i = 1;
                            if (r0 == null) {
                                if (queryIccidInfo == null) {
                                    a.e.execute(new a(queryIccidInfo));
                                } else {
                                    queryIccid(null, str, null, true, true);
                                }
                            }
                        }
                    }
                }
                Object obj = null;
                if (obj == null) {
                    if (queryIccidInfo == null) {
                        queryIccid(null, str, null, true, true);
                    } else {
                        a.e.execute(new a(queryIccidInfo));
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void putIccidAreaCodeToCache(String str, String str2, String str3, String str4, String str5, int i, int i2) {
        if (str != null) {
            int i3;
            String valueOf;
            String[] strArr = (String[]) c.get(str);
            if (strArr != null) {
                strArr[0] = str2;
                strArr[1] = String.valueOf(System.currentTimeMillis());
                strArr[2] = getOperatorNum(str, str3);
                strArr[3] = str4;
                strArr[4] = getOperatorNum(null, str5);
                strArr[5] = String.valueOf(i);
                i3 = 6;
                valueOf = String.valueOf(i2);
            } else {
                strArr = new String[7];
                strArr[0] = str2;
                strArr[1] = String.valueOf(System.currentTimeMillis());
                strArr[2] = getOperatorNum(str, str3);
                strArr[3] = str4;
                strArr[4] = getOperatorNum(null, str5);
                strArr[5] = String.valueOf(i);
                i3 = 6;
                valueOf = String.valueOf(i2);
            }
            strArr[i3] = valueOf;
            c.put(str, strArr);
        }
    }

    public static boolean queryAreaCode(boolean z) {
        try {
            if (IccidInfoManager.queryDeftIccidInfo(Constant.getContext()) == null) {
                HashMap hashMap = new HashMap();
                hashMap.put("simIccid", XyUtil.getIccid());
                startQueryIccidLocation(hashMap, z);
                return false;
            }
        } catch (Exception e) {
        }
        return true;
    }

    public static void queryIccid(String str, String str2, String str3, boolean z, boolean z2) {
        try {
            if (NetUtil.isEnhance()) {
                long currentTimeMillis = System.currentTimeMillis();
                if (!StringUtils.isNull(str2)) {
                    e.put(str2, Long.valueOf(currentTimeMillis));
                }
                String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str);
                if (z) {
                    a.e.execute(new c(phoneNumberNo86, str2, str3));
                } else {
                    b(phoneNumberNo86, str2, str3, false);
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void startQueryIccidLocation(HashMap<String, String> hashMap, boolean z) {
        if (hashMap != null) {
            String str = (String) hashMap.get("simIccid");
            String str2 = (String) hashMap.get("receiveNum");
            String str3 = (String) hashMap.get("centerNum");
            hashMap.get("sceneId");
            hashMap.get("sms");
            hashMap.get("smsLocate");
            IccidInfo queryIccidInfo = str != null ? IccidInfoManager.queryIccidInfo(str, Constant.getContext()) : IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
            if (queryIccidInfo != null) {
                if (str3 != null && str3.length() > 0 && queryIccidInfo.cnum == null) {
                    queryIccidInfo.cnum = str3;
                }
                if (str2 != null && str2.length() > 0 && queryIccidInfo.num == null) {
                    queryIccidInfo.num = str2;
                }
            } else {
                queryIccidInfo = new IccidInfo();
                queryIccidInfo.cnum = str3;
                queryIccidInfo.iccid = str;
                queryIccidInfo.num = str2;
            }
            IccidInfo iccidInfo = queryIccidInfo;
            IccidInfoManager.updateIccidCnum(iccidInfo.iccid, str3, str2, Constant.getContext());
            Context context = Constant.getContext();
            SysParamEntityManager.getBooleanParam(Constant.getContext(), Constant.SMSLOCATEENABLE);
            a(context, iccidInfo, z);
        }
    }

    public static void updateIccidCache(String str, int i) {
        IccidInfo queryIccidInfo = IccidInfoManager.queryIccidInfo(str, i);
        if (queryIccidInfo != null) {
            putIccidAreaCodeToCache(!StringUtils.isNull(str) ? str : String.valueOf(i), queryIccidInfo.areaCode, queryIccidInfo.operator, queryIccidInfo.userAreacode, queryIccidInfo.userOperator, queryIccidInfo.simIndex, queryIccidInfo.deft);
        }
    }
}
