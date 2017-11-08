package com.loc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.amap.api.location.AMapLocation;
import org.json.JSONObject;

/* compiled from: LastLocationManager */
public class i {
    private static i e;
    Context a;
    SharedPreferences b = null;
    Editor c = null;
    private String d = null;

    private i(Context context) {
        this.a = context;
        if (this.d == null) {
            this.d = cg.a("MD5", this.a.getPackageName());
        }
    }

    public static i a(Context context) {
        if (e == null) {
            e = new i(context);
        }
        return e;
    }

    public AMapLocation a() {
        AMapLocation aMapLocation = null;
        if (this.a == null) {
            return aMapLocation;
        }
        SharedPreferences sharedPreferences = this.a.getSharedPreferences("pref", 0);
        if (sharedPreferences == null) {
            return aMapLocation;
        }
        String str = "lastfix" + e.f;
        if (sharedPreferences.contains(str)) {
            try {
                String string = sharedPreferences.getString(str, null);
                if (string != null) {
                    string = new String(cg.d(r.b(string), this.d), "UTF-8");
                }
            } catch (Throwable th) {
                e.a(th, "LastLocationManager", "getLastFix part1");
                sharedPreferences.edit().remove(str).commit();
            }
            if (!TextUtils.isEmpty(r0)) {
                try {
                    aMapLocation = e.a(new JSONObject(r0));
                } catch (Throwable th2) {
                    e.a(th2, "LastLocationManager", "getLastFix part2");
                }
            }
            return aMapLocation;
        }
        Object obj = aMapLocation;
        if (TextUtils.isEmpty(obj)) {
            aMapLocation = e.a(new JSONObject(obj));
        }
        return aMapLocation;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(AMapLocation aMapLocation) {
        Object obj = null;
        if (this.a != null && cw.a(aMapLocation) && aMapLocation.getLocationType() != 2) {
            if (this.b == null) {
                this.b = this.a.getSharedPreferences("pref", 0);
            }
            if (this.c == null) {
                this.c = this.b.edit();
            }
            if (!TextUtils.isEmpty(aMapLocation.toStr())) {
                byte[] c;
                try {
                    c = cg.c(aMapLocation.toStr().getBytes("UTF-8"), this.d);
                } catch (Throwable th) {
                    e.a(th, "LastLocationManager", "setLastFix");
                    c = null;
                }
                obj = r.b(c);
            }
            if (!TextUtils.isEmpty(obj)) {
                this.c.putString("lastfix" + e.f, obj);
                cv.a(this.c);
            }
        }
    }
}
