package com.loc;

import android.content.ContentValues;
import android.database.Cursor;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import java.util.HashMap;
import java.util.Map;

/* compiled from: DynamicFileEntity */
public class be implements ak<bf> {
    private bf a;

    public static String a(String str) {
        Map hashMap = new HashMap();
        hashMap.put("sdkname", str);
        return aj.a(hashMap);
    }

    public static String a(String str, String str2) {
        Map hashMap = new HashMap();
        hashMap.put("sdkname", str);
        hashMap.put("dynamicversion", str2);
        return aj.a(hashMap);
    }

    public static String a(String str, String str2, String str3, String str4) {
        Map hashMap = new HashMap();
        hashMap.put("filename", str);
        hashMap.put("sdkname", str2);
        hashMap.put("dynamicversion", str4);
        hashMap.put(NumberInfo.VERSION_KEY, str3);
        return aj.a(hashMap);
    }

    public static String b(String str) {
        Map hashMap = new HashMap();
        hashMap.put("filename", str);
        return aj.a(hashMap);
    }

    public static String b(String str, String str2) {
        Map hashMap = new HashMap();
        hashMap.put("sdkname", str);
        hashMap.put("status", str2);
        return aj.a(hashMap);
    }

    public ContentValues a() {
        if (this.a == null) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", this.a.a());
        contentValues.put("md5", this.a.b());
        contentValues.put("sdkname", this.a.c());
        contentValues.put(NumberInfo.VERSION_KEY, this.a.d());
        contentValues.put("dynamicversion", this.a.e());
        contentValues.put("status", this.a.f());
        return contentValues;
    }

    public /* synthetic */ Object a(Cursor cursor) {
        return b(cursor);
    }

    public void a(bf bfVar) {
        this.a = bfVar;
    }

    public /* synthetic */ void a(Object obj) {
        a((bf) obj);
    }

    public bf b(Cursor cursor) {
        String string = cursor.getString(1);
        String string2 = cursor.getString(2);
        String string3 = cursor.getString(3);
        String string4 = cursor.getString(4);
        String string5 = cursor.getString(5);
        return new a(string2, string3, string, string4, string5).a(cursor.getString(6)).a();
    }

    public String b() {
        return "file";
    }
}
