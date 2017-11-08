package com.google.android.gms.tagmanager;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;

/* compiled from: Unknown */
class cz {
    static void a(Context context, String str, String str2, String str3) {
        Editor edit = context.getSharedPreferences(str, 0).edit();
        edit.putString(str2, str3);
        a(edit);
    }

    static void a(final Editor editor) {
        if (VERSION.SDK_INT < 9) {
            new Thread(new Runnable() {
                public void run() {
                    editor.commit();
                }
            }).start();
        } else {
            editor.apply();
        }
    }
}
