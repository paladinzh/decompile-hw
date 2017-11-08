package cn.com.xy.sms.sdk.net.util;

import android.content.ContentValues;
import android.telephony.TelephonyManager;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.entity.A;
import cn.com.xy.sms.sdk.db.entity.C;
import cn.com.xy.sms.sdk.db.entity.H;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SceneRule;
import cn.com.xy.sms.sdk.db.entity.a.b;
import cn.com.xy.sms.sdk.db.entity.a.c;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.db.entity.a.k;
import cn.com.xy.sms.sdk.db.entity.a.m;
import cn.com.xy.sms.sdk.db.entity.g;
import cn.com.xy.sms.sdk.db.entity.q;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.l;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.G;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.amap.api.services.district.DistrictSearchQuery;
import com.google.android.gms.common.Scopes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* compiled from: Unknown */
public final class j {
    public static int a = 0;
    public static int b = 1;
    private static String c = "1";
    private static String d = "2";
    private static String e = "3";
    private static final int f = -1;
    private static String g = "0";
    private static String h = "1";
    private static String i = "-1";
    private static int j = -1;
    private static String k = null;

    public static String a() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("secdata", l.b + "_" + System.currentTimeMillis());
            return jSONObject.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String a(IccidInfo iccidInfo, int i) {
        try {
            StringBuffer c = c();
            c.append("<queryIccidInfoRequest>");
            c.append("<iccid>");
            c.append(iccidInfo.iccid);
            c.append("</iccid>");
            c.append("<cmd>" + i + "</cmd>");
            c.append("<operator>" + iccidInfo.operator + "</operator>");
            c.append("<provinces>" + iccidInfo.provinces + "</provinces>");
            c.append("<city>" + iccidInfo.city + "</city>");
            c.append("<updateTime>" + iccidInfo.updateTime + "</updateTime>");
            c.append("</queryIccidInfoRequest>");
            return c.toString();
        } catch (Throwable th) {
            return null;
        }
    }

    public static String a(String str) {
        StringBuffer c = c();
        c.append("<QueryToken>");
        c.append("<sdkVersion>");
        c.append(DexUtil.getSceneVersion());
        c.append("</sdkVersion>");
        c.append("<iccid>" + str + "</iccid>");
        c.append("</QueryToken>");
        return c.toString();
    }

    public static String a(String str, int i) {
        StringBuffer c = c();
        c.append("<checkResourseRequest>");
        c.append("<sdk_version>");
        c.append(DexUtil.getSceneVersion());
        c.append("</sdk_version>");
        c.append("<res_type>");
        c.append(i);
        c.append("</res_type>");
        c.append("<res_version>");
        c.append(str);
        c.append("</res_version>");
        c.append("</checkResourseRequest>");
        return c.toString();
    }

    public static String a(String str, int i, int i2) {
        StringBuffer c = c();
        c.append("<UpdatePublicInfoRequest>");
        c.append("<PublicInfoVersion>");
        c.append(str);
        c.append("</PublicInfoVersion>");
        c.append("<status>");
        c.append(i);
        c.append("</status>");
        c.append("<count>");
        c.append(i2);
        c.append("</count>");
        c.append("</UpdatePublicInfoRequest>");
        return c.toString();
    }

