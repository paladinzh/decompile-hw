package cn.com.xy.sms.util;

import android.os.Process;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.PhoneSmsParseManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.g;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class a extends Thread {
    private static final Object a = new Object();
    private static boolean b = false;
    private static boolean c = false;
    private static long j = 0;
    private static Map k = null;
    private static int l = 0;
    private static Map<String, Object> m = new HashMap();
    private boolean d = false;
    private String e;
    private int f;
    private int g;
    private int h = 0;
    private boolean i = false;

    private a() {
    }

    private a(boolean z, String str, int i, int i2, boolean z2) {
        this.d = z;
        this.e = str;
        this.f = i;
        this.g = i2;
        this.i = z2;
        setName("before_parse_thread");
    }

    public static void a() {
        synchronized (a) {
            b = true;
        }
    }

    public static void a(boolean z, String str, int i, int i2, boolean z2) {
        a aVar = new a(z, str, i, i2, z2);
        aVar.setPriority(1);
        aVar.start();
    }

    private static boolean b() {
        int i = 0;
        while (true) {
            if (ParseItemManager.isInitData() && !c) {
                return true;
            }
            try {
                sleep(1000);
                i++;
                if (i > 30) {
                    return false;
                }
                if (g.a && !c) {
                    return true;
                }
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    private void c() {
        if (!b && j == Thread.currentThread().getId()) {
            long j = 0;
            if (this.d) {
                j = SysParamEntityManager.getLongParam(Constant.BEFORE_HAND_PARSE_SMS_TIME, 0, Constant.getContext());
            } else {
                JSONObject findObjectByPhone = PhoneSmsParseManager.findObjectByPhone(this.e);
                if (findObjectByPhone != null) {
                    Object valFromJsonObject = JsonUtil.getValFromJsonObject(findObjectByPhone, "maxReceiveTime");
                    if (valFromJsonObject != null) {
                        j = Long.valueOf(valFromJsonObject.toString()).longValue();
                    }
                }
            }
            if (j == 0) {
                j = System.currentTimeMillis() + 2147483647L;
            }
            List receiveMsgByReceiveTime = DuoquUtils.getSdkDoAction().getReceiveMsgByReceiveTime(this.e, 0, j, this.f);
            if (receiveMsgByReceiveTime != null && !receiveMsgByReceiveTime.isEmpty()) {
                long j2;
                int size = receiveMsgByReceiveTime.size();
                k = d();
                Object hashMap = new HashMap();
                if (k != null) {
                    hashMap.putAll(k);
                }
                int i = 0;
                long j3 = j;
                while (i < size) {
                    JSONObject jSONObject = (JSONObject) receiveMsgByReceiveTime.get(i);
                    j = Long.valueOf((String) JsonUtil.getValFromJsonObject(jSONObject, "smsReceiveTime")).longValue();
                    j2 = ((j > j3 ? 1 : (j == j3 ? 0 : -1)) >= 0 ? 1 : null) == null ? j : j3;
                    hashMap.put(Constant.KEY_IS_SAFE_VERIFY_CODE, (String) JsonUtil.getValFromJsonObject(jSONObject, Constant.KEY_IS_SAFE_VERIFY_CODE));
                    ParseSmsToBubbleUtil.parseSmsToBubbleResultMap((String) JsonUtil.getValFromJsonObject(jSONObject, "msgId"), (String) JsonUtil.getValFromJsonObject(jSONObject, "phone"), (String) JsonUtil.getValFromJsonObject(jSONObject, "msg"), (String) JsonUtil.getValFromJsonObject(jSONObject, "centerNum"), j, this.g, this.d, this.i, hashMap);
                    this.h++;
                    if (this.d) {
                        SysParamEntityManager.setParam(Constant.BEFORE_HAND_PARSE_SMS_TIME, String.valueOf(j2));
                    }
                    sleep(1);
                    if (b || j != Thread.currentThread().getId()) {
                        break;
                    }
                    i++;
                    j3 = j2;
                }
                j2 = j3;
                if (this.d) {
                    if (i != size || size % 10 != 0) {
                        SysParamEntityManager.setParam(Constant.BEFORE_HAND_PARSE_SMS_TIME, String.valueOf(j2));
                    }
                }
            }
        }
    }

    private synchronized Map d() {
        if (k == null) {
            try {
                String extendConfig = DuoquUtils.getSdkDoAction().getExtendConfig(1, null);
                if (!StringUtils.isNull(extendConfig)) {
                    k = JsonUtil.parseJSON2Map(extendConfig);
                }
            } catch (Throwable th) {
            }
        }
        if (k == null) {
            int i = l + 1;
            l = i;
            if (i > 5) {
                k = new HashMap();
            }
        }
        return k;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        try {
            m.clear();
            m.put(IccidInfoManager.NUM, this.e);
            m.put("maxLimit", Integer.valueOf(this.f));
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "executeBeforeParse start ", m);
            Process.setThreadPriority(10);
            Thread.currentThread().setPriority(1);
            synchronized (a) {
                if (c) {
                    b = true;
                }
            }
            if (b()) {
                synchronized (a) {
                    if (b) {
                        synchronized (a) {
                            c = false;
                            b = false;
                            j = 0;
                        }
                        m.put("parseCount", Integer.valueOf(this.h));
                        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "executeBeforeParse end ", m);
                        return;
                    }
                    c = true;
                    b = false;
                    j = Thread.currentThread().getId();
                }
            } else {
                synchronized (a) {
                    c = false;
                    b = false;
                    j = 0;
                }
                m.put("parseCount", Integer.valueOf(this.h));
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "executeBeforeParse end ", m);
            }
        } catch (Throwable th) {
            synchronized (a) {
                c = false;
                b = false;
                j = 0;
                m.put("parseCount", Integer.valueOf(this.h));
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "executeBeforeParse end ", m);
            }
        }
    }
}
