package com.a.a.a;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.a.a.m;
import java.io.File;

/* compiled from: Unknown */
public class k {
    public static m a(Context context) {
        return a(context, null);
    }

    public static m a(Context context, e eVar) {
        return a(context, eVar, -1);
    }

    public static m a(Context context, e eVar, int i) {
        File file = new File(context.getCacheDir(), "volley");
        String str = "volley/0";
        try {
            str = context.getPackageName();
            str + "/" + context.getPackageManager().getPackageInfo(str, 0).versionCode;
        } catch (NameNotFoundException e) {
        }
        if (eVar == null) {
            eVar = new f();
        }
        m mVar = new m(new b(file), new a(eVar));
        mVar.a();
        return mVar;
    }
}
