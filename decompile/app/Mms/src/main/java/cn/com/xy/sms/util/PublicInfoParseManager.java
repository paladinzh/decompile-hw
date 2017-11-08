package cn.com.xy.sms.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.a.c;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.service.e.g;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.f;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class PublicInfoParseManager {
    private static HashMap<String, Long> a = new HashMap();
    public static long mins = 1;

    private static File a(String str) {
        File file = new File(str);
        return !file.exists() ? null : file;
    }

    private static boolean a(String str, String str2, String str3) {
        return NetUtil.checkAccessNetWork(2) && f.a(str, str2, str3, true) == 0;
    }

    private static BitmapDrawable b(Context context, String str, String str2, String str3, String str4, int i, int i2, SdkCallBack sdkCallBack) {
        BitmapDrawable bitmapDrawable = null;
        try {
            if (NetUtil.checkAccessNetWork(2)) {
                a.put(str4, Long.valueOf(System.currentTimeMillis()));
                if (f.a(str, str2, str3, true) == 0) {
                    a.remove(str4);
                    bitmapDrawable = ViewUtil.createBitmapByPath2(context, str4, i, i2);
                    if (sdkCallBack != null) {
                        sdkCallBack.execute(bitmapDrawable);
                    }
                    if (bitmapDrawable != null) {
                        a.remove(str4);
                    }
                } else if (sdkCallBack != null) {
                    sdkCallBack.execute(null);
                }
                return bitmapDrawable;
            }
            if (sdkCallBack != null) {
                sdkCallBack.execute(null);
            }
            return null;
        } catch (Throwable th) {
        }
    }

    public static BitmapDrawable findBitmapLogoByLogoName(Context context, String str, int i, int i2, Map<String, String> map, SdkCallBack sdkCallBack) {
        BitmapDrawable createBitmapByPath2;
        BitmapDrawable bitmapDrawable;
        Throwable th;
        Object obj = null;
        String a = w.a();
        Long l;
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.util.PublicInfoParseManager", "findBitmapLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), map, sdkCallBack);
            String path = Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR);
            String stringBuilder = new StringBuilder(String.valueOf(path)).append(str).toString();
            File file = new File(stringBuilder);
            if (file.exists()) {
                createBitmapByPath2 = ViewUtil.createBitmapByPath2(context, file, i, i2);
                if (createBitmapByPath2 == null) {
                    try {
                        a.put(stringBuilder, Long.valueOf(System.currentTimeMillis()));
                        XyUtil.doXycallBackResult(sdkCallBack, null);
                        ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.PublicInfoParseManager", "findBitmapLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), map, sdkCallBack, createBitmapByPath2);
                        l = (Long) a.get("runResourseQueue");
                        if (l != null) {
                            if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                            }
                            return null;
                        }
                        i.a(new k(7, new String[0]));
                        a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                        return null;
                    } catch (Throwable th2) {
                        bitmapDrawable = createBitmapByPath2;
                        th = th2;
                        ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.PublicInfoParseManager", "findBitmapLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), map, sdkCallBack, obj);
                        l = (Long) a.get("runResourseQueue");
                        if (l != null) {
                            if ((System.currentTimeMillis() > l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                            }
                            throw th;
                        }
                        i.a(new k(7, new String[0]));
                        a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                        throw th;
                    }
                }
                XyUtil.doXycallBackResult(sdkCallBack, createBitmapByPath2);
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.PublicInfoParseManager", "findBitmapLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), map, sdkCallBack, createBitmapByPath2);
                l = (Long) a.get("runResourseQueue");
                if (l != null) {
                    if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                    }
                    return createBitmapByPath2;
                }
                i.a(new k(7, new String[0]));
                a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                return createBitmapByPath2;
            }
            l = (Long) a.get(stringBuilder);
            if (l != null) {
                if ((System.currentTimeMillis() >= l.longValue() + DexUtil.getUpdateCycleByType(19, (mins * 60) * 1000) ? 1 : null) == null) {
                    XyUtil.doXycallBackResult(sdkCallBack, null);
                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.PublicInfoParseManager", "findBitmapLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), map, sdkCallBack, null);
                    l = (Long) a.get("runResourseQueue");
                    if (l != null) {
                        if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                        }
                        return null;
                    }
                    i.a(new k(7, new String[0]));
                    a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                    return null;
                }
            }
            a.b().execute(new t(context, NetUtil.BIZPORT_DOWN_URL + str, path, str, stringBuilder, i, i2, sdkCallBack));
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.PublicInfoParseManager", "findBitmapLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), map, sdkCallBack, null);
            l = (Long) a.get("runResourseQueue");
            if (l != null) {
                if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                }
                i.a(new k(12, ParseItemManager.STATE, "256"));
                return bitmapDrawable;
            }
            i.a(new k(7, new String[0]));
            a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
            i.a(new k(12, ParseItemManager.STATE, "256"));
            return bitmapDrawable;
        } catch (Throwable th22) {
            th = th22;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.PublicInfoParseManager", "findBitmapLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), map, sdkCallBack, obj);
            l = (Long) a.get("runResourseQueue");
            if (l != null) {
                if (System.currentTimeMillis() > l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000)) {
                }
                if ((System.currentTimeMillis() > l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                }
                throw th;
            }
            i.a(new k(7, new String[0]));
            a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
            throw th;
        }
    }

    public static File findLogoFile(String str) {
        Long l;
        try {
            String path = Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR);
            String stringBuilder = new StringBuilder(String.valueOf(path)).append(str).toString();
            File a = a(stringBuilder);
            if (a == null) {
                Object obj;
                l = (Long) a.get(stringBuilder);
                if (l != null) {
                    if ((System.currentTimeMillis() >= l.longValue() + DexUtil.getUpdateCycleByType(19, (mins * 60) * 1000) ? 1 : null) == null) {
                        l = (Long) a.get("runResourseQueue");
                        if (l != null) {
                            if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                            }
                            return null;
                        }
                        i.a(new k(7, new String[0]));
                        a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                        return null;
                    }
                }
                String str2 = NetUtil.BIZPORT_DOWN_URL + str;
                if (NetUtil.checkAccessNetWork(2)) {
                    if (f.a(str2, path, str, true) == 0) {
                        obj = 1;
                        if (obj != null) {
                            a.put(stringBuilder, Long.valueOf(System.currentTimeMillis()));
                        } else {
                            a = a(stringBuilder);
                        }
                    }
                }
                obj = null;
                if (obj != null) {
                    a = a(stringBuilder);
                } else {
                    a.put(stringBuilder, Long.valueOf(System.currentTimeMillis()));
                }
            }
            l = (Long) a.get("runResourseQueue");
            if (l != null) {
                if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                }
                return a;
            }
            i.a(new k(7, new String[0]));
            a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
            return a;
        } catch (Throwable th) {
            Throwable th2 = th;
            l = (Long) a.get("runResourseQueue");
            if (l != null) {
                if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                }
            }
            i.a(new k(7, new String[0]));
            a.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
        }
    }

    public static JSONObject getJSONObject(String... strArr) {
        int i = 0;
        if (strArr == null || strArr.length <= 0 || strArr.length % 2 != 0) {
            return null;
        }
        JSONObject jSONObject = new JSONObject();
        while (i < strArr.length) {
            try {
                jSONObject.put(strArr[i], strArr[i + 1]);
                i += 2;
            } catch (Exception e) {
            }
        }
        return jSONObject;
    }

    public static String getLogoNameByNum(Context context, String str, int i, int i2, String str2, Map<String, String> map, SdkCallBack sdkCallBack) {
        try {
            if (!n.a((byte) 5)) {
                return null;
            }
            String a = g.a(str, i, str2, (Map) map, new u(i2, sdkCallBack));
            if (!StringUtils.isNull(a)) {
                JSONObject jSONObject = new JSONObject(a);
                return i2 != 1 ? jSONObject.optString("logoc") : jSONObject.optString(NumberInfo.LOGO_KEY);
            }
            return "";
        } catch (Throwable th) {
        }
    }

    public static JSONObject getNameAndLogoNameByNum(Context context, String str, int i, int i2, String str2, Map<String, String> map, SdkCallBack sdkCallBack) {
        try {
            if (!n.a((byte) 5)) {
                return null;
            }
            String a = g.a(str, i, str2, (Map) map, sdkCallBack == null ? null : new v(i2, sdkCallBack));
            if (!StringUtils.isNull(a)) {
                JSONObject jSONObject = new JSONObject(a);
                if (i2 != 1) {
                    return getJSONObject("name", jSONObject.optString("name"), NumberInfo.LOGO_NAME_KEY, jSONObject.optString("logoc"));
                }
                return getJSONObject("name", jSONObject.optString("name"), NumberInfo.LOGO_NAME_KEY, jSONObject.optString(NumberInfo.LOGO_KEY));
            }
            return null;
        } catch (Throwable th) {
        }
    }

    public static String queryLocalSmsSignByNum(String str) {
        String b = c.b(str);
        return b != null ? b : "";
    }

    public static Drawable queryLogoByPhone(Context context, String str, int i, int i2, String str2, int i3, int i4, Map<String, String> map, SdkCallBack sdkCallBack) {
        try {
            String logoNameByNum = getLogoNameByNum(context, str, i, i2, str2, map, null);
            if (logoNameByNum != null) {
                Drawable findBitmapLogoByLogoName = findBitmapLogoByLogoName(context, logoNameByNum, i3, i4, map, null);
                if (findBitmapLogoByLogoName != null) {
                    XyUtil.doXycallBackResult(sdkCallBack, "0", findBitmapLogoByLogoName);
                    return findBitmapLogoByLogoName;
                }
                XyUtil.doXycallBackResult(sdkCallBack, ThemeUtil.SET_NULL_STR, "logo drawable is null");
                return null;
            }
            XyUtil.doXycallBackResult(sdkCallBack, ThemeUtil.SET_NULL_STR, "logoName is null");
            return null;
        } catch (Exception e) {
            XyUtil.doXycallBackResult(sdkCallBack, ThemeUtil.SET_NULL_STR, e.getLocalizedMessage());
            return null;
        }
    }
}
