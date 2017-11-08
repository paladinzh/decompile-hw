package cn.com.xy.sms.sdk.queue;

import android.content.Intent;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.util.ParseSmsToBubbleUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;

/* compiled from: Unknown */
public class BubbleTaskQueue {
    private static BlockingQueue<JSONObject> a = new LinkedBlockingQueue();
    private static Thread b = null;

    static /* synthetic */ void a(JSONObject jSONObject) {
        boolean z = true;
        ParseSmsToBubbleUtil.parseSmsToBubbleResultMap((String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), ((Integer) JsonUtil.getValueFromJsonObject(jSONObject, "dataType")).intValue(), true, z, c(jSONObject));
        try {
            Integer num = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "isNeedNotify");
            if (num != null && 1 == num.intValue()) {
                try {
                    Constant.getContext().sendBroadcast(new Intent("cn.com.xy.douqu.reflashlist"));
                } catch (Throwable th) {
                }
            }
        } catch (Throwable th2) {
        }
    }

    public static void addDataToQueue(int i, String str, String str2, String str3, String str4, long j, int i2, JSONObject jSONObject) {
        addDataToQueue(i, str, str2, str3, str4, j, i2, jSONObject, 0);
    }

    public static void addDataToQueue(int i, String str, String str2, String str3, String str4, long j, int i2, JSONObject jSONObject, int i3) {
        if (jSONObject == null) {
            jSONObject = new JSONObject();
        }
        try {
            jSONObject.put("dataStatu", i);
            jSONObject.put("msg_id", str);
            jSONObject.put("phoneNum", str2);
            jSONObject.put("smsContent", str3);
            jSONObject.put("smsReceiveTime", j);
            if (str4 != null) {
                jSONObject.put("centerNum", str4);
            }
            jSONObject.put("dataType", i2);
            jSONObject.put("isNeedNotify", i3);
            a.put(jSONObject);
            startTaskQueue();
        } catch (Throwable th) {
        }
    }

    private static void b(JSONObject jSONObject) {
        boolean z = true;
        ParseSmsToBubbleUtil.parseSmsToBubbleResultMap((String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), ((Integer) JsonUtil.getValueFromJsonObject(jSONObject, "dataType")).intValue(), true, z, c(jSONObject));
        try {
            Integer num = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "isNeedNotify");
            if (num != null && 1 == num.intValue()) {
                try {
                    Constant.getContext().sendBroadcast(new Intent("cn.com.xy.douqu.reflashlist"));
                } catch (Throwable th) {
                }
            }
        } catch (Throwable th2) {
        }
    }

    private static Map<String, String> c(JSONObject jSONObject) {
        try {
            Map<String, String> hashMap = new HashMap();
            hashMap.put("isUseNewAction", JsonUtil.getValueFromJsonObject(jSONObject, "isUseNewAction"));
            hashMap.put("msgTime", JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime"));
            return hashMap;
        } catch (Throwable th) {
            return null;
        }
    }

    public static synchronized void startTaskQueue() {
        synchronized (BubbleTaskQueue.class) {
            if (b == null) {
                Thread aVar = new a();
                b = aVar;
                aVar.start();
            }
        }
    }
}
