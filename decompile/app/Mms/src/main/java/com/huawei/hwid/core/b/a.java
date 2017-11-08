package com.huawei.hwid.core.b;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/* compiled from: AccountInfoPreferences */
public class a {
    private static a b;
    private SharedPreferences a;

    public a(Context context) {
        this.a = context.getSharedPreferences("HwAccount", 4);
    }

    public static synchronized a a(Context context) {
        a aVar;
        synchronized (a.class) {
            if (b == null) {
                b = new a(context);
            }
            aVar = b;
        }
        return aVar;
    }

    public String a(String str, String str2) {
        return this.a == null ? str2 : this.a.getString(str, str2);
    }

    public long a(String str, long j) {
        return this.a == null ? j : this.a.getLong(str, j);
    }

    public void b(String str, String str2) {
        Editor edit = this.a.edit();
        if (edit != null) {
            edit.putString(str, str2).commit();
        }
    }

    public void b(String str, long j) {
        Editor edit = this.a.edit();
        if (edit != null) {
            edit.putLong(str, j).commit();
        }
    }

    public void a(String str, int i) {
        Editor edit = this.a.edit();
        if (edit != null) {
            edit.putInt(str, i).commit();
        }
    }

    public boolean a(String str) {
        if (this.a != null && this.a.contains(str)) {
            return true;
        }
        return false;
    }

    public void b(String str) {
        Editor edit = this.a.edit();
        if (edit != null) {
            edit.remove(str).commit();
        }
    }
}
