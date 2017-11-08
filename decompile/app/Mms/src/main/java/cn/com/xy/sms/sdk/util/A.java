package cn.com.xy.sms.sdk.util;

import android.content.Context;

/* compiled from: Unknown */
public final class a {
    public static String a() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            stringBuilder.append(DuoquUtils.getCode(i));
        }
        return stringBuilder.toString();
    }

    public static boolean a(Context context, String str) {
        try {
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(str));
            return true;
        } catch (Throwable th) {
            return false;
        }
    }
}