    public static String a(String str, Object obj) {
        try {
            if (StringUtils.isNull(str) || obj == null) {
                return null;
            }
            JSONArray jSONArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("area", str);
            if (obj instanceof String) {
                if (!StringUtils.isNull((String) obj)) {
                    jSONArray.put((String) obj);
                }
            } else if (obj instanceof JSONArray) {
                JSONArray jSONArray2 = (JSONArray) obj;
                if (jSONArray2 == null || jSONArray2.length() <= 0) {
                    return null;
                }
                for (int i = 0; i < jSONArray2.length(); i++) {
                    jSONArray.put(jSONArray2.optString(i));
                }
            }
            jSONObject.put("arr", jSONArray);
            return jSONObject.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String a(String str, String str2) {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("phone", str);
            jSONObject2.put("area", str2);
            jSONArray.put(jSONObject2);
            jSONObject.put("phones", jSONArray);
        } catch (Exception e) {
        }
        return jSONObject.toString();
    }

    public static String a(String str, String str2, String str3) {
        StringBuffer c = c();
        String e = e(str2);
        if (e == null) {
            e = "";
        }
        c.append("<QueryLocationRequest>");
        c.append("<cNum>");
        c.append(str);
        c.append("</cNum>");
        c.append("<iccid>" + str2 + "</iccid>");
        c.append("<num>" + str3 + "</num>");
        c.append("<mid>" + e + "</mid>");
        c.append("</QueryLocationRequest>");
        return c.toString();
    }

    public static String a(String str, String str2, String str3, String str4) {
        StringBuffer c = c();
        c.append("<queryIccidSceneRequest>");
        c.append("<iccid>");
        c.append(str);
        c.append("</iccid>");
        c.append("<cmd>" + str2 + "</cmd>");
        c.append("<imei>" + str3 + "</imei>");
        c.append("<sceneId>" + str4 + "</sceneId>");
        c.append("</queryIccidSceneRequest>");
        return c.toString();
    }

    public static String a(String str, String str2, String str3, String str4, String str5) {
        b a = c.b() ? c.a(StringUtils.getPhoneNumberNo86(str), true) : null;
        String str6 = a != null ? a.d : "";
        try {
            StringBuffer c = c();
            c.append("<QueryPubInfoRequest>");
            c.append("<cnum>");
            if (str2 == null) {
                str2 = str6;
            }
            c.append(str2);
            c.append("</cnum>");
            c.append("<areaCode>" + str3 + "</areaCode>");
            c.append("<iccid>" + str4 + "</iccid>");
            c.append("<type>" + str5 + "</type>");
            StringBuffer stringBuffer = new StringBuffer();
            if ("1".equals(str5)) {
                if (a != null) {
                    Object obj;
                    Object obj2 = a.f == 1 ? 1 : null;
                    if (!(obj2 == null || StringUtils.isNull(a.c))) {
                        c.append("<sign>");
                        c.append(g(a.c));
                        c.append("</sign>");
                        int a2 = f.a(a.b, str3);
                        if (a2 != -1) {
                            stringBuffer.append(" f=\"").append(a2).append("\" ");
                        }
                    }
                    if (a.g != 1) {
                        obj = null;
                    } else {
                        int i = 1;
                    }
                    if (!(obj == null || StringUtils.isNull(a.e))) {
                        str6 = b(str, a.e);
                        if (!StringUtils.isNull(str6)) {
                            c.append("<unSubscribe>");
                            c.append(str6);
                            c.append("</unSubscribe>");
                        }
                    }
                    Object obj3 = a.m != 1 ? null : 1;
                    if (!(obj3 == null || StringUtils.isNull(a.l))) {
                        c.append("<ec>");
                        c.append(a.l);
                        c.append("</ec>");
                    }
                    if (obj2 != null || obj != null || obj3 != null) {
                        c.a(a.b, 0, 0);
                    }
                }
                str6 = b(str, 10);
                if (!StringUtils.isNull(str6)) {
                    c.append("<shard>");
                    c.append(str6);
                    c.append("</shard>");
                }
            }
            c.append("<num");
            if (stringBuffer.length() > 0) {
                c.append(stringBuffer);
            }
            c.append(">");
            c.append(str);
            c.append("</num>");
            c.append("</QueryPubInfoRequest>");
            return c.toString();
        } catch (Throwable th) {
            return null;
        }
    }

    public static String a(String str, String str2, Map map) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("phone", str);
            jSONObject.put(IccidInfoManager.ICCID, str2);
            if (!(map == null || map.isEmpty())) {
                jSONObject.put(IccidInfoManager.CNUM, map.get(IccidInfoManager.CNUM));
                jSONObject.put(IccidInfoManager.OPERATOR, map.get(IccidInfoManager.OPERATOR));
                jSONObject.put(DistrictSearchQuery.KEYWORDS_PROVINCE, map.get(DistrictSearchQuery.KEYWORDS_PROVINCE));
            }
            return jSONObject.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String a(String str, String[] strArr) {
        return a(str, strArr, false);
    }

    public static String a(String str, String[] strArr, boolean z) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("phone", str);
            JSONArray jSONArray = new JSONArray();
            for (Object put : strArr) {
                jSONArray.put(put);
            }
            jSONObject.put("urlLists", jSONArray);
            jSONObject.put("breviary", z);
            return jSONObject.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String a(List<SceneRule> list) {
        Object obj = null;
        if (list != null) {
            try {
                if (!list.isEmpty()) {
                    StringBuffer c = c();
                    c.append("<QuerySceneRuleRequest>");
                    c.append("<SceneRuleList>");
                    if (!(list == null || list.isEmpty())) {
                        int size = list.size();
                        int i = 0;
                        while (i < size) {
                            Object obj2;
                            SceneRule sceneRule = (SceneRule) list.get(i);
                            if (StringUtils.isNull(sceneRule.sceneruleVersion)) {
                                obj2 = obj;
                            } else {
                                c.append("<SceneRule>");
                                c.append("<id>");
                                c.append(sceneRule.id);
                                c.append("</id>");
                                c.append("<version>");
                                c.append(sceneRule.sceneruleVersion);
                                c.append("</version>");
                                c.append("</SceneRule>");
                                obj2 = 1;
                            }
                            i++;
                            obj = obj2;
                        }
                    }
                    c.append("</SceneRuleList>");
                    c.append("<clientVersion>");
                    c.append(DexUtil.getSceneVersion());
                    c.append("</clientVersion>");
                    c.append("</QuerySceneRuleRequest>");
                    return obj != null ? c.toString() : null;
                }
            } catch (Throwable th) {
                return null;
            }
        }
        return null;
    }

    private static String a(List<String> list, int i) {
        if (list.isEmpty()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String b : list) {
            String b2 = b(b2, 5);
            if (!StringUtils.isNull(b2)) {
                stringBuilder.append(b2);
            }
        }
        return stringBuilder.toString();
    }

    public static String a(List<String> list, String str) {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        int i = 0;
        while (i < list.size()) {
            try {
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("phone", list.get(i));
                jSONObject2.put("area", str);
                jSONArray.put(jSONObject2);
                i++;
            } catch (Exception e) {
            }
        }
        jSONObject.put("phones", jSONArray);
        return jSONObject.toString();
    }

    public static String a(List<String> list, String str, String str2, String str3) {
        if (list != null) {
            try {
                if (!list.isEmpty()) {
                    Map map;
                    StringBuffer c = c();
                    c.append("<QueryPubInfoRequest>");
                    c.append("<areaCode>" + str + "</areaCode>");
                    c.append("<iccid>" + str2 + "</iccid>");
                    c.append("<type>" + str3 + "</type>");
                    List<JSONObject> arrayList = new ArrayList();
                    List arrayList2 = new ArrayList();
                    List arrayList3 = new ArrayList();
                    for (String str4 : list) {
                        if (!arrayList3.contains(str4)) {
                            arrayList3.add(str4);
                            JSONObject jSONObject = new JSONObject(str4);
                            arrayList.add(jSONObject);
                            arrayList2.add(jSONObject.optString(IccidInfoManager.NUM));
                        }
                    }
                    if ("1".equals(str3)) {
                        Map a = C.a(arrayList2);
                        String a2 = a(arrayList2, 5);
                        if (!StringUtils.isNull(a2)) {
                            c.append("<shard>");
                            c.append(a2);
                            c.append("</shard>");
                        }
                        map = a;
                    } else {
                        map = null;
                    }
                    StringBuffer stringBuffer = new StringBuffer();
                    StringBuffer stringBuffer2 = new StringBuffer();
                    List arrayList4 = new ArrayList();
                    boolean b = c.b();
                    for (JSONObject jSONObject2 : arrayList) {
                        String optString = jSONObject2.optString(IccidInfoManager.NUM);
                        String optString2 = jSONObject2.optString("name");
                        String optString3 = jSONObject2.optString("cmd");
                        String optString4 = jSONObject2.optString("ec");
                        int optInt = jSONObject2.optInt("nameType", -1);
                        int optInt2 = jSONObject2.optInt("markTime");
                        int optInt3 = jSONObject2.optInt("markCmd");
                        int optInt4 = jSONObject2.optInt("markEC");
                        if (!StringUtils.isNull(optString)) {
                            StringBuffer stringBuffer3 = null;
                            if ("1".equals(str3)) {
                                Object obj;
                                Object obj2;
                                StringBuffer stringBuffer4 = new StringBuffer();
                                if (optInt2 != 1) {
                                    obj = null;
                                } else {
                                    optInt2 = 1;
                                }
                                if (!(!b || obj == null || StringUtils.isNull(optString2))) {
                                    stringBuffer4.append("\" sign=\"").append(g(optString2));
                                    if (optInt != -1) {
                                        stringBuffer4.append("\" f=\"").append(optInt);
                                    }
                                }
                                Map map2 = map != null ? (Map) map.get(optString) : null;
                                if (!(map2 == null || map2.isEmpty())) {
                                    stringBuffer4.append("\" ac=\"").append((CharSequence) map2.get("ac"));
                                    stringBuffer4.append("\" rc=\"").append((CharSequence) map2.get("rc"));
                                    stringBuffer4.append("\" dt=\"").append((CharSequence) map2.get("dt"));
                                }
                                if (optInt3 != 1) {
                                    obj2 = null;
                                } else {
                                    int i = 1;
                                }
                                if (!(obj2 == null || StringUtils.isNull(optString3))) {
                                    String b2 = b(optString, optString3);
                                    if (!StringUtils.isNull(b2)) {
                                        stringBuffer2.append(b2);
                                    }
                                }
                                Object obj3 = optInt4 != 1 ? null : 1;
                                if (!(obj3 == null || StringUtils.isNull(optString4))) {
                                    stringBuffer4.append("\" ec=\"").append(optString4);
                                }
                                if (obj != null || obj2 != null || obj3 != null) {
                                    arrayList4.add(optString);
                                }
                                stringBuffer3 = stringBuffer4;
                            }
                            stringBuffer.append("<num ver=\"").append(jSONObject2.optString(NumberInfo.VERSION_KEY));
                            if (stringBuffer3 != null && stringBuffer3.length() > 0) {
                                stringBuffer.append(stringBuffer3);
                            }
                            stringBuffer.append("\" >");
                            stringBuffer.append(optString);
                            stringBuffer.append("</num>");
                        }
                    }
                    if (stringBuffer.length() > 0) {
                        c.append("<allNums>");
                        c.append(stringBuffer);
                        c.append("</allNums>");
                    }
                    if (stringBuffer2.length() > 0) {
                        c.append("<unSubscribe>");
                        c.append(stringBuffer2);
                        c.append("</unSubscribe>");
                    }
                    c.append("</QueryPubInfoRequest>");
                    if (arrayList4.size() > 0) {
                        c.a(arrayList4, 0, 0);
                    }
                    return c.toString();
                }
            } catch (Throwable th) {
                return null;
            }
        }
        return null;
    }

    public static String a(List<g> list, Map<String, String> map, String str) {
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        int i = 0;
        while (i < list.size()) {
            try {
                g gVar = (g) list.get(i);
                if (i > 0) {
                    stringBuffer.append(",");
                    stringBuffer2.append(",");
                }
                stringBuffer.append(gVar.b);
                Object obj = gVar.b;
                if (obj.startsWith("PU")) {
                    obj = obj.replace("PU", "");
                }
                String str2 = (String) map.get(obj);
                if (!StringUtils.isNull(str2)) {
                    stringBuffer.append(str2);
                }
                stringBuffer2.append(gVar.c);
                String str3 = gVar.b;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("update_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
                    DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str3});
                } catch (Throwable th) {
                }
                i++;
            } catch (Throwable th2) {
                return null;
            }
        }
        StringBuffer c = c();
        c.append("<UpdateRecognitionJarRequest>");
        c.append("<reqVersion>");
        c.append(DexUtil.getSuanfaVersion());
        c.append("</reqVersion>");
        c.append("<jarVersion>");
        c.append(stringBuffer2.toString());
        c.append("</jarVersion>");
        c.append("<jarname>");
        c.append(stringBuffer.toString());
        c.append("</jarname>");
        c.append("<emVer>");
        c.append(str);
        c.append("</emVer>");
        c.append("</UpdateRecognitionJarRequest>");
        return c.toString();
    }

    public static String a(JSONArray jSONArray) {
        try {
            JSONArray jSONArray2 = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                jSONArray2.put(jSONArray.getJSONObject(i).get(Constant.URLS));
            }
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("urlLists", jSONArray2);
            return jSONObject.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static Map<String, JSONObject> a(Document document) {
        Map<String, JSONObject> hashMap = new HashMap();
        NodeList elementsByTagName = document.getElementsByTagName("info");
        Element documentElement = document.getDocumentElement();
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            JSONObject jSONObject = new JSONObject();
            Element element = (Element) elementsByTagName.item(i);
            String attribute = element.getAttribute("pubId");
            NodeList childNodes = element.getChildNodes();
            try {
                jSONObject.put("id", G.a(documentElement, "rstCode"));
                jSONObject.put("rid", element.getAttribute("rid"));
                jSONObject.put("logoType", "0");
            } catch (Exception e) {
            }
            for (int i2 = 0; i2 < childNodes.getLength(); i2++) {
                Node item = childNodes.item(i2);
                if (item.getNodeType() == (short) 1) {
                    String nodeName = item.getNodeName();
                    try {
                        if ("pubId".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("pubId", G.a(item));
                        } else if ("pubName".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("pubName", G.a(item));
                        } else if ("pubType".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("pubType", G.a(item));
                        } else if ("pubTypeCode".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("classifyCode", G.a(item));
                        } else if ("weiXin".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("weiXin", G.a(item));
                        } else if ("weiBoName".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("weiBoName", G.a(item));
                        } else if ("weiBoUrl".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("weiBoUrl", G.a(item));
                        } else if ("introduce".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("introduce", G.a(item));
                        } else if ("address".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("address", G.a(item));
                        } else if ("faxNum".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("faxNum", G.a(item));
                        } else if ("website".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("webSite", G.a(item));
                        } else if ("versionCode".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("versionCode", G.a(item));
                        } else if (Scopes.EMAIL.equalsIgnoreCase(nodeName)) {
                            jSONObject.put(Scopes.EMAIL, G.a(item));
                        } else if ("parentPubId".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("parentPubId", G.a(item));
                        } else if ("slogan".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("slogan", G.a(item));
                        } else if ("rectLogoName".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("rectLogoName", G.a(item));
                        } else if ("circleLogoName".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("circleLogoName", G.a(item));
                        } else if ("extend".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("extend", G.a(item));
                        } else if ("moveWebsite".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("moveWebsite", G.a(item));
                        } else if ("corpLevel".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("corpLevel", G.a(item));
                        } else if ("istl".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("logoType", G.a(item));
                        } else if ("scale".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("scale", G.a(item));
                        } else if ("backColor".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("backColor", G.a(item));
                        } else if ("backColorEnd".equalsIgnoreCase(nodeName)) {
                            jSONObject.put("backColorEnd", G.a(item));
                        }
                    } catch (JSONException e2) {
                    }
                }
            }
            hashMap.put(attribute, jSONObject);
        }
        return hashMap;
    }

    private static void a(H h, String str, int i) {
        a.a.execute(new l(str, h, i));
    }

    private static void a(Map<String, JSONObject> map, Document document) {
        NodeList elementsByTagName = document.getElementsByTagName("menuList");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            JSONObject jSONObject;
            JSONArray jSONArray = new JSONArray();
            HashMap hashMap = new HashMap();
            Element element = (Element) elementsByTagName.item(i);
            String attribute = element.getAttribute("pubId");
            NodeList elementsByTagName2 = element.getElementsByTagName("menu");
            for (int i2 = 0; i2 < elementsByTagName2.getLength(); i2++) {
                String nodeName;
                NodeList childNodes = ((Element) elementsByTagName2.item(i2)).getChildNodes();
                JSONObject jSONObject2 = new JSONObject();
                for (int i3 = 0; i3 < childNodes.getLength(); i3++) {
                    Node item = childNodes.item(i3);
                    if (item.getNodeType() == (short) 1) {
                        nodeName = item.getNodeName();
                        try {
                            if ("menuCode".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("menuCode", G.a(item));
                            } else if ("menuName".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("menuName", G.a(item));
                            } else if ("menuDesc".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("menuDesc", G.a(item));
                            } else if ("menuType".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("menuType", G.a(item));
                            } else if ("sendTo".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("sendTo", G.a(item));
                            } else if ("sp".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("sp", G.a(item));
                            } else if ("sms".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("sms", G.a(item));
                            } else if (Constant.URLS.equalsIgnoreCase(nodeName)) {
                                jSONObject2.put(Constant.URLS, G.a(item));
                            } else if ("phoneNum".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("phoneNum", G.a(item));
                            } else if ("extend".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("extend", G.a(item));
                            } else if ("extendVal".equalsIgnoreCase(nodeName)) {
                                jSONObject2.put("extendVal", G.a(item));
                            }
                        } catch (Throwable th) {
                            new StringBuilder("ServerUtil generateMenuList error: ").append(th.getMessage());
                        }
                    }
                }
                jSONObject2.put("pubId", attribute);
                String optString = jSONObject2.optString("menuType");
                Object optString2 = jSONObject2.optString("actionData");
                if (!"menu".equalsIgnoreCase(optString) && StringUtils.isNull(optString2)) {
                    optString2 = f.a(optString, jSONObject2);
                }
                jSONObject2.put("actionData", optString2);
                try {
                    String str = "name";
                    nodeName = NumberInfo.TYPE_KEY;
                    String str2 = "action_data";
                    String str3 = "secondmenu";
                    String str4 = "pubId";
                    String str5 = "extend";
                    String str6 = "menuCode";
                    String optString3 = jSONObject2.optString("menuCode");
                    String optString4 = jSONObject2.optString("menuName");
                    String optString5 = jSONObject2.optString("extend");
                    String optString6 = jSONObject2.optString("pubId");
                    JSONObject jsonObject;
                    if (optString3.length() == 2) {
                        if ("menu".equalsIgnoreCase(optString)) {
                            jsonObject = JsonUtil.getJsonObject(jSONObject2, str6, optString3, str4, optString6, str5, optString5, str, optString4, nodeName, optString);
                            jSONObject = (JSONObject) hashMap.get(optString3);
                            optString2 = jSONObject == null ? null : jSONObject.optJSONArray(str3);
                            if (optString2 == null) {
                                optString2 = new JSONArray();
                            }
                            jsonObject.put(str3, optString2);
                            hashMap.put(optString3, jsonObject);
                            optString2 = jsonObject;
                        } else {
                            optString2 = JsonUtil.getJsonObject(jSONObject2, str6, optString3, str4, optString6, str5, optString5, str, optString4, nodeName, optString, str2, optString2);
                        }
                        if (optString2 != null) {
                            jSONArray.put(optString2);
                        }
                    } else if (optString3.length() == 4) {
                        jsonObject = JsonUtil.getJsonObject(jSONObject2, str6, optString3, str4, optString6, str5, optString5, str, optString4, nodeName, optString, str2, optString2);
                        str = optString3.substring(0, 2);
                        jSONObject = (JSONObject) hashMap.get(str);
                        if (jSONObject == null) {
                            jSONObject = new JSONObject();
                            JSONArray jSONArray2 = new JSONArray();
                            jSONArray2.put(jsonObject);
                            jSONObject.put(str3, jSONArray2);
                            hashMap.put(str, jSONObject);
                        } else {
                            jSONObject.optJSONArray(str3).put(jsonObject);
                        }
                    }
                } catch (Throwable th2) {
                }
            }
            hashMap.clear();
            jSONObject = (JSONObject) map.get(attribute);
            if (jSONObject != null) {
                jSONObject.put("pubMenuInfolist", jSONArray);
            }
        }
    }

    private static void a(Element element) {
        a(element, "rstSign", H.UPLOAD_PUBINFO_SIGN, 0);
    }

    private static void a(Element element, String str, H h, int i) {
        if (element != null) {
            try {
                if (!StringUtils.isNull(str)) {
                    NodeList elementsByTagName = element.getElementsByTagName(str);
                    if (elementsByTagName != null && elementsByTagName.getLength() != 0) {
                        String a = G.a(elementsByTagName.item(0));
                        if (!StringUtils.isNull(a)) {
                            a.a.execute(new l(a, h, 0));
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static String b() {
        if (StringUtils.isNull(k)) {
            IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
            if (!(queryDeftIccidInfo == null || StringUtils.isNull(queryDeftIccidInfo.iccid))) {
                k = m.a(queryDeftIccidInfo.iccid);
            }
        }
        return !StringUtils.isNull(k) ? k : "";
    }

    private static String b(String str, int i) {
        if (StringUtils.isNull(str) || i <= 0) {
            return null;
        }
        List a;
        if (m.STATUS_NOT_REQUEST == m.ALL) {
            a = cn.com.xy.sms.sdk.db.entity.a.l.a("num=? ", new String[]{str}, i);
        } else {
            a = cn.com.xy.sms.sdk.db.entity.a.l.a("num=? AND status=? ", new String[]{str, m.STATUS_NOT_REQUEST.toString()}, i);
        }
        if (a == null || a.isEmpty()) {
            return null;
        }
        try {
            List arrayList = new ArrayList();
            CharSequence stringBuilder = new StringBuilder();
            int size = a.size();
            for (int i2 = 0; i2 < size; i2++) {
                if (i2 > 0) {
                    stringBuilder.append(";");
                }
                String str2 = ((k) a.get(i2)).d;
                stringBuilder.append(str2);
                arrayList.add(str2);
            }
            cn.com.xy.sms.sdk.db.entity.a.l.a(arrayList, m.STATUS_HAS_REQUEST);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("<shardSign sign=\"").append(stringBuilder).append("\">");
            stringBuilder2.append(str);
            stringBuilder2.append("</shardSign>");
            return stringBuilder2.toString();
        } catch (Throwable th) {
            return null;
        }
    }

    private static String b(String str, String str2) {
        if (StringUtils.isNull(str) || StringUtils.isNull(str2)) {
            return null;
        }
        CharSequence stringBuilder = new StringBuilder();
        for (String str3 : str2.split(";&XY_PIX&;")) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(";");
            }
            stringBuilder.append(f(str3));
        }
        if (stringBuilder.length() == 0) {
            return null;
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("<numSign sign=\"").append(stringBuilder).append("\">");
        stringBuilder2.append(str);
        stringBuilder2.append("</numSign>");
        return stringBuilder2.toString();
    }

    public static String b(String str, String str2, String str3, String str4) {
        StringBuffer c = c();
        c.append("<QueryCheciRequest>");
        c.append("<cc>" + str + "</cc>");
        c.append("<d>" + str2 + "</d>");
        if (!StringUtils.isNull(str3)) {
            c.append("<ss>" + str3 + "</ss>");
        }
        if (!StringUtils.isNull(str4)) {
            c.append("<ft>" + str4 + "</ft>");
        }
        c.append("</QueryCheciRequest>");
        return c.toString();
    }

    public static String b(String str, String str2, Map map) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("phone", str);
            jSONObject.put("msg", str2);
            if (!(map == null || map.isEmpty())) {
                jSONObject.put(IccidInfoManager.CNUM, map.get(IccidInfoManager.CNUM));
            }
            return jSONObject.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String b(List<A> list) {
        if (list != null) {
            try {
                if (!list.isEmpty()) {
                    StringBuffer c = c();
                    c.append("<QuerySceneRequest>");
                    c.append("<SceneList>");
                    if (!(list == null || list.isEmpty())) {
                        int size = list.size();
                        for (int i = 0; i < size; i++) {
                            A a = (A) list.get(i);
                            c.append("<Scene count='" + a.c + "'>");
                            c.append("<sceneId >");
                            c.append(a.a);
                            c.append("</sceneId>");
                            c.append("<sceneVersion>");
                            String str = a.b;
                            if (StringUtils.isNull(str)) {
                                str = ThemeUtil.SET_NULL_STR;
                            }
                            c.append(str);
                            c.append("</sceneVersion>");
                            c.append("</Scene>");
                        }
                    }
                    c.append("</SceneList>");
                    c.append("<clientVersion>");
                    c.append(DexUtil.getSceneVersion());
                    c.append("</clientVersion>");
                    c.append("</QuerySceneRequest>");
                    return c.toString();
                }
            } catch (Throwable th) {
                return null;
            }
        }
        return null;
    }

    public static Map<String, JSONObject> b(String str) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        int i = j;
        HashMap hashMap = new HashMap();
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return null;
            }
            Element documentElement = stringConvertXML.getDocumentElement();
            if (G.a(G.a(documentElement, "rstCode")) == j) {
                return null;
            }
            Map a = a(stringConvertXML);
            b(a, stringConvertXML);
            a(a, stringConvertXML);
            a(documentElement, "rstSign", H.UPLOAD_PUBINFO_SIGN, 0);
            a(documentElement, "subSign", H.UPLOAD_PUBINFO_CMD, 0);
            if (documentElement != null) {
                try {
                    NodeList elementsByTagName = documentElement.getElementsByTagName("shaSign");
                    if (!(elementsByTagName == null || elementsByTagName.getLength() == 0)) {
                        elementsByTagName = elementsByTagName.item(0).getChildNodes();
                        if (!(elementsByTagName == null || elementsByTagName.getLength() == 0)) {
                            a.a.execute(new k(elementsByTagName));
                        }
                    }
                } catch (Throwable th) {
                }
            }
            return a;
        } catch (Throwable th2) {
            return null;
        }
    }

    private static void b(Map<String, JSONObject> map, Document document) {
        NodeList elementsByTagName = document.getElementsByTagName("pubNumList");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element element = (Element) elementsByTagName.item(i);
            String attribute = element.getAttribute("pubId");
            NodeList elementsByTagName2 = element.getElementsByTagName("pubNum");
            JSONArray jSONArray = new JSONArray();
            for (int i2 = 0; i2 < elementsByTagName2.getLength(); i2++) {
                element = (Element) elementsByTagName2.item(i2);
                NodeList childNodes = element.getChildNodes();
                JSONObject jSONObject = new JSONObject();
                if (element.hasAttribute("f")) {
                    jSONObject.put("nameType", element.getAttribute("f"));
                }
                for (int i3 = 0; i3 < childNodes.getLength(); i3++) {
                    Node item = childNodes.item(i3);
                    if (item.getNodeType() == (short) 1) {
                        String nodeName = item.getNodeName();
                        try {
                            if (IccidInfoManager.NUM.equalsIgnoreCase(nodeName)) {
                                jSONObject.put(IccidInfoManager.NUM, G.a(item));
                            } else if ("purpose".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("purpose", G.a(item));
                            } else if ("areaCode".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("areaCode", G.a(item));
                            } else if (NumberInfo.TYPE_KEY.equalsIgnoreCase(nodeName)) {
                                jSONObject.put(NumberInfo.TYPE_KEY, G.a(item));
                            } else if ("main".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("main", G.a(item));
                            } else if ("communication".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("communication", G.a(item));
                            } else if ("extend".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("extend", G.a(item));
                            } else if ("ntype".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("ntype", G.a(item));
                            } else if ("len".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("len", G.a(item));
                            } else if ("maxlen".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("maxlen", G.a(item));
                            } else if ("minlen".equalsIgnoreCase(nodeName)) {
                                jSONObject.put("minlen", G.a(item));
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
                if (!StringUtils.isNull(attribute)) {
                    jSONObject.put("pubId", attribute);
                }
                jSONArray.put(jSONObject);
            }
            JSONObject jSONObject2 = (JSONObject) map.get(attribute);
            if (jSONObject2 != null) {
                jSONObject2.put("pubNumInfolist", jSONArray);
            }
        }
    }

    private static void b(Element element) {
        a(element, "subSign", H.UPLOAD_PUBINFO_CMD, 0);
    }

    public static cn.com.xy.sms.sdk.db.entity.a c(String str) {
        cn.com.xy.sms.sdk.db.entity.a aVar = new cn.com.xy.sms.sdk.db.entity.a();
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return aVar;
            }
            Element documentElement = stringConvertXML.getDocumentElement();
            int a = G.a(G.a(documentElement, "rstCode"));
            if (a != 0) {
                aVar.a = a;
            } else {
                aVar.c = G.a(documentElement, IccidInfoManager.AREACODE);
                aVar.d = G.a(documentElement, DistrictSearchQuery.KEYWORDS_PROVINCE);
                aVar.e = G.a(documentElement, "city");
                aVar.f = G.a(documentElement, IccidInfoManager.OPERATOR);
            }
            return aVar;
        } catch (Throwable th) {
        }
    }

    private static String c(String str, String str2, String str3, String str4) {
        StringBuffer c = c();
        c.append("<QueryPubInfoRequest>");
        c.append("<pubId>");
        c.append(str);
        c.append("</pubId>");
        c.append("<version>" + str2 + "</version>");
        c.append("<areaCode>" + str3 + "</areaCode>");
        c.append("<iccid>" + str4 + "</iccid>");
        c.append("</QueryPubInfoRequest>");
        return c.toString();
    }

    public static String c(List<q> list) {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        int i = 0;
        while (i < list.size()) {
            try {
                q qVar = (q) list.get(i);
                if (qVar != null) {
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("phone", qVar.a);
                    jSONObject2.put("area", qVar.b);
                    jSONObject2.put(NumberInfo.VERSION_KEY, qVar.d);
                    jSONArray.put(jSONObject2);
                }
                i++;
            } catch (Exception e) {
            }
        }
        jSONObject.put("phones", jSONArray);
        return jSONObject.toString();
    }

    private static StringBuffer c() {
        return new StringBuffer("<?xml version='1.0' encoding='utf-8'?>");
    }

    private static void c(Element element) {
        if (element != null) {
            try {
                NodeList elementsByTagName = element.getElementsByTagName("shaSign");
                if (elementsByTagName != null && elementsByTagName.getLength() != 0) {
                    elementsByTagName = elementsByTagName.item(0).getChildNodes();
                    if (elementsByTagName != null && elementsByTagName.getLength() != 0) {
                        a.a.execute(new k(elementsByTagName));
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static String d(String str) {
        Document stringConvertXML = StringUtils.stringConvertXML(str, "");
        return stringConvertXML != null ? G.a(stringConvertXML.getDocumentElement(), NetUtil.REQ_QUERY_TOEKN) : null;
    }

    private static String d(String str, String str2, String str3, String str4) {
        StringBuffer c = c();
        c.append("<QueryMenuInfoRequest>");
        c.append("<pubId>");
        c.append(str);
        c.append("</pubId>");
        c.append("<version>" + str2 + "</version>");
        c.append("<areaCode>" + str3 + "</areaCode>");
        c.append("<iccid>" + str4 + "</iccid>");
        c.append("</QueryMenuInfoRequest>");
        return c.toString();
    }

    private static String e(String str) {
        String str2 = null;
        try {
            String str3;
            Object obj;
            Object obj2;
            JSONObject telephonyInfoBySimIndex = DuoquUtils.getSdkDoAction().getTelephonyInfoBySimIndex(0);
            JSONObject telephonyInfoBySimIndex2 = DuoquUtils.getSdkDoAction().getTelephonyInfoBySimIndex(1);
            if (telephonyInfoBySimIndex == null) {
                str3 = null;
                obj = null;
            } else {
                String optString = telephonyInfoBySimIndex.optString(IccidInfoManager.ICCID);
                str3 = telephonyInfoBySimIndex.optString("mid");
            }
            if (telephonyInfoBySimIndex2 == null) {
                obj2 = null;
            } else {
                obj2 = telephonyInfoBySimIndex2.optString(IccidInfoManager.ICCID);
                str2 = telephonyInfoBySimIndex2.optString("mid");
            }
            if (!StringUtils.isNull(str)) {
                if (str.equals(obj) && !StringUtils.isNull(str3)) {
                    return str3;
                }
                if (str.equals(obj2) && !StringUtils.isNull(str2)) {
                    return str2;
                }
            }
            return StringUtils.isNull(str3) ? StringUtils.isNull(str2) ? ((TelephonyManager) Constant.getContext().getSystemService("phone")).getSubscriberId() : str2 : str3;
        } catch (Throwable th) {
            return "";
        }
    }

    private static String f(String str) {
        if (StringUtils.isNull(str)) {
            return "";
        }
        try {
            return m.a(str.trim());
        } catch (Exception e) {
            return "";
        }
    }

    private static String g(String str) {
        if (StringUtils.isNull(str)) {
            return "";
        }
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (String str2 : str.split(";")) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(";");
                }
                stringBuilder.append(m.a(str2.trim()));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            new StringBuilder("ServerUtil getSha256EncodeSafeName: ").append(e.getMessage());
            return "";
        }
    }
}
