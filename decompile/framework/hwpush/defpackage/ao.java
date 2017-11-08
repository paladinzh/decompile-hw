package defpackage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.pushmessage.DecoupledPushMessage;
import com.huawei.android.pushagent.datatype.pushmessage.RegisterTokenReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.UnRegisterReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/* renamed from: ao */
public class ao {
    private static ao bv = null;
    public HashMap bw = new HashMap();

    private ao(Context context) {
        aw.d("PushLog2841", "DeviceTokenMgr: create the DeviceTokenMgr");
        bt btVar = new bt(context, "pclient_info_v2");
        if (this.bw == null) {
            aw.e("PushLog2841", "create TokenMap occur an error!!");
            return;
        }
        Set<String> keySet = btVar.getAll().keySet();
        if (keySet != null && keySet.size() > 0) {
            for (String str : keySet) {
                CharSequence s = az.s(context, str);
                if (TextUtils.isEmpty(s)) {
                    btVar.z(str);
                } else {
                    this.bw.put(s, str);
                    aw.i("PushLog2841", str + " has registed token");
                }
            }
        }
    }

    public static ArrayList A(Context context) {
        bt btVar = new bt(context, "pclient_info_v2");
        ArrayList arrayList = new ArrayList();
        List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(new Intent("com.huawei.android.push.intent.RECEIVE"), 787072, ActivityManager.getCurrentUser());
        int size = queryBroadcastReceivers == null ? 0 : queryBroadcastReceivers.size();
        if (size == 0) {
            aw.d("PushLog2841", "we have no push client");
            return arrayList;
        }
        String E = au.E(context);
        if (E == null) {
            aw.e("PushLog2841", "have no deviceId, when queryAllClientForRegister");
            return arrayList;
        }
        ArrayList arrayList2 = new ArrayList();
        Set<String> keySet = btVar.getAll().keySet();
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) queryBroadcastReceivers.get(i);
            ComponentInfo componentInfo = resolveInfo.activityInfo != null ? resolveInfo.activityInfo : resolveInfo.serviceInfo;
            if (!(componentInfo == null || TextUtils.isEmpty(componentInfo.packageName) || "com.huawei.android.pushagent".equals(componentInfo.packageName))) {
                arrayList2.add(componentInfo.packageName);
            }
        }
        ao.a(context, E, arrayList, arrayList2);
        if (keySet != null && keySet.size() > 0) {
            for (String str : keySet) {
                String o = au.o(str);
                int p = au.p(str);
                if (!arrayList2.contains(o)) {
                    if (p == ActivityManager.getCurrentUser()) {
                        String s = az.s(context, str);
                        aw.i("PushLog2841", "this package [" + o + "] need to unregister device token");
                        arrayList.add(new UnRegisterReqMessage(s));
                        ao.f(context, str);
                    } else {
                        aw.d("PushLog2841", "apk not found in this user, but other user maybe exist:" + str);
                    }
                }
            }
        }
        if (arrayList != null && arrayList.size() == 0) {
            aw.d("PushLog2841", "there is no more client need register and unregister token");
        }
        return arrayList;
    }

    public static void B(Context context) {
        bt btVar = new bt(context, "pclient_info_v2");
        Map all = btVar.getAll();
        if (all != null && all.size() > 0) {
            bt btVar2 = new bt(context, "pclient_request_info");
            for (String str : all.keySet()) {
                btVar2.f(str, "true");
                aw.d("PushLog2841", str + " need to register again");
            }
            btVar.clear();
            ao.z(context).bw.clear();
        }
    }

    private static void a(Context context, String str, ArrayList arrayList, ArrayList arrayList2) {
        bt btVar = new bt(context, "pclient_request_info");
        if (!ao.d(context, "com.huawei.android.hwouc", String.valueOf(ActivityManager.getCurrentUser())) && new bt(context, "push_notify_key").containsKey("com.huawei.android.hwouc")) {
            aw.i("PushLog2841", "need register token for hwouc");
            btVar.f("com.huawei.android.hwouc", "true");
        }
        for (String str2 : btVar.getAll().keySet()) {
            Object obj = str2.split("/")[0];
            int p = au.p(str2);
            if (arrayList2.contains(obj) || p != ActivityManager.getCurrentUser()) {
                PushMessage c = ao.c(context, str, str2);
                if (c != null) {
                    arrayList.add(c);
                } else {
                    btVar.z(str2);
                }
            } else {
                btVar.z(str2);
            }
        }
    }

    static void a(Context context, String[] strArr) {
        if (strArr != null && strArr.length != 0) {
            bt btVar = new bt(context, "pclient_info_v2");
            bt btVar2 = new bt(context, "pclient_request_info");
            Set<String> keySet = btVar.getAll().keySet();
            for (String str : strArr) {
                aw.d("PushLog2841", str + " need to register again");
                String s = az.s(context, str);
                for (String str2 : keySet) {
                    if (str2.startsWith(str)) {
                        btVar.z(str2);
                        ao.z(context).bw.remove(s);
                        btVar2.f(str2, "true");
                    }
                }
            }
        }
    }

    private static PushMessage c(Context context, String str, String str2) {
        if (TextUtils.isEmpty(str2)) {
            aw.d("PushLog2841", "packageName is null when buildRegisterReqMsg");
            return null;
        }
        String[] split = str2.split("/");
        if (split.length <= 1) {
            return new RegisterTokenReqMessage(str, au.o(context, str2));
        }
        Object obj = split[0];
        Object obj2 = split[1];
        PushMessage decoupledPushMessage = new DecoupledPushMessage((byte) -92);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("cmdid", -36);
            jSONObject.put("packageName", obj);
            jSONObject.put("usrid", obj2);
            decoupledPushMessage.a(jSONObject);
            return decoupledPushMessage;
        } catch (Throwable e) {
            aw.d("PushLog2841", "create DecoupledPushMessage params error:" + e.toString(), e);
            return null;
        }
    }

    public static boolean d(Context context, String str, String str2) {
        bt btVar = new bt(context, "pclient_info_v2");
        String b = au.b(str, str2);
        return (btVar == null || !btVar.containsKey(b) || TextUtils.isEmpty(btVar.getString(b))) ? false : true;
    }

    public static void e(Context context, String str, String str2) {
        if (ao.z(context).bw == null) {
            aw.e("PushLog2841", "responseRegisterToken the map is null!!! ");
            return;
        }
        ao.z(context).bw.put(str, str2);
        az.l(context, str2, str);
    }

    public static void f(Context context, String str) {
        if (ao.z(context).bw == null) {
            aw.e("PushLog2841", "when removeClientInfo, tokenMap the map is null!!! ");
            return;
        }
        bt btVar = new bt(context, "pclient_info_v2");
        aw.d("PushLog2841", "remove package:" + str);
        btVar.z(str);
    }

    public static void g(Context context, String str) {
        String str2 = "";
        if (ao.z(context).bw != null) {
            str2 = (String) ao.z(context).bw.get(str);
        }
        bt btVar = new bt(context, "pclient_info_v2");
        aw.d("PushLog2841", "responseUnregisterToken,after delPClientInfo token,  packagename: " + str2);
        ao.z(context).bw.remove(str);
        btVar.z(str2);
    }

    public static String h(Context context, String str) {
        return az.s(context, str);
    }

    public static synchronized ao z(Context context) {
        ao aoVar;
        synchronized (ao.class) {
            if (bv != null) {
                aoVar = bv;
            } else {
                bv = new ao(context);
                aoVar = bv;
            }
        }
        return aoVar;
    }
}
