package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

/* renamed from: ag */
public class ag {
    private static ag aR = null;
    private static final HashMap aT = new HashMap();
    private HashMap aS = new HashMap();
    private Context context = null;

    static {
        ag.bv();
    }

    private ag(Context context) {
        this.context = context;
        init();
    }

    public static int a(Context context, String str, int i) {
        try {
            Object c = ag.c(context, str);
            if (c != null) {
                i = ((Integer) c).intValue();
            }
        } catch (Exception e) {
        }
        return i;
    }

    public static long a(Context context, String str, long j) {
        try {
            Object c = ag.c(context, str);
            if (c != null) {
                j = ((Long) c).longValue();
            }
        } catch (Exception e) {
        }
        return j;
    }

    public static String a(Context context, String str, String str2) {
        Object c = ag.c(context, str);
        if (c == null) {
            return str2;
        }
        String str3;
        try {
            str3 = (String) c;
        } catch (Exception e) {
            aw.d("PushLog2841", "getString from config failed!");
            str3 = str2;
        }
        return str3;
    }

    public static void a(Context context, g gVar) {
        if (gVar == null || gVar.q == null) {
            aw.e("PushLog2841", "set value err, cfg is null or itemName is null, cfg:" + gVar);
        } else if (ag.n(context) == null) {
            aw.e("PushLog2841", "System init failed in set Value");
        } else {
            aR.aS.put(gVar.q, gVar);
            aR.b(context, gVar);
        }
    }

    public static boolean a(Context context, String str, boolean z) {
        try {
            Object c = ag.c(context, str);
            if (c != null) {
                z = ((Boolean) c).booleanValue();
            }
        } catch (Exception e) {
        }
        return z;
    }

    private boolean b(Context context, g gVar) {
        if (context == null) {
            context = this.context;
        }
        bt btVar = new bt(context, "pushConfig");
        if (Boolean.class == gVar.s) {
            btVar.a(gVar.q, ((Boolean) gVar.r).booleanValue());
        } else if (String.class == gVar.s) {
            btVar.f(gVar.q, (String) gVar.r);
        } else if (Long.class == gVar.s) {
            btVar.a(gVar.q, (Long) gVar.r);
        } else if (Integer.class == gVar.s) {
            btVar.a(gVar.q, (Integer) gVar.r);
        } else if (Float.class == gVar.s) {
            btVar.a(gVar.q, (Float) gVar.r);
        }
        return true;
    }

    private static void bv() {
        aT.clear();
        aT.put("cloudpush_isLogLocal", new g("cloudpush_isLogLocal", Boolean.class, Boolean.valueOf(false)));
        aT.put("cloudpush_pushLogLevel", new g("cloudpush_pushLogLevel", Integer.class, Integer.valueOf(4)));
        aT.put("cloudpush_isReportLog", new g("cloudpush_isReportLog", Boolean.class, Boolean.valueOf(false)));
        aT.put("cloudpush_isNoDelayConnect", new g("cloudpush_isNoDelayConnect", Boolean.class, Boolean.valueOf(false)));
        aT.put("cloudpush_isSupportUpdate", new g("cloudpush_isSupportUpdate", Boolean.class, Boolean.valueOf(false)));
        aT.put("cloudpush_isSupportCollectSocketInfo", new g("cloudpush_isSupportCollectSocketInfo", Boolean.class, Boolean.valueOf(false)));
        aT.put("cloudpush_trsIp", new g("cloudpush_trsIp", String.class, "push.hicloud.com"));
        aT.put("cloudpush_fixHeatBeat", new g("cloudpush_fixHeatBeat", String.class, " unit sec"));
        aT.put("USE_SSL", new g("USE_SSL", Integer.class, Integer.valueOf(ChannelType.ChannelType_Secure.ordinal())));
    }

    private void bx() {
        this.aS.clear();
        this.aS.putAll(aT);
        for (Entry entry : new bt(this.context, "pushConfig").getAll().entrySet()) {
            this.aS.put(entry.getKey(), new g((String) entry.getKey(), entry.getValue().getClass(), entry.getValue()));
        }
    }

    private static Object c(Context context, String str) {
        if (ag.n(context) == null) {
            return null;
        }
        g gVar = (g) aR.aS.get(str);
        return gVar == null ? null : gVar.r;
    }

    private static g d(Context context, String str) {
        if (ag.n(context) == null || str == null) {
            return null;
        }
        g gVar = (g) aR.aS.get(str);
        return gVar == null ? null : gVar;
    }

    public static synchronized ag n(Context context) {
        ag agVar;
        synchronized (ag.class) {
            if (aR != null) {
                agVar = aR;
            } else if (context == null) {
                agVar = null;
            } else {
                aR = new ag(context);
                agVar = aR;
            }
        }
        return agVar;
    }

    public static ChannelType o(Context context) {
        g d = ag.d(context, "USE_SSL");
        ChannelType channelType = ChannelType.ChannelType_Secure;
        if (d == null) {
            return channelType;
        }
        aw.d("PushLog2841", " " + d);
        Integer num = (Integer) d.r;
        if (num.intValue() >= 0 && num.intValue() < ChannelType.values().length) {
            return ChannelType.values()[num.intValue()];
        }
        aw.e("PushLog2841", "useSSL:" + d.r + " is invalid cfg");
        return channelType;
    }

    public void bw() {
        bt btVar = new bt(this.context, "pushConfig");
        Set<String> keySet = this.aS.keySet();
        LinkedList linkedList = new LinkedList();
        for (String str : keySet) {
            if (!(aT.containsKey(str) || "NeedMyServiceRun".equals(str) || "version_config".equals(str))) {
                aw.d("PushLog2841", "item " + str + " remove from " + "pushConfig" + " in deleteNoSysCfg");
                linkedList.add(str);
                btVar.z(str);
            }
        }
        Iterator it = linkedList.iterator();
        while (it.hasNext()) {
            this.aS.remove((String) it.next());
        }
    }

    public void init() {
        ag.bv();
        bx();
    }
}
