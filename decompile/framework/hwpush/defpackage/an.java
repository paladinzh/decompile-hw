package defpackage;

import android.content.Context;
import android.util.Log;
import com.huawei.android.pushagent.datatype.pushmessage.DecoupledPushMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import org.json.JSONArray;
import org.json.JSONObject;

/* renamed from: an */
public class an {
    private static an bu = null;

    private an() {
    }

    private void a(JSONObject jSONObject, Context context) {
        Log.d("PushLog2841", "server command agent to refresh token");
        JSONObject jSONObject2 = jSONObject.getJSONObject("refreshToken");
        if (jSONObject2.has("pkgs")) {
            JSONArray jSONArray = jSONObject2.getJSONArray("pkgs");
            String[] strArr = new String[jSONArray.length()];
            String str = "";
            for (int i = 0; i < jSONArray.length(); i++) {
                String string = jSONArray.getString(i);
                Log.d("PushLog2841", "package need to refresh token:" + string);
                strArr[i] = string;
            }
            ao.a(context, strArr);
            return;
        }
        Log.d("PushLog2841", "all packages need to refresh token");
        ao.B(context);
    }

    private void b(JSONObject jSONObject, Context context) {
        Log.d("PushLog2841", "server command agent to refresh trs");
        JSONObject jSONObject2 = jSONObject.getJSONObject("refreshTrs");
        if (jSONObject2.has("belongId")) {
            int i = jSONObject2.getInt("belongId");
            Log.d("PushLog2841", "need to refresh trs in belongId:" + i);
            if (i >= 0) {
                ae.l(context).a("belongId", (Object) Integer.valueOf(i));
            }
        }
        ae.m(context);
    }

    public static synchronized an bI() {
        an anVar;
        synchronized (an.class) {
            if (bu != null) {
                anVar = bu;
            } else {
                bu = new an();
                anVar = bu;
            }
        }
        return anVar;
    }

    private void c(JSONObject jSONObject, Context context) {
        Log.d("PushLog2841", "server command agent to refresh heartbeat");
        JSONObject jSONObject2 = jSONObject.getJSONObject("refreshHb");
        if (jSONObject2.has("fixedWifiHb")) {
            int i = jSONObject2.getInt("fixedWifiHb");
            Log.d("PushLog2841", "fixed heartbeat in wifi is " + i);
            if (i > 60) {
                ae.l(context).b((long) i);
                ae.l(context).c((long) i);
            } else {
                Log.d("PushLog2841", "fixed heartbeat in wifi is invalid");
            }
        }
        if (jSONObject2.has("fixed3GHb")) {
            int i2 = jSONObject2.getInt("fixed3GHb");
            Log.d("PushLog2841", "fixed heartbeat in 3g is " + i2);
            if (i2 > 60) {
                ae.l(context).d((long) i2);
                ae.l(context).e((long) i2);
            } else {
                Log.d("PushLog2841", "fixed heartbeat in 3g is invalid");
            }
        }
        try {
            Log.d("PushLog2841", "delete heartbeat files and reload heartbeat");
            au.n(context, new y(context).be());
            ChannelMgr.aX().T.bh();
        } catch (Throwable e) {
            Log.e("PushLog2841", "delete heartbeat files or reload heartbeat error:" + e.getMessage(), e);
        }
    }

    public void a(Context context, DecoupledPushMessage decoupledPushMessage) {
        try {
            byte k = decoupledPushMessage.k();
            if ((byte) -91 == k) {
                Log.d("PushLog2841", "receive response from server");
            } else if ((byte) -90 == k) {
                JSONObject aG = decoupledPushMessage.aG();
                if (aG.has("refreshHb")) {
                    c(aG, context);
                } else if (aG.has("refreshToken")) {
                    a(aG, context);
                } else if (aG.has("refreshTrs")) {
                    b(aG, context);
                } else {
                    Log.e("PushLog2841", "cannot parse the unknown message:" + aG.toString());
                }
            }
        } catch (Throwable e) {
            Log.e("PushLog2841", "parse json error:" + e.getMessage(), e);
        } catch (Throwable e2) {
            Log.e("PushLog2841", "parse DecoupledPushMessage error: " + e2.getMessage(), e2);
        }
    }
}
