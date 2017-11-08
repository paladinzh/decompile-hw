package com.loc;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import java.lang.reflect.Method;

/* compiled from: SPUtil */
public class cv {
    private static Method a;

    public static void a(Context context, String str, String str2, int i) {
        try {
            Editor edit = context.getSharedPreferences(str, 0).edit();
            edit.putInt(str2, i);
            a(edit);
        } catch (Throwable th) {
            e.a(th, "SPUtil", "setPrefsInt");
        }
    }

    public static void a(Context context, String str, String str2, long j) {
        try {
            Editor edit = context.getSharedPreferences(str, 0).edit();
            edit.putLong(str2, j);
            a(edit);
        } catch (Throwable th) {
            e.a(th, "SPUtil", "setPrefsLong");
        }
    }

    public static void a(Editor editor) {
        if (editor != null) {
            if (VERSION.SDK_INT < 9) {
                editor.commit();
            } else {
                try {
                    if (a == null) {
                        a = Editor.class.getDeclaredMethod("apply", new Class[0]);
                    }
                    a.invoke(editor, new Object[0]);
                } catch (Throwable th) {
                    e.a(th, "SPUtil", "applySharedPreference");
                    editor.commit();
                }
            }
        }
    }

    public static int b(Context context, String str, String str2, int i) {
        try {
            i = context.getSharedPreferences(str, 0).getInt(str2, i);
        } catch (Throwable th) {
            e.a(th, "SPUtil", "getPrefsInt");
        }
        return i;
    }

    public static long b(Context context, String str, String str2, long j) {
        try {
            j = context.getSharedPreferences(str, 0).getLong(str2, j);
        } catch (Throwable th) {
            e.a(th, "SPUtil", "getPrefsLong");
        }
        return j;
    }
}
