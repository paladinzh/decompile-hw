package com.huawei.hwid.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.huawei.hwid.core.encrypt.e;

public class i {
    private static i a = null;
    private SharedPreferences b;

    public i(Context context) {
        this.b = context.getSharedPreferences("HwIDVersionInfo", 0);
    }

    public static synchronized i a(Context context) {
        i iVar;
        synchronized (i.class) {
            if (a == null) {
                a = new i(context);
            }
            iVar = a;
        }
        return iVar;
    }

    public void a(Context context, String str, String str2, String str3) {
        if (this.b != null) {
            Editor edit = this.b.edit();
            edit.putString("component_id", e.b(context, str));
            edit.putString("version_name", e.b(context, str2));
            edit.putString("download_path", e.b(context, str3));
            edit.commit();
        }
    }

    public SharedPreferences a() {
        return this.b;
    }

    public String b(Context context) {
        if (this.b == null) {
            return "";
        }
        return e.c(context, this.b.getString("version_name", ""));
    }

    public String c(Context context) {
        if (this.b == null) {
            return "";
        }
        return e.c(context, this.b.getString("download_path", ""));
    }

    public void b() {
        if (this.b != null) {
            Editor edit = this.b.edit();
            edit.clear();
            edit.commit();
        }
    }
}
