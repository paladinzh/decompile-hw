package cn.com.xy.sms.sdk.queue.a;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.f;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public final class a {
    private static int a = 1;

    public static String a() {
        return "3531333036463338";
    }

    public static List<File> a(String str) {
        return f.e(Constant.getPath("duoqu_temp"), new StringBuilder(String.valueOf(str)).append("_").toString(), ".zip");
    }

    public static void a(String str, String str2) {
        List a = a(str);
        if (a != null && !a.isEmpty()) {
            Iterator it = a.iterator();
            while (it != null && it.hasNext()) {
                File file = (File) it.next();
                if (new StringBuilder(String.valueOf(str)).append("_").append(str2).append("_").toString().compareTo(file.getName()) < 0) {
                    it.remove();
                } else {
                    file.delete();
                }
            }
        }
    }

    public static boolean a(int i) {
        try {
            int intParam = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.ONLINE_UPDATE_RES_PERIOD);
            if (intParam <= 0) {
                intParam = 2;
            }
            return !((System.currentTimeMillis() > (SysParamEntityManager.getLongParam(new StringBuilder("LastCheckResourseTime_").append(i).toString(), 0, Constant.getContext()) + DexUtil.getUpdateCycleByType(9, ((long) intParam) * 86400000)) ? 1 : (System.currentTimeMillis() == (SysParamEntityManager.getLongParam(new StringBuilder("LastCheckResourseTime_").append(i).toString(), 0, Constant.getContext()) + DexUtil.getUpdateCycleByType(9, ((long) intParam) * 86400000)) ? 0 : -1)) <= 0);
        } catch (Throwable th) {
        }
    }

    public static void b(int i) {
        SysParamEntityManager.setParam("LastCheckResourseTime_" + i, new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
    }
}
