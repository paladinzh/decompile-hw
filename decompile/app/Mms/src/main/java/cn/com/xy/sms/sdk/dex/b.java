package cn.com.xy.sms.sdk.dex;

import android.content.Context;
import android.os.Process;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
final class b implements Runnable {
    private final /* synthetic */ Context a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ long f;
    private final /* synthetic */ Map g;
    private final /* synthetic */ JSONObject h;

    b(Context context, String str, String str2, String str3, String str4, long j, Map map, JSONObject jSONObject) {
        this.a = context;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = j;
        this.g = map;
        this.h = jSONObject;
    }

    public final void run() {
        try {
            Process.setThreadPriority(19);
            Class classBymap = DexUtil.getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilConversation");
            if (classBymap != null) {
                classBymap.getMethod("handleParseMsg", new Class[]{Context.class, String.class, String.class, String.class, String.class, Long.TYPE, Map.class, JSONObject.class}).invoke(classBymap, new Object[]{this.a, this.b, this.c, this.d, this.e, Long.valueOf(this.f), this.g, this.h});
            }
        } catch (Throwable th) {
        }
    }
}
