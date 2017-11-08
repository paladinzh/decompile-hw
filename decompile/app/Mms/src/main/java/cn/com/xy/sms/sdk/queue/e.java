package cn.com.xy.sms.sdk.queue;

import android.os.Process;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.y;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.queue.a.a;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.f;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class e extends Thread {
    private static boolean a = false;

    public static synchronized void a() {
        synchronized (e.class) {
            if (!a) {
                if (NetUtil.checkAccessNetWork(2)) {
                    new e().start();
                }
            }
        }
    }

    private static void a(int i) {
        int f;
        try {
            if (NetUtil.checkAccessNetWork(2)) {
                JSONArray a = y.a(1);
                if (a != null && a.length() > 0) {
                    int length = a.length();
                    for (int i2 = 0; i2 < length; i2++) {
                        JSONObject jSONObject = a.getJSONObject(i2);
                        String str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_url");
                        Integer num = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "res_version");
                        Long l = (Long) JsonUtil.getValueFromJsonObject(jSONObject, "down_failed_time");
                        Integer num2 = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "id");
                        if (!StringUtils.isNull(str)) {
                            long currentTimeMillis = System.currentTimeMillis();
                            String str2 = "1" + "_" + num + "_" + currentTimeMillis + ".zip";
                            if ((currentTimeMillis <= l.longValue() + DexUtil.getUpdateCycleByType(17, 3600000) ? 1 : null) == null) {
                                f = f.f(str, Constant.getPath("duoqu_temp"), str2);
                                if (f != 0) {
                                    y.a(num2, false, str2);
                                    return;
                                }
                                y.a(num2, true, str2);
                            } else {
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    private static void a(String str, int i) {
        try {
            XyCallBack fVar = new f(i);
            if (!StringUtils.isNull(str) && NetUtil.checkAccessNetWork(2)) {
                String a = j.a(str, i);
                if (!StringUtils.isNull(a)) {
                    NetUtil.executePubNumServiceHttpRequest(a, "990005", fVar, "", true, false, NetUtil.CheckResourseRequest, true);
                }
            }
        } catch (Throwable th) {
        }
    }

    private static void b(int i) {
        String name;
        try {
            List a = a.a(new StringBuilder("1").toString());
            if (!(a == null || a.isEmpty())) {
                Iterator it = a.iterator();
                while (it != null && it.hasNext()) {
                    File file = (File) it.next();
                    name = file.getName();
                    XyUtil.upZipFile(file, Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR));
                    try {
                        file.delete();
                        y.a(true, name);
                    } catch (Throwable th) {
                    }
                }
            }
        } catch (Exception e) {
            y.a(false, name);
        } catch (Throwable th2) {
        }
    }

    private static void c(int i) {
        if (NetUtil.checkAccessNetWork(2) && a.a(1)) {
            String b = y.b(1);
            if (StringUtils.isNull(b)) {
                b = ThemeUtil.SET_NULL_STR;
            }
            try {
                XyCallBack fVar = new f(1);
                if (!StringUtils.isNull(b) && NetUtil.checkAccessNetWork(2)) {
                    b = j.a(b, 1);
                    if (!StringUtils.isNull(b)) {
                        NetUtil.executePubNumServiceHttpRequest(b, "990005", fVar, "", true, false, NetUtil.CheckResourseRequest, true);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public final void run() {
        try {
            setName("xiaoyuan_resoursequeue");
            Process.setThreadPriority(i.b);
            if (!a) {
                a = true;
                try {
                    String b;
                    Thread.sleep(1000);
                    if (NetUtil.checkAccessNetWork(2)) {
                        if (a.a(1)) {
                            b = y.b(1);
                            if (StringUtils.isNull(b)) {
                                b = ThemeUtil.SET_NULL_STR;
                            }
                            a(b, 1);
                        }
                    }
                    Thread.sleep(1000);
                    int f;
                    try {
                        if (NetUtil.checkAccessNetWork(2)) {
                            JSONArray a = y.a(1);
                            if (a != null) {
                                if (a.length() > 0) {
                                    int length = a.length();
                                    for (int i = 0; i < length; i++) {
                                        JSONObject jSONObject = a.getJSONObject(i);
                                        b = (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_url");
                                        Integer num = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "res_version");
                                        Long l = (Long) JsonUtil.getValueFromJsonObject(jSONObject, "down_failed_time");
                                        Integer num2 = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "id");
                                        if (!StringUtils.isNull(b)) {
                                            long currentTimeMillis = System.currentTimeMillis();
                                            String str = "1" + "_" + num + "_" + currentTimeMillis + ".zip";
                                            if ((currentTimeMillis <= l.longValue() + DexUtil.getUpdateCycleByType(17, 3600000) ? 1 : null) != null) {
                                                break;
                                            }
                                            f = f.f(b, Constant.getPath("duoqu_temp"), str);
                                            if (f != 0) {
                                                y.a(num2, false, str);
                                                break;
                                            }
                                            y.a(num2, true, str);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable th) {
                    }
                    Thread.sleep(1000);
                    b(1);
                } catch (Throwable th2) {
                }
                a = false;
            }
        } catch (Throwable th3) {
        }
    }
}
