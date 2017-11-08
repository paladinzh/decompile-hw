package cn.com.xy.sms.sdk.action;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.provider.ContactsContract.Contacts;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.I;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.a;
import cn.com.xy.sms.sdk.util.m;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;
import cn.com.xy.sms.util.w;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.common.Scopes;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public abstract class AbsSdkDoAction {
    public static final int CONFIG_BEFORE_HAND_PARSE_SIZE = 4;
    public static final int CONFIG_NEARBY_ACTIVITY_CLASS = 5;
    public static final int CONFIG_NOTIFY_ALL_ALGORITHM = 6;
    public static final int CONFIG_UI_CONFIG_CLASS = 3;
    public static final int CONFIG_VIEW_MANAGER_CLASS = 1;
    public static final int CONFIG_WEB_ACTIVITY_CLASS = 2;
    public static final int DO_SEND_MAP_QUERY_URL = 4102;
    public static final int EXTEND_CONFIG_PARSE_MSG = 1;
    public static final int OPEN_TYPE_DOWNLOAD_BY_MARKET = 2;
    public static final int OPEN_TYPE_DOWNLOAD_BY_URL = 1;
    public static final int OPEN_TYPE_HIDDEN_BUTTON_IF_NOT_INSTALLED = 0;
    public static final int SDK_DOACTION_ERROR = -1;
    public static final int SDK_EVENT_INIT_COMPLETE = 0;
    public static final int SDK_EVENT_LOAD_COMPLETE = 11;
    public static final int SDK_EVENT_REMOVE_PUBINFO_CACHE = 10;
    private static final String a = "send_sms";
    private static final String b = "reply_sms";
    private static final String c = "add_addrbook";
    private static final String d = "copy_code";
    private static final String e = "call_phone";
    private static final String f = "call";
    private static final String g = "open_url";
    private static final String h = "add_bookmark";
    private static final String i = "sdk_time_remind";
    private static final String j = "open_calendar";
    private static final String k = "add_alarm";
    private static final String l = "open_map_browser";
    private static final String m = "send_email";

    private static int a(String str) {
        int i = -1;
        if (str != null) {
            try {
                i = Integer.valueOf(str).intValue();
            } catch (Throwable th) {
            }
        }
        return i;
    }

    private int a(Map<String, String> map) {
        return map == null ? -1 : a((String) map.get("simIndex"));
    }

    private void a(Context context, String str) {
        try {
            if (!StringUtils.isNull(str)) {
                str = str.trim();
                if (!str.startsWith("http")) {
                    str = "http://" + str;
                }
            }
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(str));
            setNewTaskIfNeed(context, intent);
            context.startActivity(intent);
        } catch (Throwable th) {
        }
    }

    private void a(Context context, JSONObject jSONObject) {
        zfbRecharge(context, jSONObject, null);
    }

    private void a(Context context, JSONObject jSONObject, String str, Map<String, String> map) {
        JSONObject jSONObject2 = jSONObject != null ? jSONObject : new JSONObject();
        jSONObject2.put("extend", new JSONObject(map));
        jSONObject2.put(NumberInfo.TYPE_KEY, str);
        if (map.containsKey("exphone")) {
            jSONObject2.put("exphone", map.get("exphone"));
        }
        if (map.containsKey("simIndex")) {
            jSONObject2.put("simIndex", map.get("simIndex"));
        }
        a(context, jSONObject2, str, null, map);
    }

    private boolean a(Context context, JSONObject jSONObject, String str, String str2, Map<String, String> map) {
        boolean callWebActivity = callWebActivity(context, jSONObject, str, str2, map);
        if (callWebActivity) {
            return callWebActivity;
        }
        Intent b = b(context, jSONObject, str, map);
        if (b == null) {
            return callWebActivity;
        }
        startWebActivity(context, b);
        if (!StringUtils.isNull(str2)) {
            ParseManager.clearHistorySmsByNum(context, str2, null);
        }
        return true;
    }

    private static boolean a(String str, String str2) {
        int isServiceChoose = DexUtil.isServiceChoose(str, str2);
        if (isServiceChoose == 0) {
            return false;
        }
        if (isServiceChoose < 0) {
            if ("VMhlWdEwVNEW_LENOVO".equals(str) || "1i1BDH2wONE+".equals(str)) {
                return false;
            }
            if ("gOLrCBhQMEIZU2".equals(str)) {
                if ("WEB_TRAFFIC_ORDER".equalsIgnoreCase(str2) || "RECHARGE".equalsIgnoreCase(str2) || "zfb_repayment".equalsIgnoreCase(str2)) {
                    return false;
                }
            } else if ("D6mKXM8MEIZU".equals(str)) {
                if ("zfb_repayment".equalsIgnoreCase(str2)) {
                    return false;
                }
            } else if (!(!"3GdfMSKwHUAWEI".equals(str) || "repayment".equalsIgnoreCase(str2) || "zfb_repayment".equalsIgnoreCase(str2))) {
                return false;
            }
        }
        return true;
    }

    private static Intent b(Context context, JSONObject jSONObject, String str, Map<String, String> map) {
        String str2 = null;
        try {
            Intent intent = new Intent();
            try {
                String str3;
                intent.putExtra("actionType", str);
                if (jSONObject == null) {
                    jSONObject = new JSONObject();
                }
                if (map == null) {
                    str3 = null;
                } else {
                    str2 = (String) map.get("otherPackageName");
                    str3 = (String) map.get("actionName");
                    if (map.containsKey("bubbleJson")) {
                        jSONObject.put("bubbleJson", map.get("bubbleJson"));
                    }
                    if (map.containsKey("checkString")) {
                        jSONObject.put("checkString", map.get("checkString"));
                    }
                }
                intent.putExtra("JSONDATA", jSONObject.toString());
                if (!StringUtils.isNull(KeyManager.channel)) {
                    intent.putExtra("channel", KeyManager.channel);
                }
                if (!StringUtils.isNull(NetUtil.APPVERSION)) {
                    intent.putExtra("appVersion", NetUtil.APPVERSION);
                }
                String config = DuoquUtils.getSdkDoAction().getConfig(2, null);
                if (StringUtils.isNull(str2)) {
                    intent.setClassName(context, config);
                } else {
                    intent.setClassName(str2, config);
                    intent.setFlags(268435456);
                }
                if (!StringUtils.isNull(str3)) {
                    intent.setAction(str3);
                }
                if (context instanceof Activity) {
                    return intent;
                }
                intent.setFlags(268435456);
                return intent;
            } catch (Throwable th) {
                str2 = intent;
                return str2;
            }
        } catch (Throwable th2) {
            return str2;
        }
    }

    private static Map<String, Integer> b(String str, String str2) {
        Map<String, Integer> hashMap = new HashMap();
        try {
            int parseInt = Integer.parseInt(str.split("年")[0]);
            int parseInt2 = Integer.parseInt(str.split("月")[0].split("年")[1]);
            int parseInt3 = Integer.parseInt(str.split("月")[1].split("日")[0]);
            int parseInt4 = Integer.parseInt(str2.split(":")[0]);
            int parseInt5 = Integer.parseInt(str2.split(":")[1]);
            hashMap.put("company_meetingreminder_date_year", Integer.valueOf(parseInt));
            hashMap.put("company_meetingreminder_date_month", Integer.valueOf(parseInt2));
            hashMap.put("company_meetingreminder_date_day", Integer.valueOf(parseInt3));
            hashMap.put("company_meetingreminder_time_hour", Integer.valueOf(parseInt4));
            hashMap.put("company_meetingreminder_time_minute", Integer.valueOf(parseInt5));
        } catch (Throwable th) {
        }
        return hashMap;
    }

    private static JSONObject b(String str) {
        if (str == null) {
            return null;
        }
        try {
            return new JSONObject(StringUtils.decode(str));
        } catch (Throwable th) {
            return null;
        }
    }

    private static String c(String str) {
        if (str != null) {
            for (String str2 : str.split(Constant.Delimiter)) {
                if (!"null".equalsIgnoreCase(str2)) {
                    return str2;
                }
            }
        }
        return "";
    }

    public static String getCurProcessName(int i, Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            if (activityManager == null) {
                return "";
            }
            List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            if (runningAppProcesses == null) {
                return "";
            }
            for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                if (runningAppProcessInfo.pid == i) {
                    return runningAppProcessInfo.processName;
                }
            }
            return "";
        } catch (Throwable th) {
        }
    }

    public static boolean openStartWebActivity(Context context, JSONObject jSONObject, String str, String str2, Map<String, String> map) {
        Intent b = b(context, jSONObject, str, map);
        if (b == null) {
            return false;
        }
        context.startActivity(b);
        if (!StringUtils.isNull(str2)) {
            ParseManager.clearHistorySmsByNum(context, str2, null);
        }
        return true;
    }

    public static void startDisplaySceneActivity(Context context, JSONObject jSONObject, String str) {
        try {
            Intent intent = new Intent();
            intent.putExtra("actionType", str);
            if (jSONObject != null) {
                intent.putExtra("JSONDATA", jSONObject.toString());
            }
            intent.setClassName(context, "cn.com.xy.sms.sdk.ui.popu.web.SdkDisplaySceneActivity");
            intent.setFlags(268435456);
            context.startActivity(intent);
        } catch (Throwable th) {
        }
    }

    public void addAlarm(Context context, int i, int i2, Map map) {
        Intent intent = new Intent("android.intent.action.SET_ALARM");
        intent.putExtra("android.intent.extra.alarm.HOUR", i);
        intent.putExtra("android.intent.extra.alarm.MINUTES", i2);
        intent.putExtra("android.intent.extra.alarm.SKIP_UI", false);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public void addBookMark(Context context, String str, Map map) {
    }

    public void addContact(Context context, String str, String str2, String str3, String str4, Map map) {
        Intent intent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
        if (!TextUtils.isEmpty(str)) {
            intent.putExtra("name", str);
        }
        ArrayList arrayList = new ArrayList();
        if (!TextUtils.isEmpty(str2)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("mimetype", "vnd.android.cursor.item/phone_v2");
            contentValues.put("data2", Integer.valueOf(2));
            contentValues.put("data1", str2);
            arrayList.add(contentValues);
        }
        if (!TextUtils.isEmpty(str3)) {
            contentValues = new ContentValues();
            contentValues.put("mimetype", "vnd.android.cursor.item/email_v2");
            contentValues.put("data2", Integer.valueOf(2));
            contentValues.put("data1", str3);
            arrayList.add(contentValues);
        }
        if (!TextUtils.isEmpty(str4)) {
            contentValues = new ContentValues();
            contentValues.put("mimetype", "vnd.android.cursor.item/website");
            contentValues.put("data2", Integer.valueOf(5));
            contentValues.put("data1", str4);
            arrayList.add(contentValues);
        }
        intent.putParcelableArrayListExtra(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, arrayList);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public void beforeInitBubbleView(Activity activity, HashSet<String> hashSet) {
    }

    public void callPhone(Context context, String str, int i) {
        callPhone(context, str, i, null);
    }

    public void callPhone(Context context, String str, int i, Map<String, String> map) {
        String replace = str.replace("-", "");
        Intent intent = new Intent();
        intent.setAction("android.intent.action.CALL");
        intent.setData(Uri.parse("tel:" + replace));
        try {
            setNewTaskIfNeed(context, intent);
            context.startActivity(intent);
        } catch (Throwable th) {
        }
    }

    public boolean callWebActivity(Context context, JSONObject jSONObject, String str, String str2, Map<String, String> map) {
        return false;
    }

    public boolean checkHasAppName(Context context, String str) {
        try {
            context.getPackageManager().getPackageInfo(str, 1);
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public int checkValidUrl(String str, String str2, String str3, String str4, Map map) {
        return MsgUrlService.RESULT_NOT_IMPL;
    }

    public void clearPopup() {
    }

    public void closePopupWindow() {
    }

    public void copyCode(Context context, String str, Map<String, String> map) {
        ((ClipboardManager) context.getSystemService("clipboard")).setText(str);
        Toast.makeText(context, "已复制到剪贴板", 1).show();
    }

    public long createCard(JSONObject jSONObject, int i, Map<String, String> map) {
        return 0;
    }

    public abstract void deleteMsgForDatabase(Context context, String str);

    public void deleteMsgForDatabase(Context context, String str, String str2, Map<String, String> map) {
    }

    public void doAction(Context context, String str, Map<String, String> map) {
        String a = w.a();
        JSONObject jSONObject;
        try {
            if (StringUtils.isNull(str)) {
                throw new Exception("doAction  actionData is null");
            }
            jSONObject = new JSONObject(str);
            try {
                if (jSONObject.get("uiType").equals("MENU")) {
                    ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.action.AbsSdkDoAction", "doAction", context, str, map, jSONObject);
                    str = jSONObject.optString("menu_item_action_data");
                    i.a(new k(11, "phoneNum", jSONObject.optString("phoneNum"), "companyNum", jSONObject.optString("companyNum"), "functionMode", jSONObject.optString("menuCode")));
                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.action.AbsSdkDoAction", "doAction", context, str, map, jSONObject);
                }
            } catch (Throwable th) {
            }
            try {
                JSONObject b = b(str);
                if (b != null) {
                    if (jSONObject != null) {
                        b.put("conversation_num", jSONObject.optString("phoneNum"));
                        b.put("conversation_pub", jSONObject.optString("companyNum"));
                        b.put("conversation_menu", jSONObject.optString("menuCode"));
                    }
                    exectueAction(context, b, map);
                    return;
                }
                throw new Exception("please valid  actionData ");
            } catch (Throwable th2) {
            }
        } catch (Throwable th3) {
            jSONObject = null;
        }
    }

    public void doConversationAction(Context context, String str, JSONObject jSONObject, Map map) {
        String str2 = "";
        if (map != null && map.containsKey("phone")) {
            str2 = (String) map.get("phone");
        }
        if (jSONObject != null) {
            Object optString = jSONObject.optString("extendVal");
            if (!TextUtils.isEmpty(optString)) {
                try {
                    JSONObject jSONObject2 = new JSONObject(optString);
                    String optString2 = jSONObject2.optString(NumberInfo.TYPE_KEY);
                    String optString3 = jSONObject2.optString("phone");
                    String optString4 = jSONObject2.optString("titleNo");
                    if (!StringUtils.isNull(optString2)) {
                        jSONObject.put(NumberInfo.TYPE_KEY, optString2);
                        jSONObject.put("phone", optString3);
                        jSONObject.put("titleNo", optString4);
                    }
                } catch (Throwable th) {
                }
            }
        }
        a(context, jSONObject, str, str2, map);
    }

    public void doExAction(Context context, String str, JSONObject jSONObject, Map map) {
    }

    public void doRemind(Activity activity, Map<String, Object> map) {
    }

    public void doRemind(Context context, String str, String str2, String str3, String str4, String str5, String str6, String str7, Map<String, String> map) {
        try {
            Intent intent = new Intent();
            if (!(context instanceof Activity)) {
                intent.addFlags(268435456);
            }
            intent.setType("vnd.android.cursor.item/event");
            intent.setAction("android.intent.action.EDIT");
            if (!StringUtils.isNull(str2)) {
                intent.putExtra("title", str2);
            }
            if (!StringUtils.isNull(str3)) {
                intent.putExtra("eventLocation", str3);
            }
            if (!StringUtils.isNull(str4)) {
                intent.putExtra("description", str4);
            }
            long longByString = StringUtils.getLongByString(str5);
            if (longByString != -1) {
                intent.putExtra("beginTime", longByString);
            }
            longByString = StringUtils.getLongByString(str6);
            if (longByString != -1) {
                intent.putExtra("endTime", longByString);
            }
            intent.putExtra("accessLevel", 0);
            if (map != null && map.containsKey("founder_packagename")) {
                intent.putExtra("founder_packagename", (String) map.get("founder_packagename"));
            }
            context.startActivity(intent);
        } catch (Throwable th) {
        }
    }

    public void doRemind(Context context, String str, Map<String, Object> map) {
        if (context != null && map != null && map.size() != 0) {
            String obj = !map.containsKey("title_name") ? null : map.get("title_name").toString();
            String obj2 = !map.containsKey("company_meetingreminder_add") ? null : map.get("company_meetingreminder_add").toString();
            String str2 = !map.containsKey("company_meetingreminder_Convener") ? null : "召集人:" + map.get("company_meetingreminder_Convener") + "  ";
            if (map.containsKey("company_meetingreminder_theme")) {
                str2 = new StringBuilder(String.valueOf(str2)).append("主题:").append(map.get("company_meetingreminder_theme").toString()).toString();
            }
            if (map.containsKey("company_meetingreminder_date")) {
                str2 = new StringBuilder(String.valueOf(str2)).append("  时间:").append(map.get("company_meetingreminder_date")).toString();
            }
            if (map.containsKey("company_meetingreminder_time")) {
                str2 = new StringBuilder(String.valueOf(str2)).append(" ").append(map.get("company_meetingreminder_time")).toString();
            }
            String str3 = str2;
            Map b = b(map.get("company_meetingreminder_date").toString(), map.get("company_meetingreminder_time").toString());
            int intValue = !b.containsKey("company_meetingreminder_date_year") ? -1 : ((Integer) b.get("company_meetingreminder_date_year")).intValue();
            int intValue2 = !b.containsKey("company_meetingreminder_date_month") ? -1 : ((Integer) b.get("company_meetingreminder_date_month")).intValue();
            int intValue3 = !b.containsKey("company_meetingreminder_date_day") ? -1 : ((Integer) b.get("company_meetingreminder_date_day")).intValue();
            int intValue4 = !b.containsKey("company_meetingreminder_time_hour") ? -1 : ((Integer) b.get("company_meetingreminder_time_hour")).intValue();
            int intValue5 = !b.containsKey("company_meetingreminder_time_minute") ? -1 : ((Integer) b.get("company_meetingreminder_time_minute")).intValue();
            int intValue6 = !b.containsKey("company_meetingreminder_minutes_early") ? 120 : ((Integer) b.get("company_meetingreminder_minutes_early")).intValue();
            Intent intent = new Intent();
            intent.setType("vnd.android.cursor.item/event");
            intent.setAction("android.intent.action.EDIT");
            intent.putExtra("title", obj);
            intent.putExtra("eventLocation", obj2);
            intent.putExtra("description", str3);
            if (!(intValue == -1 || intValue2 == -1 || intValue3 == -1 || intValue4 == -1 || intValue5 == -1 || intValue6 == -1)) {
                Calendar instance = Calendar.getInstance();
                instance.set(intValue, intValue2 - 1, intValue3, intValue4, intValue5);
                instance.add(12, -intValue6);
                intent.putExtra("beginTime", instance.getTimeInMillis());
                instance.set(intValue, intValue2 - 1, intValue3, intValue4, intValue5);
                instance.add(12, -10);
                intent.putExtra("endTime", instance.getTimeInMillis());
            }
            intent.putExtra("accessLevel", 0);
            intent.putExtra("availability", 2);
            setNewTaskIfNeed(context, intent);
            context.startActivity(intent);
        }
    }

    public void downLoadApp(Context context, String str, String str2, Map<String, String> map) {
        JSONObject jSONObject = null;
        Object obj = null;
        try {
            if (!StringUtils.isNull(str)) {
                if (checkHasAppName(context, str)) {
                    openAppByAppName(context, str);
                } else {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + str));
                    intent.addFlags(268435456);
                    context.startActivity(intent);
                }
                obj = 1;
            }
        } catch (Throwable th) {
        }
        if (obj == null) {
            if (map != null && map.containsKey("menuName")) {
                jSONObject = new JSONObject();
                try {
                    jSONObject.put("menuName", map.get("menuName"));
                } catch (JSONException e) {
                }
            }
            openUrl(context, str2, jSONObject);
        }
    }

    public void downLoadUrl(Context context, String str) {
        a(context, str);
    }

    public void downLoadUrl(Context context, String str, JSONObject jSONObject) {
        downLoadUrl(context, str);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void exectueAction(Context context, JSONObject jSONObject, Map map) {
        String a = w.a();
        String str;
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.action.AbsSdkDoAction", "exectueAction", context, jSONObject, map);
            String optString = jSONObject.optString(NumberInfo.TYPE_KEY);
            if (StringUtils.isNull(optString)) {
                throw new Exception("actionType is  null");
            }
            if (optString.toLowerCase().startsWith("conversation_")) {
                doConversationAction(context, optString, jSONObject, map);
            } else if (optString.toLowerCase().startsWith("ex_")) {
                doExAction(context, optString, jSONObject, map);
            } else if (c.equalsIgnoreCase(optString)) {
                optString = null;
                r4 = null;
                String str2 = null;
                str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "field");
                if (str.equalsIgnoreCase("phone")) {
                    optString = (String) JsonUtil.getValueFromJsonObject(jSONObject, "content");
                }
                if (str.equalsIgnoreCase(Scopes.EMAIL)) {
                    r4 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "content");
                }
                if (str.equalsIgnoreCase(Constant.URLS)) {
                    str2 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "content");
                }
                addContact(context, null, optString, r4, str2, map);
            } else if (b.equalsIgnoreCase(optString)) {
                optString = (String) JsonUtil.getValueFromJsonObject(jSONObject, "send_code");
                str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "phone");
                if (StringUtils.isNull(str) || "null".equalsIgnoreCase(str)) {
                    if (map != null) {
                        str = (String) map.get("phoneNum");
                    }
                }
                sendSms(context, str, optString, a(map), map);
            } else if (a.equalsIgnoreCase(optString)) {
                sendSms(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, "phone"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "send_code"), a(map), map);
            } else if (k.equalsIgnoreCase(optString)) {
                str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "time");
                if (!TextUtils.isEmpty(str)) {
                    r1 = "";
                    r0 = DexUtil.convertDate(str);
                    String[] split = (r0 == null ? r1 : new SimpleDateFormat("HH:mm").format(r0)).split(":");
                    if (split != null && split.length == 2) {
                        addAlarm(context, Integer.parseInt(split[0]), Integer.parseInt(split[1]), map);
                    }
                }
            } else if (j.equalsIgnoreCase(optString)) {
                str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "date");
                long currentTimeMillis = System.currentTimeMillis();
                r0 = DexUtil.convertDate(str);
                viewCalendar(context, r0 == null ? currentTimeMillis : r0.getTime(), map);
            } else if (h.equalsIgnoreCase(optString)) {
                addBookMark(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, Constant.URLS), map);
            } else if (g.equalsIgnoreCase(optString)) {
                openUrl(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, Constant.URLS), jSONObject);
            } else if (d.equalsIgnoreCase(optString)) {
                copyCode(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, "code"), map);
            } else if (f.equalsIgnoreCase(optString) || e.equalsIgnoreCase(optString)) {
                str = null;
                if (jSONObject.has("phone")) {
                    str = jSONObject.getString("phone");
                } else if (jSONObject.has("phoneNum")) {
                    str = jSONObject.getString("phoneNum");
                }
                callPhone(context, c(str), a(map));
            } else if (m.equalsIgnoreCase(optString)) {
                sendEmailTo(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, Scopes.EMAIL), map);
            } else if (l.equalsIgnoreCase(optString)) {
                openMapOnBrowser(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, "address"));
            } else if ("access_url".equalsIgnoreCase(optString) || g.equalsIgnoreCase(optString)) {
                openUrl(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, Constant.URLS), jSONObject);
            } else if ("down_url".equalsIgnoreCase(optString)) {
                downLoadUrl(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, Constant.URLS));
            } else if ("download".equalsIgnoreCase(optString)) {
                str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "appName");
                if (StringUtils.isNull(str)) {
                    str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "extend");
                }
                r2 = str;
                str = (String) JsonUtil.getValueFromJsonObject(jSONObject, Constant.URLS);
                if (map == null) {
                    Object hashMap = new HashMap();
                }
                map.put("menuName", (String) JsonUtil.getValueFromJsonObject(jSONObject, "menuName"));
                downLoadApp(context, r2, str, map);
            } else if ("weibo_url".equalsIgnoreCase(optString)) {
                openWeiBoUrl(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, Constant.URLS));
            } else if ("map_site".equalsIgnoreCase(optString) || "open_map".equalsIgnoreCase(optString)) {
                openMap(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, "address"));
            } else if ("recharge".equalsIgnoreCase(optString)) {
                if (a(KeyManager.channel, optString)) {
                    a(context, jSONObject, "WEB_RECHARGE_CHOOSE", map);
                } else if (recharge(context, jSONObject, map) == -1) {
                    a(context, jSONObject, "WEB_RECHARGE_CHOOSE", map);
                }
            } else if ("repayment".equalsIgnoreCase(optString)) {
                if (a(KeyManager.channel, optString)) {
                    a(context, jSONObject, "WEB_REPAYMENT_CHOOSE", map);
                } else if (checkHasAppName(context, jSONObject.getString("appName"))) {
                    repayment(context, jSONObject);
                } else {
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_REPAYMENT");
                    a(context, jSONObject, "WEB_REPAYMENT", null, map);
                }
            } else if ("zfb_recharge".equalsIgnoreCase(optString)) {
                if (a(KeyManager.channel, optString)) {
                    a(context, jSONObject, "WEB_RECHARGE_CHOOSE", map);
                } else {
                    zfbRecharge(context, jSONObject, map);
                }
            } else if ("zfb_repayment".equalsIgnoreCase(optString)) {
                if (a(KeyManager.channel, optString)) {
                    a(context, jSONObject, "WEB_REPAYMENT_CHOOSE", map);
                } else {
                    zfbRepayment(context, jSONObject, map);
                }
            } else if ("open_app".equalsIgnoreCase(optString)) {
                str = jSONObject.getString("appName");
                r1 = jSONObject.optString("extendVal");
                if (!StringUtils.isNull(r1)) {
                    r1 = r1.trim();
                    if (r1.startsWith("{")) {
                        openAppView(context, jSONObject, r1);
                    } else {
                        if (StringUtils.isNull(str)) {
                            str = r1;
                        }
                        if (!StringUtils.isNull(str)) {
                            openAppByAppName(context, str, jSONObject.optString("appDownUrl"));
                        }
                    }
                } else if (!StringUtils.isNull(str)) {
                    openAppByAppName(context, str, jSONObject.optString("appDownUrl"));
                }
            } else if ("open_app_view".equalsIgnoreCase(optString)) {
                str = jSONObject.optString("extend");
                if (StringUtils.isNull(str)) {
                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.action.AbsSdkDoAction", "exectueAction", context, jSONObject, map);
                    if (map != null) {
                        try {
                            if ("0".equals((String) map.get("viewType"))) {
                                str = "";
                                if (!map.isEmpty()) {
                                    if (StringUtils.isNull(str)) {
                                        str = (String) map.get("phone");
                                    }
                                    if (StringUtils.isNull(str)) {
                                        map.get("phoneNum");
                                    }
                                }
                                finishActivity(context, map);
                                return;
                            }
                        } catch (Throwable th) {
                        }
                    }
                    return;
                }
                JSONObject jSONObject2 = new JSONObject(str);
                if (!m.a(context, jSONObject2.optString("appName"), jSONObject2.optString("viewUrl"), jSONObject)) {
                    downLoadUrl(context, jSONObject.optString("appDownUrl"));
                }
            } else if ("time_remind".equalsIgnoreCase(optString)) {
                doRemind(context, (String) map.get("msgId"), ParseManager.parseMsgToMap(Constant.getContext(), (String) map.get("phoneNum"), "", (String) map.get("content"), null));
            } else if (i.equalsIgnoreCase(optString)) {
                r2 = "";
                if (!(map == null || map.isEmpty())) {
                    r2 = (String) map.get("msgId");
                }
                doRemind(context, r2, (String) JsonUtil.getValueFromJsonObject(jSONObject, "title"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "eventLocation"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "description"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "startTime"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "endTime"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "remind"), map);
            } else if ("del_msg".equalsIgnoreCase(optString)) {
                deleteMsgForDatabase(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, "msgId"));
                finishActivity(context, map);
            } else if ("open_map_list".equalsIgnoreCase(optString)) {
                r2 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "address");
                optString = (String) map.get("latitude");
                r4 = (String) map.get("longitude");
                if ("3GdfMSKwHUAWEI".equals(KeyManager.channel)) {
                    map.put("menuName", (String) JsonUtil.getValueFromJsonObject(jSONObject, "menuName"));
                    nearSite(context, r2, optString, r4, map);
                } else {
                    nearSite(context, r2, optString, r4);
                }
            } else if (l.equalsIgnoreCase(optString)) {
                openMapOnBrowser(context, (String) JsonUtil.getValueFromJsonObject(jSONObject, "address"));
            } else if ("pay_water_gas".equalsIgnoreCase(optString)) {
                payWaterGas(context, jSONObject, map);
            } else if (optString.toUpperCase().startsWith("WEB_")) {
                if (!"WEB_TRAFFIC_ORDER".equalsIgnoreCase(optString)) {
                    r4 = "";
                    if (!(map == null || map.isEmpty())) {
                        if (StringUtils.isNull(r4)) {
                            r4 = (String) map.get("phone");
                        }
                        if (StringUtils.isNull(r4)) {
                            r4 = (String) map.get("phoneNum");
                        }
                    }
                    if (map != null) {
                        str = (String) map.get("simIndex");
                        if (str != null) {
                            try {
                                jSONObject.put("simIndex", str);
                            } catch (JSONException e) {
                            }
                        }
                    }
                    a(context, jSONObject, optString, r4, map);
                } else if (a(KeyManager.channel, optString)) {
                    a(context, jSONObject, "WEB_TRAFFIC_CHOOSE", map);
                } else if (orderTraiffc(context, jSONObject, map) == -1) {
                    a(context, jSONObject, "WEB_TRAFFIC_CHOOSE", map);
                }
            } else if ("display_scene_result".equalsIgnoreCase(optString)) {
                startDisplaySceneActivity(context, jSONObject, optString);
            } else if ("wizard_service".equalsIgnoreCase(optString)) {
                startWizardService(context, jSONObject, map);
            } else {
                m.a(context, optString, jSONObject);
            }
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.action.AbsSdkDoAction", "exectueAction", context, jSONObject, map);
            if (map != null) {
                try {
                    if ("0".equals((String) map.get("viewType"))) {
                        str = "";
                        if (!map.isEmpty()) {
                            if (StringUtils.isNull(str)) {
                                str = (String) map.get("phone");
                            }
                            if (StringUtils.isNull(str)) {
                                map.get("phoneNum");
                            }
                        }
                        finishActivity(context, map);
                    }
                } catch (Throwable th2) {
                }
            }
        } catch (Throwable th3) {
            Throwable th4 = th3;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.action.AbsSdkDoAction", "exectueAction", context, jSONObject, map);
            if (map != null) {
                if ("0".equals((String) map.get("viewType"))) {
                    str = "";
                    if (!map.isEmpty()) {
                        if (StringUtils.isNull(str)) {
                            str = (String) map.get("phone");
                        }
                        if (StringUtils.isNull(str)) {
                            map.get("phoneNum");
                        }
                    }
                    finishActivity(context, map);
                }
            }
        }
    }

    public void finishActivity(Context context) {
        finishActivity(context, null);
    }

    public void finishActivity(Context context, Map<String, String> map) {
        if (context != null && (context instanceof Activity)) {
            ((Activity) context).finish();
        }
    }

    public Drawable getAirDrawableByFlyNo(Context context, String str) {
        return null;
    }

    public String getConfig(int i, Map map) {
        switch (i) {
            case 1:
                return "cn.com.xy.sms.sdk.ui.popu.util.ViewManger";
            case 2:
                return "cn.com.xy.sms.sdk.ui.popu.web.SdkWebActivity";
            case 3:
                return "cn.com.xy.sms.sdk.ui.config.UIConfig";
            case 4:
                return "50";
            case 5:
                return "cn.com.xy.sms.sdk.ui.popu.web.NearbyPointList";
            case 6:
                return "true";
            default:
                return null;
        }
    }

    public abstract String getContactName(Context context, String str);

    public JSONObject getContactObj(Context context, String str) {
        return null;
    }

    public Drawable getDrawableByNumber(Context context, String str, Map<String, Object> map) {
        return null;
    }

    public AssetManager getExtendAssetManager() {
        return null;
    }

    public String getExtendConfig(int i, Map map) {
        if (i == 1) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("ref_basevalue", "true");
                jSONObject.put(Constant.KEY_ALLOW_PERSONAL_MSG, "true");
                return jSONObject.toString();
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public JSONObject getExtendValue(int i, JSONObject jSONObject) {
        switch (i) {
            case 1:
                int i2 = -1;
                if (jSONObject != null) {
                    i2 = a((String) JsonUtil.getValFromJsonObject(jSONObject, "simIndex"));
                }
                JSONObject proviceAndSP = getProviceAndSP(i2);
                JSONObject jSONObject2;
                if (proviceAndSP != null) {
                    try {
                        String proviceCode = IccidInfoManager.getProviceCode((String) JsonUtil.getValueFromJsonObject(proviceAndSP, "provice"));
                        if (proviceCode == null) {
                            return proviceAndSP;
                        }
                        proviceAndSP.put("provice", proviceCode);
                        return proviceAndSP;
                    } catch (Throwable th) {
                        jSONObject2 = proviceAndSP;
                    }
                } else {
                    IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
                    if (queryDeftIccidInfo == null) {
                        return proviceAndSP;
                    }
                    jSONObject2 = new JSONObject();
                    try {
                        jSONObject2.put("provice", queryDeftIccidInfo.areaCode);
                        jSONObject2.put("sp", queryDeftIccidInfo.operator);
                    } catch (Throwable th2) {
                    }
                    return jSONObject2;
                }
            default:
                return null;
        }
    }

    public Drawable getHeadDrawableByNumber(Context context, String str, Map<String, Object> map) {
        return null;
    }

    public int[] getHeadDrawableColorByNumber(Context context, String str, Map<String, Object> map) {
        return null;
    }

    public String getIccidBySimIndex(int i) {
        return null;
    }

    public void getLocation(Context context, Handler handler) {
        LocationManager locationManager = (LocationManager) context.getSystemService(NetUtil.REQ_QUERY_LOCATION);
        if (locationManager.isProviderEnabled(GeocodeSearch.GPS) || locationManager.isProviderEnabled("network")) {
            Location location;
            Criteria criteria = new Criteria();
            criteria.setAccuracy(2);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(3);
            criteria.setSpeedRequired(false);
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (lastKnownLocation != null) {
                location = lastKnownLocation;
            } else {
                location = locationManager.getLastKnownLocation("network");
                if (location == null) {
                    handler.obtainMessage(4100).sendToTarget();
                    return;
                }
            }
            Message obtainMessage = handler.obtainMessage(4102);
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", location.getLatitude());
            bundle.putDouble("longitude", location.getLongitude());
            obtainMessage.setData(bundle);
            obtainMessage.sendToTarget();
            return;
        }
        handler.obtainMessage(4100).sendToTarget();
    }

    public String getPhoneNumberBySimIndex(int i) {
        return null;
    }

    public JSONObject getProviceAndSP(int i) {
        String iccidBySimIndex = getIccidBySimIndex(i);
        if (iccidBySimIndex == null) {
            return null;
        }
        JSONObject jSONObject;
        try {
            IccidInfo queryIccidInfo = IccidInfoManager.queryIccidInfo(iccidBySimIndex, Constant.getContext());
            if (queryIccidInfo == null) {
                jSONObject = null;
            } else {
                jSONObject = new JSONObject();
                try {
                    jSONObject.put("provice", queryIccidInfo.areaCode);
                    jSONObject.put("sp", queryIccidInfo.operator);
                } catch (Throwable th) {
                }
            }
        } catch (Throwable th2) {
            jSONObject = null;
        }
        return jSONObject;
    }

    public List<JSONObject> getReceiveMsgByReceiveTime(String str, long j, long j2, int i) {
        return null;
    }

    public JSONObject getTelephonyInfoBySimIndex(int i) {
        return null;
    }

    public View getThirdPopupView(Context context, String str, Map<String, Object> map, SdkCallBack sdkCallBack) {
        return null;
    }

    public JSONArray getTimeSubInfo(String str, long j) {
        return null;
    }

    public int getWifiType(Context context) {
        return 0;
    }

    public String getresources(String str, Map<String, String> map) {
        return null;
    }

    public boolean isContact(Context context, String str) {
        return false;
    }

    public boolean isDoubleSimPhone() {
        return true;
    }

    public Set<String> loadPublicNumbers(Context context) {
        Cursor query;
        Throwable th;
        Cursor cursor = null;
        Set hashSet = new HashSet();
        try {
            query = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), new String[]{"address"}, null, null, null);
            if (query != null) {
                try {
                    int columnIndex = query.getColumnIndex("address");
                    while (query.moveToNext()) {
                        String string = query.getString(columnIndex);
                        if (!StringUtils.isNull(string)) {
                            string = StringUtils.getPhoneNumberNo86(StringUtils.replaceBlank(string));
                            if (StringUtils.isNumber(string) && !StringUtils.isPhoneNumber(string)) {
                                hashSet.add(string);
                            }
                        }
                    }
                } catch (Throwable th2) {
                    cursor = query;
                    th = th2;
                    try {
                        cursor.close();
                    } catch (Throwable th3) {
                    }
                    throw th;
                }
            }
            try {
                query.close();
            } catch (Throwable th4) {
            }
        } catch (Throwable th5) {
            th = th5;
            cursor.close();
            throw th;
        }
        return hashSet;
    }

    public void logError(String str, String str2, Throwable th) {
        if (str == null) {
            str = "XIAOYUAN";
        }
        if (str2 != null) {
            try {
                Log.e(str, str2);
            } catch (Throwable th2) {
            }
        }
    }

    public void logInfo(String str, String str2, Map<String, Object> map) {
        if (str == null) {
            str = "XIAOYUAN";
        }
        if (str2 != null) {
            try {
                Log.i(str, str2);
            } catch (Throwable th) {
            }
        }
    }

    public abstract void markAsReadForDatabase(Context context, String str);

    public void nearList(Context context, String str) {
        a(context, str);
    }

    public void nearSite(Context context, String str, String str2, String str3) {
        nearSite(context, str, str2, str3, null);
    }

    public void nearSite(Context context, String str, String str2, String str3, Map<String, String> map) {
        Intent intent = new Intent();
        String config = DuoquUtils.getSdkDoAction().getConfig(5, null);
        if (StringUtils.isNull(config)) {
            config = "cn.com.xy.sms.sdk.ui.popu.web.NearbyPointList";
        }
        intent.setClassName(context, config);
        intent.putExtra("address", str);
        intent.putExtra("locationLatitude", str2);
        intent.putExtra("locationLongitude", str3);
        if (map != null && map.containsKey("menuName")) {
            intent.putExtra("menuName", (String) map.get("menuName"));
        }
        setNewTaskIfNeed(context, intent);
        context.startActivity(intent);
    }

    public Boolean needRecognisedValue() {
        return Boolean.valueOf(true);
    }

    public void onEventCallback(int i, Map<String, Object> map) {
    }

    public void openAppByAppName(Context context, String str) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(str);
        setNewTaskIfNeed(context, launchIntentForPackage);
        context.startActivity(launchIntentForPackage);
    }

    public void openAppByAppName(Context context, String str, String str2) {
        if (checkHasAppName(context, str)) {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = new Intent();
            Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(str);
            setNewTaskIfNeed(context, launchIntentForPackage);
            context.startActivity(launchIntentForPackage);
        } else if (StringUtils.isNull(str2)) {
            Toast.makeText(context, "please check is installed  " + str, 1).show();
        } else {
            downLoadUrl(context, str2);
        }
    }

    public void openAppByUrl(Context context, String str) {
    }

    public void openAppByUrl(Context context, String str, String str2) {
    }

    public void openAppView(Context context, JSONObject jSONObject, String str) {
        try {
            JSONObject jSONObject2 = new JSONObject(str);
            if (!m.a(context, jSONObject.optString("appName"), jSONObject2.optString("viewUrl"), jSONObject2)) {
                downLoadUrl(context, jSONObject.optString("appDownUrl"), jSONObject);
            }
        } catch (Throwable th) {
        }
    }

    public void openMap(Context context, String str) {
        openMap(context, null, str, 0.0d, 0.0d);
    }

    public void openMap(Context context, String str, String str2, double d, double d2) {
        try {
            if (StringUtils.isNull(str)) {
                str = "";
            } else {
                str = URLEncoder.encode(str, "utf-8");
            }
            if (!StringUtils.isNull(str2)) {
                str2 = URLEncoder.encode(str2.replace(" ", ",").replace("（", ",").replace("(", ",").replace("，", ",").replace("）", "").replace(")", "").replace("?", "").replace("&", "").replace("#", "").trim(), "utf-8");
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setData(Uri.parse("geo:" + d2 + "," + d + "?q=" + str2));
                intent.addFlags(268435456);
                context.startActivity(intent);
            }
        } catch (Throwable th) {
        }
    }

    public void openMapOnBrowser(Context context, String str) {
        openMapOnBrowser(context, null, str, 0.0d, 0.0d);
    }

    public void openMapOnBrowser(Context context, String str, String str2, double d, double d2) {
        try {
            Intent intent;
            if (StringUtils.isNull(str2)) {
                if (!(d == 0.0d || d2 == 0.0d)) {
                    intent = new Intent("android.intent.action.VIEW", Uri.parse("http://api.map.baidu.com/geocoder?location=" + d2 + "," + d + "&coord_type=gcj02&output=html&src=xiaoyuan|多趣"));
                    intent.addFlags(268435456);
                    context.startActivity(intent);
                    return;
                }
                return;
            }
            Object obj = (d == 0.0d || d2 == 0.0d) ? "http://api.map.baidu.com/geocoder?address=" + str2 : "http://api.map.baidu.com/marker?location=" + d2 + "," + d + "&title=" + str + "&content=" + str2;
            intent = new Intent("android.intent.action.VIEW", Uri.parse(new StringBuilder(String.valueOf(obj)).append("&output=html&src=xiaoyuan|多趣").toString()));
            intent.addFlags(268435456);
            context.startActivity(intent);
        } catch (Throwable th) {
        }
    }

    public abstract void openSms(Context context, String str, Map<String, String> map);

    public void openSmsDetail(Context context, String str, Map map) {
    }

    public void openUrl(Context context, String str, JSONObject jSONObject) {
        openUrl(context, str, jSONObject, null);
    }

    public void openUrl(Context context, String str, JSONObject jSONObject, Map<String, String> map) {
        JSONObject jSONObject2 = jSONObject != null ? jSONObject : new JSONObject();
        try {
            if (!StringUtils.isNull(str)) {
                str = str.replaceAll("&amp;", "&").trim();
                if (!str.startsWith("http")) {
                    str = "http://" + str;
                }
            }
            jSONObject2.put(Constant.URLS, str);
            boolean a = a(context, jSONObject2, "WEB_URL", "", map);
        } catch (JSONException e) {
            a = false;
        }
        if (!a) {
            a(context, str);
        }
    }

    public void openWeiBoUrl(Context context, String str) {
        openUrl(context, str, null);
    }

    public int orderTraiffc(Context context, JSONObject jSONObject, Map<String, String> map) {
        try {
            a(context, jSONObject, "WEB_TRAFFIC_CHOOSE", map);
        } catch (JSONException e) {
        }
        return 0;
    }

    public int otherOrderTraffic(Context context, JSONObject jSONObject, Map<String, String> map) {
        return 0;
    }

    public int otherRecharge(Context context, JSONObject jSONObject, Map<String, String> map) {
        return 0;
    }

    public int otherRepayment(Context context, JSONObject jSONObject, Map<String, String> map) {
        return 0;
    }

    public int otherService(Context context, JSONObject jSONObject, Map<String, String> map) {
        return 0;
    }

    public void parseMsgCallBack(int i, Map<String, Object> map) {
    }

    public void parseVersionChange(Context context, int i) {
    }

    public int payWaterGas(Context context, JSONObject jSONObject, Map<String, String> map) {
        return 0;
    }

    public int recharge(Context context, JSONObject jSONObject, Map<String, String> map) {
        zfbRecharge(context, jSONObject, map);
        return 0;
    }

    public void rechargeService(Context context, JSONObject jSONObject, Map<String, String> map) {
        String str = null;
        if (jSONObject.has(NumberInfo.TYPE_KEY)) {
            try {
                if ("ZFB_RECHARGE".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    if (map == null) {
                        map = new HashMap();
                        if (jSONObject.has("simIndex")) {
                            map.put("simIndex", jSONObject.getString("simIndex"));
                        }
                        if (jSONObject.has("exphone")) {
                            map.put("exphone", jSONObject.getString("exphone"));
                        }
                    }
                    zfbRecharge(context, jSONObject, map);
                } else if ("ALIPAY_RECHARGE".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    Map map2;
                    if (map != null) {
                        map2 = map;
                    } else {
                        map2 = new HashMap();
                        if (jSONObject.has("simIndex")) {
                            map2.put("simIndex", jSONObject.getString("simIndex"));
                        }
                        if (jSONObject.has("exphone")) {
                            map2.put("exphone", jSONObject.getString("exphone"));
                        }
                    }
                    String obj = JsonUtil.getValFromJsonObject(jSONObject, "appName").toString();
                    if (map2 != null) {
                        str = (String) map2.get("exphone");
                    }
                    if (StringUtils.isNull(str)) {
                        str = XyUtil.getPhoneNumber(context);
                    }
                    if (!StringUtils.isNull(obj) && checkHasAppName(context, obj)) {
                        I.b(context, str);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map2);
                } else if ("WEXIN_RECHARGE".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    str = jSONObject.getString("appName");
                    if (checkHasAppName(context, str)) {
                        a.a(context, str);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else if ("TENPAY_RECHARGE".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    str = jSONObject.getString("appName");
                    if (checkHasAppName(context, str)) {
                        a.a(context, str);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else if ("BAIDU_RECHARGE".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    str = jSONObject.optString(Constant.URLS);
                    if (StringUtils.isNull(str)) {
                        str = jSONObject.optString("appName");
                    }
                    openUrl(context, str, jSONObject);
                } else {
                    otherRecharge(context, jSONObject, map);
                }
            } catch (JSONException e) {
            }
        }
    }

    public void repayment(Context context, JSONObject jSONObject) {
        zfbRepayment(context, jSONObject);
    }

    public void repaymentService(Context context, JSONObject jSONObject, Map<String, String> map) {
        if (jSONObject.has(NumberInfo.TYPE_KEY)) {
            try {
                if ("ZFB_REPAYMENT".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    zfbRepayment(context, jSONObject);
                } else if ("ALIPAY_REPAYMENT".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    if (checkHasAppName(context, jSONObject.getString("appName"))) {
                        Context context2 = context;
                        I.a(context2, (String) JsonUtil.getValueFromJsonObject(jSONObject, "cardNumber"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "repayAmount"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "holderName"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "bankCode"), false);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else if ("WEXIN_REPAYMENT".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    r0 = jSONObject.getString("appName");
                    if (checkHasAppName(context, r0)) {
                        a.a(context, r0);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else if ("TENPAY_REPAYMENT".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    r0 = jSONObject.getString("appName");
                    if (checkHasAppName(context, r0)) {
                        a.a(context, r0);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else if ("BAIDU_REPAYMENT".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    r0 = jSONObject.optString(Constant.URLS);
                    if (StringUtils.isNull(r0)) {
                        r0 = jSONObject.optString("appName");
                    }
                    openUrl(context, r0, jSONObject);
                } else {
                    otherRepayment(context, jSONObject, map);
                }
            } catch (JSONException e) {
            }
        }
    }

    public void replySms(Context context, String str, String str2, String str3, Map<String, String> map) {
    }

    public void sendEmailTo(Context context, String str, Map map) {
        String format = String.format("mailto:%s", new Object[]{str});
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setData(Uri.parse(format));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public abstract void sendSms(Context context, String str, String str2, int i, Map<String, String> map);

    public void setNewTaskIfNeed(Context context, Intent intent) {
        if (context != null && intent != null) {
            try {
                if (!(context instanceof Activity)) {
                    intent.setFlags(268435456);
                }
            } catch (Throwable th) {
            }
        }
    }

    public void showXyToast(Context context, String str, Map map) {
        if (context != null && !StringUtils.isNull(str)) {
            Toast.makeText(context, str, 0).show();
        }
    }

    public void simChange() {
        NetUtil.executeRunnable(new a(this));
    }

    public void startWebActivity(Context context, Intent intent) {
        context.startActivity(intent);
    }

    public void startWizardService(Context context, JSONObject jSONObject, Map map) {
    }

    public void statisticAction(String str, String str2, Map<String, Object> map) {
    }

    public void toService(Context context, String str, JSONObject jSONObject) {
        Map hashMap = new HashMap();
        if (jSONObject != null) {
            try {
                if (jSONObject.has("actionName")) {
                    hashMap.put("actionName", jSONObject.optString("actionName"));
                }
                if (jSONObject.has("otherPackageName")) {
                    hashMap.put("otherPackageName", jSONObject.optString("otherPackageName"));
                }
                if (jSONObject.has("extend")) {
                    JsonUtil.putJsonToMap(new JSONObject(jSONObject.optString("extend")), hashMap);
                }
            } catch (JSONException e) {
            } catch (Throwable th) {
            }
        }
        String optString = jSONObject.optString(NumberInfo.TYPE_KEY);
        if (!StringUtils.isNull(optString)) {
            if (optString.toUpperCase().startsWith("WEB_")) {
                String str2 = "";
                if (!hashMap.isEmpty()) {
                    if (StringUtils.isNull(str2)) {
                        str2 = (String) hashMap.get("phone");
                    }
                    if (StringUtils.isNull(str2)) {
                        str2 = (String) hashMap.get("phoneNum");
                    }
                }
                String str3 = (String) hashMap.get("simIndex");
                if (str3 != null) {
                    jSONObject.put("simIndex", str3);
                }
                a(context, jSONObject, optString, str2, hashMap);
                return;
            }
        }
        if ("repaymentService".equalsIgnoreCase(str)) {
            repaymentService(context, jSONObject, hashMap);
        } else if ("rechargeService".equalsIgnoreCase(str)) {
            rechargeService(context, jSONObject, hashMap);
        } else if ("trafficService".equalsIgnoreCase(str)) {
            trafficService(context, jSONObject, hashMap);
        } else {
            m.a(context, str, jSONObject);
        }
    }

    public void trafficService(Context context, JSONObject jSONObject, Map<String, String> map) {
        if (jSONObject.has(NumberInfo.TYPE_KEY)) {
            try {
                String str;
                if ("WEB_TRAFFIC_ORDER".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    String str2 = "";
                    if (!(map == null || map.isEmpty())) {
                        if (StringUtils.isNull(str2)) {
                            str2 = (String) map.get("phone");
                        }
                        if (StringUtils.isNull(str2)) {
                            str2 = (String) map.get("phoneNum");
                        }
                    }
                    if (map != null) {
                        str = (String) map.get("simIndex");
                        if (str != null) {
                            jSONObject.put("simIndex", str);
                        }
                    }
                    a(context, jSONObject, "WEB_TRAFFIC_ORDER", str2, map);
                } else if ("TAOBAO_TRAFFIC".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    str = jSONObject.getString("appName");
                    if (checkHasAppName(context, str)) {
                        a.a(context, str);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else if ("WEIXIN_TRAFFIC".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    str = jSONObject.getString("appName");
                    if (checkHasAppName(context, str)) {
                        a.a(context, str);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else if ("TENPAY_TRAFFIC".equalsIgnoreCase(jSONObject.getString(NumberInfo.TYPE_KEY))) {
                    str = jSONObject.getString("appName");
                    if (checkHasAppName(context, str)) {
                        a.a(context, str);
                        return;
                    }
                    jSONObject.put(NumberInfo.TYPE_KEY, "WEB_URL");
                    a(context, jSONObject, "WEB_URL", null, map);
                } else {
                    otherOrderTraffic(context, jSONObject, map);
                }
            } catch (JSONException e) {
            }
        }
    }

    public void viewCalendar(Context context, long j, Map map) {
        Builder buildUpon = CalendarContract.CONTENT_URI.buildUpon();
        buildUpon.appendPath("time");
        ContentUris.appendId(buildUpon, j);
        try {
            context.startActivity(new Intent("android.intent.action.VIEW").setData(buildUpon.build()));
        } catch (Exception e) {
        }
    }

    public void zfbRecharge(Context context, JSONObject jSONObject, Map<String, String> map) {
        String str = null;
        try {
            String obj = JsonUtil.getValFromJsonObject(jSONObject, "appName").toString();
            if (map != null) {
                str = (String) map.get("exphone");
            }
            if (StringUtils.isNull(str)) {
                str = XyUtil.getPhoneNumber(context);
            }
            String str2 = str;
            if (!StringUtils.isNull(obj) && checkHasAppName(context, obj)) {
                I.b(context, str2);
                return;
            }
            Object obj2 = ThemeUtil.SET_NULL_STR;
            if (map != null) {
                str = (String) map.get("simIndex");
            }
            if (obj2 == null) {
                obj2 = ThemeUtil.SET_NULL_STR;
            }
            jSONObject.put("simIndex", obj2);
            if (str2 != null) {
                jSONObject.put("chang_phone", str2);
            }
            jSONObject.put(NumberInfo.TYPE_KEY, "WEB_CHONG_ZHI");
            a(context, jSONObject, "WEB_CHONG_ZHI", null, map);
        } catch (Throwable th) {
        }
    }

    public void zfbRepayment(Context context, JSONObject jSONObject) {
        zfbRepayment(context, jSONObject, null);
    }

    public void zfbRepayment(Context context, JSONObject jSONObject, Map<String, String> map) {
        try {
            if (checkHasAppName(context, jSONObject.getString("appName"))) {
                Context context2 = context;
                I.a(context2, (String) JsonUtil.getValueFromJsonObject(jSONObject, "cardNumber"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "repayAmount"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "holderName"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "bankCode"), false);
                return;
            }
            jSONObject.put(NumberInfo.TYPE_KEY, "WEB_REPAYMENT");
            a(context, jSONObject, "WEB_REPAYMENT", null, map);
        } catch (Throwable th) {
        }
    }
}
