package com.huawei.hwid.core.d;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.widget.Toast;
import com.huawei.hwid.core.d.b.e;

public class m {
    public static final boolean a = (VERSION.SDK_INT >= 11);

    public static Builder a(Context context, int i, int i2, boolean z) {
        return a(context, i2, context.getString(i), z);
    }

    public static Builder a(Context context, int i, String str, boolean z) {
        if (context != null) {
            Builder builder = new Builder(context, a(context));
            builder.setMessage(str);
            if (i != j.a(context, "CS_title_tips")) {
                builder.setTitle(i);
            }
            if (z) {
                builder.setPositiveButton(17039370, new n(context));
                builder.setOnCancelListener(new o(context));
            } else {
                builder.setPositiveButton(17039370, null);
            }
            return builder;
        }
        e.b("UIUtil", "activity is null");
        return null;
    }

    public static void a(Context context, String str, int i) {
        if (b.g()) {
            Toast.makeText(context, str, i).show();
            return;
        }
        int b = b(context);
        if (b != 0) {
            context.setTheme(b);
        }
        Toast.makeText(context, str, i).show();
    }

    public static int a(Context context) {
        if (context != null) {
            return (VERSION.SDK_INT >= 16 && b(context) != 0) ? 0 : 3;
        } else {
            e.b("UIUtil", "getDialogThemeId, context is null");
            return 3;
        }
    }

    public static int b(Context context) {
        if (d.a()) {
            return context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
        }
        return context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
    }

    public static void c(Context context) {
        if (context != null) {
            try {
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(intent);
            } catch (Exception e) {
                e.d("UIUtil", e.getMessage());
            }
        }
    }
}
