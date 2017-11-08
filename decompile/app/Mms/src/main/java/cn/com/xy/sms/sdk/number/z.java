package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.c;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.util.E;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class z {
    static /* synthetic */ Map a(String str, String str2, int i) {
        Map hashMap = new HashMap();
        hashMap.put(str, new String[]{str2, String.valueOf(i)});
        return hashMap;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(String str) {
        XyCursor xyCursor = null;
        if (!StringUtils.isNull(str)) {
            try {
                xyCursor = c.a("tb_number_info", new String[]{"result"}, "num = ? ", new String[]{str});
                if (xyCursor != null) {
                    if (xyCursor.getCount() > 0 && xyCursor.moveToFirst()) {
                        n.a(new JSONObject(xyCursor.getString(xyCursor.getColumnIndex("result"))));
                        String[] strArr = new String[]{str};
                        c.a("tb_number_info", BaseManager.getContentValues(null, "result", r2.toString()), "num = ? ", strArr);
                    }
                }
                XyCursor.closeCursor(xyCursor, true);
            } catch (Throwable th) {
                Throwable th2 = th;
                XyCursor xyCursor2 = xyCursor;
                Throwable th3 = th2;
                XyCursor.closeCursor(xyCursor2, true);
                throw th3;
            }
        }
        k.f(str);
    }

    public static void a(String str, String str2, int i, Map<String, String> map, XyCallBack xyCallBack) {
        JSONObject a = n.a(str, str2, i);
        if (a != null) {
            k.a(str, a);
            k.d(str);
            try {
                E.e.execute(new A(str, str2, i, map, xyCallBack));
                return;
            } catch (Throwable th) {
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, th.getMessage());
                return;
            }
        }
        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, "save user tag fail");
    }

    private static Map<String, String[]> b(String str, String str2, int i) {
        Map<String, String[]> hashMap = new HashMap();
        hashMap.put(str, new String[]{str2, String.valueOf(i)});
        return hashMap;
    }
}
