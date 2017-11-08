package cn.com.xy.sms.sdk.action;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.CalendarContract;
import android.provider.ContactsContract.Contacts;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.widget.Toast;
import cn.com.xy.sms.sdk.dex.DexUtil;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/* compiled from: Unknown */
final class b {
    b() {
    }

    public static String a(String str) {
        String str2 = "";
        Date convertDate = DexUtil.convertDate(str);
        return convertDate == null ? str2 : new SimpleDateFormat("HH:mm").format(convertDate);
    }

    public static void a(Context context, int i, int i2) {
        Intent intent = new Intent("android.intent.action.SET_ALARM");
        intent.putExtra("android.intent.extra.alarm.HOUR", i);
        intent.putExtra("android.intent.extra.alarm.MINUTES", i2);
        intent.putExtra("android.intent.extra.alarm.SKIP_UI", false);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public static void a(Context context, long j) {
        Builder buildUpon = CalendarContract.CONTENT_URI.buildUpon();
        buildUpon.appendPath("time");
        ContentUris.appendId(buildUpon, j);
        try {
            context.startActivity(new Intent("android.intent.action.VIEW").setData(buildUpon.build()));
        } catch (Exception e) {
        }
    }

    public static void a(Context context, String str) {
        String format = String.format("mailto:%s", new Object[]{str});
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setData(Uri.parse(format));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public static void a(Context context, String str, String str2, String str3, String str4) {
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

    public static long b(String str) {
        long currentTimeMillis = System.currentTimeMillis();
        Date convertDate = DexUtil.convertDate(str);
        return convertDate == null ? currentTimeMillis : convertDate.getTime();
    }

    private static void b(Context context, String str) {
        ((ClipboardManager) context.getSystemService("clipboard")).setText(str);
        Toast.makeText(context, "已复制到剪贴板", 1).show();
    }

    private static String c(String str) {
        Object obj = "时";
        Object obj2 = "分";
        return (str.contains(obj) && str.contains(obj2)) ? str.substring(0, str.indexOf(obj2)).replaceAll(obj, ":") : str;
    }

    private static String d(String str) {
        Object obj = "年";
        Object obj2 = "月";
        Object obj3 = "日";
        Object obj4 = "时";
        Object obj5 = "分";
        int i = Calendar.getInstance().get(1);
        int i2 = Calendar.getInstance().get(2);
        int i3 = Calendar.getInstance().get(5);
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        String[] split;
        if (str.contains(obj2) && str.contains(obj3)) {
            String replaceAll = str.substring(0, str.indexOf(obj3)).replaceAll(obj2, "-");
            if (replaceAll.contains(obj)) {
                str = replaceAll.replaceAll(obj, "-");
            } else {
                str = String.format("%d-%s", new Object[]{Integer.valueOf(i), replaceAll});
            }
        }
        if (str.contains(obj4) && str.contains(obj5)) {
            str = str.substring(0, str.indexOf(obj5)).replaceAll(obj4, "-");
        }
        if (str.contains(" ")) {
            str = str.substring(0, str.lastIndexOf(" "));
        }
        if (str.contains("/")) {
            split = str.split("\\/");
            if (split != null) {
                if (split.length == 3) {
                    i = Integer.parseInt(split[0]);
                    i2 = Integer.parseInt(split[1]);
                    i3 = Integer.parseInt(split[2]);
                } else if (split.length == 2) {
                    i2 = Integer.parseInt(split[0]);
                    i3 = Integer.parseInt(split[1]);
                }
            }
        }
        if (str.contains("-")) {
            split = str.split("-");
            if (split != null) {
                if (split.length == 3) {
                    i = Integer.parseInt(split[0]);
                    i2 = Integer.parseInt(split[1]);
                    i3 = Integer.parseInt(split[2]);
                } else if (split.length == 2) {
                    i2 = Integer.parseInt(split[0]);
                    i3 = Integer.parseInt(split[1]);
                }
            }
        }
        return String.format("%d-%02d-%02d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3)});
    }
}
