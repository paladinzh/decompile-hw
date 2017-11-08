package cn.com.xy.sms.sdk.net.util;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.A;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SceneRule;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.util.G;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.amap.api.services.district.DistrictSearchQuery;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* compiled from: Unknown */
public final class i {
    private static int a(String str, IccidInfo iccidInfo) {
        int i = -1;
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return -1;
            }
            Element documentElement = stringConvertXML.getDocumentElement();
            i = Integer.parseInt(a(documentElement, "rstCode"));
            if (i == 1) {
                iccidInfo.iccid = a(documentElement, IccidInfoManager.ICCID);
                iccidInfo.operator = a(documentElement, IccidInfoManager.OPERATOR);
                iccidInfo.provinces = a(documentElement, IccidInfoManager.PROVINCES);
                iccidInfo.city = a(documentElement, "city");
                iccidInfo.updateTime = Long.parseLong(a(documentElement, IccidInfoManager.UPDATE_TIME));
            }
            return i;
        } catch (Throwable th) {
        }
    }

    private static String a(Element element, String str) {
        NodeList elementsByTagName = element.getElementsByTagName(str);
        return (elementsByTagName != null && elementsByTagName.getLength() > 0) ? G.a(elementsByTagName.item(0)) : "";
    }

    public static Map<String, Object> a(String str) {
        Map<String, Object> map = null;
        long j = 0;
        Map<String, Object> hashMap;
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return null;
            }
            String attribute;
            Element documentElement = stringConvertXML.getDocumentElement();
            NodeList elementsByTagName = documentElement.getElementsByTagName("em");
            String a = a(documentElement, "jars");
            String a2 = a(documentElement, "jarVersion");
            String a3 = a(documentElement, "downLoadUrl");
            String a4 = a(documentElement, "pver");
            if (StringUtils.isNull(a) || StringUtils.isNull(a2)) {
                if (elementsByTagName != null) {
                    if (elementsByTagName.getLength() != 0) {
                    }
                }
                return null;
            }
            hashMap = new HashMap();
            long j2 = -1;
            if (elementsByTagName != null) {
                if (elementsByTagName.getLength() > 0) {
                    JSONArray jSONArray = new JSONArray();
                    int i = 0;
                    while (i < elementsByTagName.getLength()) {
                        JSONObject jSONObject = new JSONObject();
                        Node item = elementsByTagName.item(i);
                        String a5 = G.a(item);
                        attribute = ((Element) item).getAttribute(NumberInfo.VERSION_KEY);
                        jSONObject.put("emContent", a5);
                        jSONObject.put("emVersion", attribute);
                        try {
                            if (!StringUtils.isNull(attribute)) {
                                long parseLong = Long.parseLong(attribute);
                                if ((j2 >= parseLong ? 1 : null) == null) {
                                    j2 = parseLong;
                                }
                            }
                        } catch (Throwable th) {
                        }
                        try {
                            jSONArray.put(jSONObject);
                            i++;
                        } catch (Throwable th2) {
                            map = hashMap;
                        }
                    }
                    SysParamEntityManager.setParam("EM_VERSION", new StringBuilder(String.valueOf(j2)).toString());
                    hashMap.put("emergencyArray", jSONArray);
                }
            }
            if (StringUtils.isNull(a) || StringUtils.isNull(a2)) {
                return hashMap;
            }
            String[] split = a.split(",");
            String[] split2 = a2.split(",");
            String[] split3 = a3.split(",");
            String[] split4 = a4.split(",");
            if (split.length != split2.length || split2.length != split3.length) {
                return hashMap;
            }
            attribute = a(documentElement, "delaystart");
            if (!StringUtils.isNull(attribute)) {
                j = Long.parseLong(attribute);
            }
            attribute = a(documentElement, "delayend");
            long parseLong2 = StringUtils.isNull(attribute) ? 0 : Long.parseLong(attribute);
            j2 = ((parseLong2 > 0 ? 1 : (parseLong2 == 0 ? 0 : -1)) > 0 ? 1 : null) == null ? 86400000 : parseLong2;
            JSONArray jSONArray2 = new JSONArray();
            for (int i2 = 0; i2 < split.length; i2++) {
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("name", split[i2]);
                jSONObject2.put(NumberInfo.VERSION_KEY, split2[i2]);
                jSONObject2.put(Constant.URLS, split3[i2]);
                if (split4.length > i2) {
                    jSONObject2.put("pver", split4[i2]);
                }
                jSONObject2.put("delayStart", j);
                jSONObject2.put("delayEnd", j2);
                jSONArray2.put(jSONObject2);
            }
            hashMap.put("updataJars", jSONArray2);
            return hashMap;
        } catch (Throwable th3) {
            hashMap = map;
            return hashMap;
        }
    }

    public static List<SceneRule> b(String str) {
        List<SceneRule> arrayList = new ArrayList();
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return null;
            }
            NodeList elementsByTagName = stringConvertXML.getElementsByTagName("SceneRule");
            for (int i = 0; i < elementsByTagName.getLength(); i++) {
                SceneRule sceneRule = new SceneRule();
                NodeList childNodes = ((Element) elementsByTagName.item(i)).getChildNodes();
                for (int i2 = 0; i2 < childNodes.getLength(); i2++) {
                    Node item = childNodes.item(i2);
                    if (item.getNodeType() == (short) 1) {
                        String nodeName = item.getNodeName();
                        if ("sceneId".equalsIgnoreCase(nodeName)) {
                            sceneRule.scene_id = G.a(item);
                        } else if ("sceneRuleVersion".equalsIgnoreCase(nodeName)) {
                            sceneRule.sceneruleVersion = G.a(item);
                        } else if (DistrictSearchQuery.KEYWORDS_PROVINCE.equalsIgnoreCase(nodeName)) {
                            sceneRule.province = G.a(item);
                        } else if ("id".equalsIgnoreCase(nodeName)) {
                            sceneRule.id = G.a(item);
                        } else if (IccidInfoManager.OPERATOR.equalsIgnoreCase(nodeName)) {
                            sceneRule.operator = G.a(item);
                        } else if ("expire_date".equalsIgnoreCase(nodeName)) {
                            sceneRule.expire_date = G.a(item);
                        } else if ("fun_call".equalsIgnoreCase(nodeName)) {
                            sceneRule.Func_call = Integer.parseInt(G.a(item));
                        } else if ("fun_acc_url".equalsIgnoreCase(nodeName)) {
                            sceneRule.Func_acc_url = Integer.parseInt(G.a(item));
                        } else if ("fun_reply_sms".equalsIgnoreCase(nodeName)) {
                            sceneRule.Func_reply_sms = Integer.parseInt(G.a(item));
                        } else if ("fun_config".equalsIgnoreCase(nodeName)) {
                            sceneRule.Func_config = G.a(item);
                        } else if ("res_urls".equalsIgnoreCase(nodeName)) {
                            sceneRule.res_urls = G.a(item);
                        } else if ("s_version".equalsIgnoreCase(nodeName)) {
                            sceneRule.s_version = G.a(item);
                        } else if ("scene_page_conf".equalsIgnoreCase(nodeName)) {
                            sceneRule.Scene_page_config = G.a(item);
                        }
                    }
                }
                arrayList.add(sceneRule);
            }
            return arrayList;
        } catch (Throwable th) {
        }
    }

    public static boolean c(String str) {
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return false;
            }
            NodeList elementsByTagName = stringConvertXML.getDocumentElement().getElementsByTagName("rstCode");
            if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                String str2 = G.a(elementsByTagName.item(0)).toString();
                if (!StringUtils.isNull(str2) && str2.equals("0")) {
                    return true;
                }
            }
            return false;
        } catch (Throwable th) {
        }
    }

    public static boolean d(String str) {
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return false;
            }
            NodeList elementsByTagName = stringConvertXML.getDocumentElement().getElementsByTagName("rstCode");
            if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                String str2 = G.a(elementsByTagName.item(0)).toString();
                if (!StringUtils.isNull(str2) && str2.equals("0")) {
                    return true;
                }
            }
            return false;
        } catch (Throwable th) {
        }
    }

    public static HashMap<String, Object> e(String str) {
        HashMap<String, Object> hashMap = new HashMap();
        ArrayList arrayList = new ArrayList();
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return null;
            }
            int i;
            NodeList elementsByTagName = stringConvertXML.getElementsByTagName("Scene");
            for (int i2 = 0; i2 < elementsByTagName.getLength(); i2++) {
                int i3;
                A a = new A();
                Element element = (Element) elementsByTagName.item(i2);
                NodeList childNodes = element.getChildNodes();
                for (i3 = 0; i3 < childNodes.getLength(); i3++) {
                    Node item = childNodes.item(i3);
                    if (item.getNodeType() == (short) 1) {
                        String nodeName = item.getNodeName();
                        if ("sceneId".equalsIgnoreCase(nodeName)) {
                            a.a = G.a(item);
                        } else if ("sceneVersion".equalsIgnoreCase(nodeName)) {
                            a.b = G.a(item);
                        }
                    }
                }
                childNodes = element.getElementsByTagName("SceneRule");
                if (childNodes != null) {
                    int length = childNodes.getLength();
                    for (i3 = 0; i3 < length; i3++) {
                        SceneRule sceneRule = new SceneRule();
                        NodeList childNodes2 = ((Element) childNodes.item(i3)).getChildNodes();
                        for (i = 0; i < childNodes2.getLength(); i++) {
                            Node item2 = childNodes2.item(i);
                            if (item2.getNodeType() == (short) 1) {
                                String nodeName2 = item2.getNodeName();
                                if (item2.getNodeType() == (short) 1) {
                                    if ("sceneId".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.scene_id = G.a(item2);
                                    } else if ("sceneRuleVersion".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.sceneruleVersion = G.a(item2);
                                    } else if (DistrictSearchQuery.KEYWORDS_PROVINCE.equalsIgnoreCase(nodeName2)) {
                                        sceneRule.province = G.a(item2);
                                    } else if ("id".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.id = G.a(item2);
                                    } else if (IccidInfoManager.OPERATOR.equalsIgnoreCase(nodeName2)) {
                                        sceneRule.operator = G.a(item2);
                                    } else if ("expire_date".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.expire_date = G.a(item2);
                                    } else if ("fun_call".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.Func_call = Integer.parseInt(G.a(item2));
                                    } else if ("fun_acc_url".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.Func_acc_url = Integer.parseInt(G.a(item2));
                                    } else if ("fun_reply_sms".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.Func_reply_sms = Integer.parseInt(G.a(item2));
                                    } else if ("fun_config".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.Func_config = G.a(item2);
                                    } else if ("res_urls".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.res_urls = G.a(item2);
                                    } else if ("s_version".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.s_version = G.a(item2);
                                    } else if ("scene_page_conf".equalsIgnoreCase(nodeName2)) {
                                        sceneRule.Scene_page_config = G.a(item2);
                                    }
                                }
                            }
                        }
                        a.a(sceneRule);
                    }
                }
                arrayList.add(a);
            }
            NodeList elementsByTagName2 = stringConvertXML.getElementsByTagName("SceneUrl");
            ArrayList arrayList2 = new ArrayList();
            if (elementsByTagName2 != null) {
                for (i = 0; i < elementsByTagName2.getLength(); i++) {
                    String a2 = G.a(elementsByTagName2.item(i));
                    if (!StringUtils.isNull(a2)) {
                        arrayList2.add(a2);
                    }
                }
            }
            hashMap.put("sceneUrllist", arrayList2);
            hashMap.put("sceneconfigList", arrayList);
            return hashMap;
        } catch (Throwable th) {
        }
    }

    public static JSONArray f(String str) {
        try {
            JSONArray jSONArray = new JSONArray();
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return null;
            }
            String str2;
            Element documentElement = stringConvertXML.getDocumentElement();
            NodeList elementsByTagName = documentElement.getElementsByTagName("code");
            if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                str2 = G.a(elementsByTagName.item(0)).toString();
                if (StringUtils.isNull(str2) || !"0".equals(str2)) {
                    return null;
                }
            }
            str2 = "";
            NodeList elementsByTagName2 = documentElement.getElementsByTagName("res_type");
            if (elementsByTagName2 != null && elementsByTagName2.getLength() > 0) {
                str2 = G.a(elementsByTagName2.item(0)).toString();
            }
            String str3 = str2;
            NodeList elementsByTagName3 = stringConvertXML.getElementsByTagName("res");
            for (int i = 0; i < elementsByTagName3.getLength(); i++) {
                JSONObject jSONObject = new JSONObject();
                Node node = (Element) elementsByTagName3.item(i);
                String attribute = node.getAttribute(NumberInfo.VERSION_KEY);
                String attribute2 = node.getAttribute("del_history");
                jSONObject.put("res_version", attribute);
                jSONObject.put("del_history", attribute2);
                jSONObject.put("res_url", G.a(node));
                jSONObject.put("res_type", str3);
                jSONArray.put(jSONObject);
            }
            return jSONArray;
        } catch (Throwable th) {
            return null;
        }
    }

    public static JSONObject g(String str) {
        JSONObject jSONObject = null;
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return null;
            }
            NodeList elementsByTagName = stringConvertXML.getDocumentElement().getElementsByTagName(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
            if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                String str2 = G.a(elementsByTagName.item(0)).toString();
                if (!StringUtils.isNull(str2)) {
                    jSONObject = new JSONObject(str2);
                }
            }
            return jSONObject;
        } catch (Throwable th) {
        }
    }
}
