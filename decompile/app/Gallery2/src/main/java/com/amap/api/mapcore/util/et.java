package com.amap.api.mapcore.util;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: CoreUtil */
public class et {
    private static String[] a = new String[]{"com.amap.api.trace", "com.amap.api.trace.core"};

    public static void a(String str, String str2) throws eq {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("status") && jSONObject.has("infocode")) {
                String string = jSONObject.getString("status");
                int i = jSONObject.getInt("infocode");
                if (!string.equals("1") && string.equals("0")) {
                    switch (i) {
                        case 10001:
                            throw new eq("用户key不正确或过期");
                        case 10002:
                            throw new eq("请求服务不存在");
                        case 10003:
                            throw new eq("访问已超出日访问量");
                        case 10004:
                            throw new eq("用户访问过于频繁");
                        case 10005:
                            throw new eq("用户IP无效");
                        case 10006:
                            throw new eq("用户域名无效");
                        case 10007:
                            throw new eq("用户签名未通过");
                        case 10008:
                            throw new eq("用户MD5安全码未通过");
                        case 10009:
                            throw new eq("请求key与绑定平台不符");
                        case 10010:
                            throw new eq("IP访问超限");
                        case 10011:
                            throw new eq("服务不支持https请求");
                        case 10012:
                            throw new eq("权限不足，服务请求被拒绝");
                        case 10013:
                            throw new eq("开发者删除了key，key被删除后无法正常使用");
                        case 20000:
                            throw new eq("请求参数非法");
                        case 20001:
                            throw new eq("缺少必填参数");
                        case 20002:
                            throw new eq("请求协议非法");
                        case 20003:
                            throw new eq("其他未知错误");
                        case 30000:
                            throw new eq("请求服务响应错误");
                        case 30001:
                            throw new eq("引擎返回数据异常");
                        case 30002:
                            throw new eq("服务端请求链接超时");
                        case 30003:
                            throw new eq("读取服务结果超时");
                        default:
                            throw new eq(jSONObject.getString("info"));
                    }
                    throw new eq("协议解析错误 - ProtocolException");
                }
            }
        } catch (JSONException e) {
            throw new eq("协议解析错误 - ProtocolException");
        }
    }

    public static int a(List<LatLng> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        int i = 0;
        int i2 = 0;
        while (i < list.size() - 1) {
            LatLng latLng = (LatLng) list.get(i);
            LatLng latLng2 = (LatLng) list.get(i + 1);
            if (latLng == null || latLng2 == null) {
                return i2;
            }
            i++;
            i2 = (int) (AMapUtils.calculateLineDistance(latLng, latLng2) + ((float) i2));
        }
        return i2;
    }
}
