package cn.com.xy.sms.sdk.service.f;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.TrainManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.util.DateUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class a {
    private static final long a = 604800000;

    private static JSONObject a(String str, String str2, String str3, String str4) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        String[] split = str.split("/");
        if (split.length != 1) {
            if (StringUtils.isNull(str3)) {
                if (!StringUtils.isNull(str4)) {
                }
            }
            for (String queryTrainInfo : split) {
                JSONObject queryTrainInfo2 = TrainManager.queryTrainInfo(queryTrainInfo, str2);
                String queryTrainInfo3 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo2, TrainManager.STATION_LIST);
                if (!StringUtils.isNull(queryTrainInfo3) && a(queryTrainInfo3, str3, str4)) {
                    return queryTrainInfo2;
                }
            }
            return null;
        }
        return TrainManager.queryTrainInfo(split[0], str2);
    }

    public static void a(String str, String str2, SdkCallBack sdkCallBack) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("flight_num", str.replace(" ", ""));
            jSONObject.put("flight_date", str2);
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL_FLIGHT, jSONObject.toString(), sdkCallBack);
        } catch (JSONException e) {
            XyUtil.doXycallBackResult(sdkCallBack, null);
        }
    }

    static /* synthetic */ void a(String str, String str2, String str3, String str4, String str5, SdkCallBack sdkCallBack, Map map) {
        if (StringUtils.isNull(str2)) {
            XyUtil.doXycallBackResult(sdkCallBack, str, null, str2, str4, str5, Boolean.valueOf(false));
            return;
        }
        JSONObject queryTrainInfo;
        String str6;
        long j;
        boolean a;
        if (!StringUtils.isNull(str2)) {
            String[] split = str2.split("/");
            if (split.length != 1) {
                if (StringUtils.isNull(str4)) {
                    if (!StringUtils.isNull(str5)) {
                    }
                }
                for (String str62 : split) {
                    queryTrainInfo = TrainManager.queryTrainInfo(str62, str3);
                    str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.STATION_LIST);
                    if (!StringUtils.isNull(str62)) {
                        if (a(str62, str4, str5)) {
                            break;
                        }
                    }
                }
            }
            queryTrainInfo = TrainManager.queryTrainInfo(split[0], str3);
            j = 0;
            str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.DATA_TIME);
            if (str62 != null) {
                j = Long.parseLong(str62);
            }
            a = a(map);
            if (queryTrainInfo != null) {
                if (((System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000) ? 1 : null) != null ? 1 : null) == null || !a) {
                    try {
                        str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.STATION_LIST);
                        if (!StringUtils.isNull(str62)) {
                            queryTrainInfo.put(TrainManager.STATION_LIST, new JSONArray(str62));
                        }
                    } catch (JSONException e) {
                    }
                    XyUtil.doXycallBackResult(sdkCallBack, str, queryTrainInfo, str2, str4, str5, Boolean.valueOf(false));
                    return;
                }
            }
            if (a) {
                XyUtil.doXycallBackResult(sdkCallBack, str, "offNetwork");
            } else {
                TrainManager.checkDataOnline(sdkCallBack, str, str2, str3, str4, str5, map);
            }
        }
        queryTrainInfo = null;
        j = 0;
        try {
            str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.DATA_TIME);
            if (str62 != null) {
                j = Long.parseLong(str62);
            }
        } catch (NumberFormatException e2) {
        }
        a = a(map);
        if (queryTrainInfo != null) {
            if (System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000)) {
            }
            if ((System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000) ? 1 : null) != null) {
            }
            if (((System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000) ? 1 : null) != null ? 1 : null) == null) {
            }
            str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.STATION_LIST);
            if (StringUtils.isNull(str62)) {
                queryTrainInfo.put(TrainManager.STATION_LIST, new JSONArray(str62));
            }
            XyUtil.doXycallBackResult(sdkCallBack, str, queryTrainInfo, str2, str4, str5, Boolean.valueOf(false));
            return;
        }
        if (a) {
            XyUtil.doXycallBackResult(sdkCallBack, str, "offNetwork");
        } else {
            TrainManager.checkDataOnline(sdkCallBack, str, str2, str3, str4, str5, map);
        }
    }

    public static void a(String str, String str2, String str3, String str4, Map<String, Object> map, SdkCallBack sdkCallBack) {
        String str5 = (String) JsonUtil.getValueWithMap(map, "phoneNumber");
        String str6 = (String) JsonUtil.getValueWithMap(map, "titleNo");
        String str7 = (String) JsonUtil.getValueWithMap(map, "bubbleJsonObj");
        String str8 = (String) JsonUtil.getValueWithMap(map, "messageBody");
        String str9 = (String) JsonUtil.getValueWithMap(map, "notSaveToDb");
        String str10 = (String) JsonUtil.getValueWithMap(map, TrainManager.DAY);
        String timeString = !StringUtils.isNull(str10) ? str10 : DateUtils.getTimeString(Constant.PATTERN, System.currentTimeMillis());
        if (!StringUtils.isNull(timeString)) {
            if (((((System.currentTimeMillis() - DateUtils.getTime(timeString, Constant.PATTERN)) > DexUtil.getUpdateCycleByType(36, Constant.month) ? 1 : ((System.currentTimeMillis() - DateUtils.getTime(timeString, Constant.PATTERN)) == DexUtil.getUpdateCycleByType(36, Constant.month) ? 0 : -1)) >= 0 ? 1 : null) == null ? 1 : null) == null) {
                XyUtil.doXycallBackResult(sdkCallBack, str, "timeOut");
                return;
            }
        }
        cn.com.xy.sms.sdk.a.a.h.execute(new c(str, str2, timeString, str3, str4, map, str7, str9, str5, str6, str8, sdkCallBack));
    }

    public static void a(String str, String str2, String str3, Map<String, Object> map, SdkCallBack sdkCallBack) {
        if (StringUtils.isNull(str2) || StringUtils.isNull(str3)) {
            XyUtil.doXycallBackResult(sdkCallBack, null);
            return;
        }
        String str4 = (String) JsonUtil.getValueWithMap(map, "flight_form");
        String str5 = (String) JsonUtil.getValueWithMap(map, "flight_to");
        String str6 = (String) JsonUtil.getValueWithMap(map, "flight_from_airport");
        String str7 = (String) JsonUtil.getValueWithMap(map, "flight_to_airport");
        String str8 = (String) JsonUtil.getValueWithMap(map, "phoneNumber");
        String str9 = (String) JsonUtil.getValueWithMap(map, "titleNo");
        String str10 = (String) JsonUtil.getValueWithMap(map, "msgId");
        String str11 = str3;
        a(str2, str11, new b((String) JsonUtil.getValueWithMap(map, "bubbleJsonObj"), (String) JsonUtil.getValueWithMap(map, "notSaveToDb"), str8, str9, str10, (String) JsonUtil.getValueWithMap(map, "messageBody"), sdkCallBack, str4, str5, str6, str7, str));
    }

    private static boolean a(long j) {
        return !(((System.currentTimeMillis() - j) > DexUtil.getUpdateCycleByType(21, 604800000) ? 1 : ((System.currentTimeMillis() - j) == DexUtil.getUpdateCycleByType(21, 604800000) ? 0 : -1)) <= 0);
    }

    private static boolean a(String str) {
        return !(((System.currentTimeMillis() - DateUtils.getTime(str, Constant.PATTERN)) > DexUtil.getUpdateCycleByType(36, Constant.month) ? 1 : ((System.currentTimeMillis() - DateUtils.getTime(str, Constant.PATTERN)) == DexUtil.getUpdateCycleByType(36, Constant.month) ? 0 : -1)) >= 0);
    }

    public static boolean a(String str, String str2, String str3) {
        if (str == null) {
            return false;
        }
        try {
            JSONArray jSONArray = new JSONArray(str);
            JSONObject jSONObject;
            if (StringUtils.isNull(str2) && !StringUtils.isNull(str3)) {
                jSONObject = (JSONObject) jSONArray.get(0);
                return (jSONObject == null || jSONObject.getString("name").trim().equalsIgnoreCase(str3.trim())) ? false : true;
            } else if (StringUtils.isNull(str3) && !StringUtils.isNull(str2)) {
                jSONObject = (JSONObject) jSONArray.get(jSONArray.length() - 1);
                if (jSONObject != null && !jSONObject.getString("name").trim().equalsIgnoreCase(str2.trim())) {
                    return true;
                }
            } else if (!StringUtils.isNull(str2) && !StringUtils.isNull(str3)) {
                int indexOf = str.indexOf("\"" + str2 + "\"");
                int indexOf2 = str.indexOf("\"" + str3 + "\"");
                if (indexOf != -1 && indexOf2 != -1 && indexOf < indexOf2) {
                    return true;
                }
            } else if (StringUtils.isNull(str2) && StringUtils.isNull(str3)) {
                return true;
            }
        } catch (JSONException e) {
        }
    }

    private static boolean a(Map<String, Object> map) {
        boolean booleanValue;
        if (map != null) {
            try {
                if (map.containsKey("allNetworkState")) {
                    booleanValue = ((Boolean) map.get("allNetworkState")).booleanValue();
                    return booleanValue ? NetUtil.checkAccessNetWork(2) : XyUtil.checkNetWork(Constant.getContext(), 2) != 0;
                }
            } catch (Throwable th) {
            }
        }
        booleanValue = false;
        if (booleanValue) {
            if (XyUtil.checkNetWork(Constant.getContext(), 2) != 0) {
            }
        }
    }

    private static void b(String str, String str2, String str3, String str4, String str5, SdkCallBack sdkCallBack, Map<String, Object> map) {
        if (StringUtils.isNull(str2)) {
            XyUtil.doXycallBackResult(sdkCallBack, str, null, str2, str4, str5, Boolean.valueOf(false));
            return;
        }
        JSONObject queryTrainInfo;
        String str6;
        long j;
        boolean a;
        if (!StringUtils.isNull(str2)) {
            String[] split = str2.split("/");
            if (split.length != 1) {
                if (StringUtils.isNull(str4)) {
                    if (!StringUtils.isNull(str5)) {
                    }
                }
                for (String str62 : split) {
                    queryTrainInfo = TrainManager.queryTrainInfo(str62, str3);
                    str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.STATION_LIST);
                    if (!StringUtils.isNull(str62)) {
                        if (a(str62, str4, str5)) {
                            break;
                        }
                    }
                }
            }
            queryTrainInfo = TrainManager.queryTrainInfo(split[0], str3);
            j = 0;
            str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.DATA_TIME);
            if (str62 != null) {
                j = Long.parseLong(str62);
            }
            a = a((Map) map);
            if (queryTrainInfo != null) {
                if (((System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000) ? 1 : null) != null ? 1 : null) == null || !a) {
                    try {
                        str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.STATION_LIST);
                        if (!StringUtils.isNull(str62)) {
                            queryTrainInfo.put(TrainManager.STATION_LIST, new JSONArray(str62));
                        }
                    } catch (JSONException e) {
                    }
                    XyUtil.doXycallBackResult(sdkCallBack, str, queryTrainInfo, str2, str4, str5, Boolean.valueOf(false));
                    return;
                }
            }
            if (a) {
                XyUtil.doXycallBackResult(sdkCallBack, str, "offNetwork");
            } else {
                TrainManager.checkDataOnline(sdkCallBack, str, str2, str3, str4, str5, map);
            }
        }
        queryTrainInfo = null;
        j = 0;
        try {
            str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.DATA_TIME);
            if (str62 != null) {
                j = Long.parseLong(str62);
            }
        } catch (NumberFormatException e2) {
        }
        a = a((Map) map);
        if (queryTrainInfo != null) {
            if (System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000)) {
            }
            if ((System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000) ? 1 : null) != null) {
            }
            if (((System.currentTimeMillis() - j > DexUtil.getUpdateCycleByType(21, 604800000) ? 1 : null) != null ? 1 : null) == null) {
            }
            str62 = (String) JsonUtil.getValueFromJsonObject(queryTrainInfo, TrainManager.STATION_LIST);
            if (StringUtils.isNull(str62)) {
                queryTrainInfo.put(TrainManager.STATION_LIST, new JSONArray(str62));
            }
            XyUtil.doXycallBackResult(sdkCallBack, str, queryTrainInfo, str2, str4, str5, Boolean.valueOf(false));
            return;
        }
        if (a) {
            XyUtil.doXycallBackResult(sdkCallBack, str, "offNetwork");
        } else {
            TrainManager.checkDataOnline(sdkCallBack, str, str2, str3, str4, str5, map);
        }
    }
}
